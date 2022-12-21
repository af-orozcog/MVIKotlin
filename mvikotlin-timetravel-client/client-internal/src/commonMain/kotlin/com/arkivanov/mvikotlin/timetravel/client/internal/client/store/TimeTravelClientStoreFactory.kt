package com.arkivanov.mvikotlin.timetravel.client.internal.client.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.reaktive.ReaktiveExecutor
import com.arkivanov.mvikotlin.timetravel.client.internal.client.Connector
import com.arkivanov.mvikotlin.timetravel.client.internal.client.store.TimeTravelClientStore.Intent
import com.arkivanov.mvikotlin.timetravel.client.internal.client.store.TimeTravelClientStore.Label
import com.arkivanov.mvikotlin.timetravel.client.internal.client.store.TimeTravelClientStore.State
import com.arkivanov.mvikotlin.timetravel.client.internal.client.store.TimeTravelClientStore.State.Connection
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelcomand.TimeTravelCommand
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetraveleventsupdate.TimeTravelEventsUpdate
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.TimeTravelFunction
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunctionlist.TimeTravelFunctionList
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelstateupdate.TimeTravelStateUpdate
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.value.ValueNode
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.doOnBeforeFinally
import com.badoo.reaktive.observable.doOnBeforeSubscribe
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.TimeTravelEvent as TimeTravelEventProto

internal class TimeTravelClientStoreFactory(
    private val storeFactory: StoreFactory,
    private val connector: Connector,
) {

    fun create(): TimeTravelClientStore =
        object : TimeTravelClientStore, Store<Intent, State, Label> by storeFactory.create(
            initialState = State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
            exposedFunctions = emptyMap(),
            exposedFunctionsSignature = TimeTravelFunctionList(emptyList())
        ) {}

    private sealed class Msg {
        data class Connecting(val disposable: Disposable) : Msg()
        data class Connected(val writer: (TimeTravelCommand) -> Unit) : Msg()
        object Disconnected : Msg()
        data class StateUpdate(val stateUpdate: TimeTravelStateUpdate) : Msg()
        data class EventSelected(val listIndex: Int, val eventIndex: Int) : Msg()
        data class EventValue(val eventId: Long, val value: ValueNode) : Msg()
        data class ErrorChanged(val text: String?) : Msg()
        data class ExposedFunctions(val functions:List<TimeTravelFunction>): Msg()
    }

    private inner class ExecutorImpl : ReaktiveExecutor<Intent, Nothing, State, Msg, Label>() {
        override fun executeIntent(intent: Intent, getState: () -> State): Unit =
            when (intent) {
                is Intent.Connect -> connectIfNeeded(getState())
                is Intent.Disconnect -> disconnectIfNeeded(getState())
                is Intent.StartRecording -> sendIfNeeded(getState()) { TimeTravelCommand.StartRecording }
                is Intent.StopRecording -> sendIfNeeded(getState()) { TimeTravelCommand.StopRecording }
                is Intent.MoveToStart -> sendIfNeeded(getState()) { TimeTravelCommand.MoveToStart }
                is Intent.StepBackward -> sendIfNeeded(getState()) { TimeTravelCommand.StepBackward }
                is Intent.StepForward -> sendIfNeeded(getState()) { TimeTravelCommand.StepForward }
                is Intent.MoveToEnd -> sendIfNeeded(getState()) { TimeTravelCommand.MoveToEnd }
                is Intent.ReplicateEvents -> sendIfNeeded(getState()) { TimeTravelCommand.ReplicateEvents }
                is Intent.Cancel -> sendIfNeeded(getState()) { TimeTravelCommand.Cancel }
                is Intent.DebugEvent -> debugEventIfNeeded(getState())
                is Intent.SelectEvent -> selectEvent(intent.listIndex, intent.eventIndex, getState())
                is Intent.ExportEvents -> sendIfNeeded(getState()) { TimeTravelCommand.ExportEvents }
                is Intent.ImportEvents -> sendIfNeeded(getState()) { TimeTravelCommand.ImportEvents(intent.data) }
                is Intent.DismissError -> dispatch(Msg.ErrorChanged(text = null))
                is Intent.ApplyFunction -> applyFunction(listIndex = intent.listIndex, eventIndex = intent.eventIndex, functionName = intent.functionName, arguments = intent.arguments, state = getState())
            }

        private fun connectIfNeeded(state: State): Unit =
            when (state.connection) {
                is Connection.Disconnected -> connect()
                is Connection.Connecting,
                is Connection.Connected -> Unit
            }

        private fun connect() {
            connector
                .connect()
                .doOnBeforeSubscribe { dispatch(Msg.Connecting(it)) }
                .doOnBeforeFinally { dispatch(Msg.Disconnected) }
                .subscribeScoped(onNext = ::onEvent)
        }

        private fun onEvent(event: Connector.Event): Unit =
            when (event) {
                is Connector.Event.Connected -> dispatch(Msg.Connected(event.writer))
                is Connector.Event.StateUpdate -> dispatch(Msg.StateUpdate(event.stateUpdate))
                is Connector.Event.EventValue -> dispatch(Msg.EventValue(eventId = event.eventId, value = event.value))
                is Connector.Event.ExposedFunctions -> dispatch(Msg.ExposedFunctions(functions = event.functions))
                is Connector.Event.ExportEvents -> publish(Label.ExportEvents(event.data))
                is Connector.Event.Error -> dispatch(Msg.ErrorChanged(text = event.text))
            }

        private fun disconnectIfNeeded(state: State) {
            val disposable =
                when (val connection = state.connection) {
                    is Connection.Disconnected -> return
                    is Connection.Connecting -> connection.disposable
                    is Connection.Connected -> connection.disposable
                }

            disposable.dispose()
            dispatch(Msg.Disconnected)
        }

        private inline fun sendIfNeeded(state: State, command: Connection.Connected.() -> TimeTravelCommand?): Unit =
            when (val connection = state.connection) {
                is Connection.Disconnected,
                is Connection.Connecting -> Unit
                is Connection.Connected -> {
                    connection.command()?.also(connection.writer)
                    Unit
                }
            }

        private fun debugEventIfNeeded(state: State) {
            sendIfNeeded(state) {
                events
                    .getOrNull(selectedEventListIndex)
                    ?.getOrNull(selectedEventIndex)?.id
                    ?.let(TimeTravelCommand::DebugEvent)
            }
        }

        private fun selectEvent(listIndex: Int, eventIndex: Int, state: State) {
            dispatch(Msg.EventSelected(listIndex = listIndex, eventIndex = eventIndex))

            sendIfNeeded(state) {
                events
                    .getOrNull(listIndex)
                    ?.getOrNull(eventIndex)
                    ?.takeIf { it.value == null }
                    ?.id
                    ?.let{
                        (TimeTravelCommand::AnalyzeEvent)(listIndex,it)
                    }
            }
        }

        private fun applyFunction(listIndex: Int, eventIndex: Int, functionName: String, arguments: List<Pair<String, Any>>, state: State) {
            sendIfNeeded(state) {
                events
                    .getOrNull(listIndex)
                    ?.getOrNull(eventIndex)
                    ?.takeIf { it.value == null }
                    ?.id
                    ?.let{
                        (TimeTravelCommand::ApplyFunction)(it,functionName,arguments)
                    }
            }
        }

    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Msg.Connecting -> copy(connection = Connection.Connecting(disposable = msg.disposable))
                is Msg.Connected -> copy(connection = connection.applyConnected(msg))
                is Msg.Disconnected -> copy(connection = Connection.Disconnected)
                is Msg.StateUpdate -> copy(connection = connection.applyStateUpdate(msg))
                is Msg.EventSelected -> copy(connection = connection.applyEventSelected(msg))
                is Msg.ExposedFunctions -> copy(connection = connection.applyExposedFunctions(msg))
                is Msg.EventValue -> copy(connection = connection.applyEventValue(msg))
                is Msg.ErrorChanged -> copy(errorText = msg.text)
            }

        private fun Connection.applyConnected(msg: Msg.Connected): Connection =
            when (this) {
                is Connection.Disconnected,
                is Connection.Connected -> this
                is Connection.Connecting -> Connection.Connected(disposable = disposable, writer = msg.writer)
            }

        private fun Connection.applyStateUpdate(msg: Msg.StateUpdate): Connection =
            when (this) {
                is Connection.Disconnected,
                is Connection.Connecting -> this
                is Connection.Connected -> applyUpdate(msg.stateUpdate)
            }

        private fun Connection.Connected.applyUpdate(update: TimeTravelStateUpdate): Connection.Connected =
            copy(
                events = events.applyUpdate(update = update.eventsUpdate),
                currentEventIndex = update.selectedEventIndex,
                selectedEventListIndex = update.selectedListEventIndex,
                mode = update.mode,
                selectedEventIndex = selectedEventIndex.coerceAtMost(events.lastIndex)
            )

        private fun addEvent(listIndex: Int, event: List<TimeTravelEvent>, events : List<List<TimeTravelEvent>>): List<List<TimeTravelEvent>> {
            val helperFun = fun(index: Int, list: List<TimeTravelEvent>):List<TimeTravelEvent> {
                var temp = list
                if(index == listIndex){
                    temp = temp + event
                }
                return temp
            }
            return events.mapIndexed{ index: Int, list: List<TimeTravelEvent> -> helperFun(index,list)}
        }

        private fun List<List<TimeTravelEvent>>.applyUpdate(update: TimeTravelEventsUpdate): List<List<TimeTravelEvent>> {
            when (update) {
                is TimeTravelEventsUpdate.All -> return update.events.map { it1 -> it1.map { it.toDomain() } }
                is TimeTravelEventsUpdate.NewList -> {
                    var temp = this
                    temp = listOf(*temp.toTypedArray(), update.events.map{it.toDomain()})
                    return temp
                }
                is TimeTravelEventsUpdate.NewElement -> {
                    return addEvent(update.listIndex,update.events.map { it.toDomain() },this)
                }
            }
        }
        private fun TimeTravelEventProto.toDomain(): TimeTravelEvent =
            TimeTravelEvent(
                id = id,
                storeName = storeName,
                type = type,
                valueType = valueType,
                value = null
            )

        private fun Connection.applyEventSelected(msg: Msg.EventSelected): Connection =
            when (this) {
                is Connection.Disconnected,
                is Connection.Connecting -> this
                is Connection.Connected -> copy(selectedEventListIndex = msg.listIndex, selectedEventIndex = msg.eventIndex)
            }

        private fun Connection.applyExposedFunctions(msg: Msg.ExposedFunctions): Connection =
            when (this) {
                is Connection.Disconnected,
                is Connection.Connecting -> this
                is Connection.Connected -> copy(exposedFunctions = msg.functions)
            }

        private fun Connection.applyEventValue(msg: Msg.EventValue): Connection =
            when (this) {
                is Connection.Disconnected,
                is Connection.Connecting -> this

                is Connection.Connected ->
                    copy(
                        events = events.map { list ->
                            list.map{ event -> event.takeIf { it.id == msg.eventId }?.copy(value = msg.value) ?: event}
                        }
                    )
            }
    }
}
