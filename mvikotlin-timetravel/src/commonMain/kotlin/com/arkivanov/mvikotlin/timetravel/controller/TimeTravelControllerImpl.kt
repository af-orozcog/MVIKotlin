package com.arkivanov.mvikotlin.timetravel.controller

import com.arkivanov.mvikotlin.core.annotations.MainThread
import com.arkivanov.mvikotlin.core.store.StoreEventType
import com.arkivanov.mvikotlin.core.utils.assertOnMainThread
import com.arkivanov.mvikotlin.rx.Disposable
import com.arkivanov.mvikotlin.rx.Observer
import com.arkivanov.mvikotlin.rx.internal.BehaviorSubject
import com.arkivanov.mvikotlin.rx.observer
import com.arkivanov.mvikotlin.timetravel.TimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.TimeTravelState
import com.arkivanov.mvikotlin.timetravel.TimeTravelState.Mode
import com.arkivanov.mvikotlin.timetravel.export.TimeTravelExport
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStore
import com.arkivanov.mvikotlin.utils.internal.ensureNeverFrozen
import com.arkivanov.mvikotlin.utils.internal.logE
import kotlin.collections.set

internal class TimeTravelControllerImpl : TimeTravelController {

    init {
        ensureNeverFrozen()
    }

    private var eventId = 1L
    private val stateSubject = BehaviorSubject(TimeTravelState())
    override val state: TimeTravelState get() = stateSubject.value
    private val postponedEvents = ArrayList<TimeTravelEvent>()
    private val stores = HashMap<String, TimeTravelStore<*, *, *>>()

    override fun states(observer: Observer<TimeTravelState>): Disposable = stateSubject.subscribe(observer)

    @MainThread
    fun attachStore(store: TimeTravelStore<*, *, *>, name: String?) {
        assertOnMainThread()

        if (name != null) {
            if (name !in stores) {
                addStore(store = store, name = name)
                return
            }

            logE("Could not enable time travel for the store: $name. Duplicate store name.")
        }

        bypassStore(store)
    }

    @MainThread
    override fun getStore(name:String):TimeTravelStore<*, *, *>{
        return stores[name]!!
    }

    @MainThread
    override fun applyFunction(listIndex: Int, eventId: Long, functionName: String, arguments: List<Pair<String, Any>>) {
        assertOnMainThread()

        var index = state.events.getOrNull(listIndex)?.indexOfFirst { element -> element.id == eventId }?: -1
        if(index == -1 || state.events[listIndex][index].type != StoreEventType.INTENT){
            return
        }
        val functions = getStore(state.events[listIndex][index].storeName).exposedFunctions
        if(!functions.containsKey(functionName)){
            return
        }
        index -= 1

        move(state.events, state.selectedListEventIndex, state.selectedEventIndex, -1)
        if(index != -1)
            move(events = state.events, listIndex = listIndex,from = 0,to = index)
        addNewEventsList(listIndex,0,index)
        swapState { it.copy(mode = Mode.RECORDING) }
        functions[functionName]?.let { it(arguments.map{ it -> it.second}) }
    }

    private fun addNewEventsList(listIndex: Int, fromEvent: Int = 0, toEvent: Int){
        var toAdd: List<TimeTravelEvent> = emptyList()
        for (i in fromEvent..toEvent){
            toAdd = toAdd + state.events[listIndex][i]
        }
        swapState { it.copy(events = listOf(*it.events.toTypedArray(), toAdd), selectedListEventIndex = it.events.size , selectedEventIndex = toAdd.size - 1) }
    }

    private fun addStore(store: TimeTravelStore<*, *, *>, name: String) {
        stores[name] = store
        store.events(
            observer(
                onComplete = { stores -= name },
                onNext = { onEvent(state.selectedListEventIndex,TimeTravelEvent(id = eventId++, storeName = name, type = it.type, value = it.value, state = it.state)) }
            )
        )
    }

    private fun bypassStore(store: TimeTravelStore<*, *, *>) {
        store.events(
            observer { event ->
                store.process(type = event.type, value = event.value)
            }
        )
    }

    override fun startRecording() {
        assertOnMainThread()

        if (state.mode === Mode.IDLE) {
            swapState { it.copy(mode = Mode.RECORDING) }
        }
    }

    override fun stopRecording() {
        assertOnMainThread()

        if (state.mode === Mode.RECORDING) {
            swapState { it.copy(mode = if (it.events.isNotEmpty()) Mode.STOPPED else Mode.IDLE) }
        }
    }

    override fun moveToStart() {
        assertOnMainThread()

        if (state.mode === Mode.STOPPED) {
            move(state.events, state.selectedListEventIndex, state.selectedEventIndex, -1)
        }
    }

    override fun stepBackward() {
        assertOnMainThread()

        if (state.mode === Mode.STOPPED) {
            step(state.events, state.selectedListEventIndex, state.selectedEventIndex, false)
        }
    }

    override fun stepForward() {
        assertOnMainThread()

        if (state.mode === Mode.STOPPED) {
            step(state.events, state.selectedListEventIndex, state.selectedEventIndex, true)
        }
    }

    override fun moveToEnd() {
        assertOnMainThread()

        if (state.mode === Mode.STOPPED) {
            move(state.events, state.selectedListEventIndex, state.selectedEventIndex, state.events[state.selectedListEventIndex].lastIndex)
        }
    }

    override fun cancel() {
        assertOnMainThread()

        if (state.mode !== Mode.IDLE) {
            val oldMode = state.mode
            swapState { it.copy(events = emptyList(), selectedEventIndex = -1, mode = Mode.IDLE) }

            if (oldMode !== Mode.RECORDING) {
                stores.values.forEach { it.restoreState() }
                postponedEvents.forEach { process(it) }
            }

            postponedEvents.clear()
        }
    }

    override fun debugEvent(eventId: Long) {
        assertOnMainThread()

        if (state.mode === Mode.STOPPED) {
            var event: TimeTravelEvent? = null
            for(timeEvent in state.events[state.selectedListEventIndex]){
                if(timeEvent.id == eventId){
                    event = timeEvent
                    break
                }
            }

            if(event == null) return
            stores[event.storeName]?.debug(type = event.type, value = event.value, state = event.state)
        }
    }

    override fun export(): TimeTravelExport {
        assertOnMainThread()
        require(state.mode === Mode.STOPPED)

        val usedStoreNames = HashSet<String>()
        for(list in state.events){
            for(event in list){
                usedStoreNames.add(event.storeName)
            }
        }

        return TimeTravelExport(
            recordedEvents = state.events,
            unusedStoreStates = stores.mapValues { it.value.state }.filterKeys { it !in usedStoreNames }
        )
    }

    override fun import(export: TimeTravelExport) {
        assertOnMainThread()

        if (state.mode === Mode.IDLE) {
            swapState { it.copy(events = export.recordedEvents, selectedEventIndex = -1, mode = Mode.STOPPED) }

            export.unusedStoreStates.forEach { (name, state) ->
                stores[name]?.process(type = StoreEventType.STATE, value = state)
            }

            moveToEnd()
        }
    }

    private fun addEvent(listIndex: Int, event: TimeTravelEvent, events : List<List<TimeTravelEvent>>): List<List<TimeTravelEvent>> {
        val helperFun = fun(index: Int, list: List<TimeTravelEvent>):List<TimeTravelEvent> {
            var temp = list
            if(index == listIndex){
              temp = temp + event
            }
            return temp
        }
        return events.mapIndexed{ index: Int, list: List<TimeTravelEvent> -> helperFun(index,list)}
    }

    private fun onEvent(listIndex: Int, event: TimeTravelEvent) {
        when (state.mode) {
            Mode.RECORDING -> {
                swapState { it.copy(events = addEvent(listIndex, event, it.events), selectedEventIndex = it.events[it.selectedListEventIndex].size) }
                process(event)
            }

            Mode.IDLE -> process(event)

            Mode.STOPPED ->
                if (event.type === StoreEventType.MESSAGE) {
                    process(event)
                } else {
                    postponedEvents += event
                }
        }.let {}
    }

    private fun step(events: List<List<TimeTravelEvent>>, listIndex: Int, from: Int, isForward: Boolean) {
        val progression =
            if (isForward) {
                (from + 1)..events[listIndex].lastIndex
            } else {
                (from - 1) downTo -1
            }

        for (i in progression) {
            val item = events.getOrNull(listIndex)?.getOrNull(i)
            if ((item == null) || (item.type === StoreEventType.STATE)) {
                move(events, listIndex, from, i)
                break
            }
        }
    }

    private fun move(events: List<List<TimeTravelEvent>>, listIndex: Int, from: Int, to: Int, publish: Boolean = true) {
        if (from == to) {
            return
        }

        val set = HashSet<String>()
        val deque = ArrayDeque<TimeTravelEvent>()
        val isForward = to > from
        val progression =
            if (isForward) {
                to downTo from + 1
            } else {
                to + 1..from
            }

        for (i in progression) {
            val event = events[listIndex][i]
            if ((event.type === StoreEventType.STATE) && stores.containsKey(event.storeName) && !set.contains(event.storeName)) {
                set.add(event.storeName)
                deque.addLast(event)
                if (set.size == stores.size) {
                    break
                }
            }
        }
        while (!deque.isEmpty()) {
            deque.removeFirst().also { event ->
                if ((event.type === StoreEventType.STATE) && !isForward) {
                    process(event, event.state)
                } else {
                    process(event)
                }
            }
        }

        if (publish) {
            swapState { it.copy(events = events, selectedEventIndex = to) }
        }
    }

    private fun process(event: TimeTravelEvent, previousValue: Any? = null) {
        stores[event.storeName]?.process(type = event.type, value = previousValue ?: event.value)
    }

    private inline fun swapState(reducer: (TimeTravelState) -> TimeTravelState) {
        stateSubject.onNext(reducer(state))
    }
}
