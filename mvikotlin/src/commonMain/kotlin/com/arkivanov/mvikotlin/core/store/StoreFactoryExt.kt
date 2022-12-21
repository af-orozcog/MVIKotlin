package com.arkivanov.mvikotlin.core.store

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.TimeTravelFunction
import com.arkivanov.mvikotlin.utils.internal.atomic
import com.arkivanov.mvikotlin.utils.internal.initialize
import com.arkivanov.mvikotlin.utils.internal.requireValue
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunctionlist.TimeTravelFunctionList

/**
 * A convenience extension function that creates an implementation of [Store]
 * without [Executor] and [Bootstrapper]. [Intent]s are processed directly by the [Reducer].
 *
 * See [StoreFactory.create] for more information.
 */
fun <Intent : Any, State : Any> StoreFactory.create(
    name: String? = null,
    autoInit: Boolean = true,
    initialState: State,
    reducer: Reducer<State, Intent>,
    exposedFunctions:TimeTravelFunctionList
): Store<Intent, State, Nothing> =
    create(
        name = name,
        autoInit = autoInit,
        initialState = initialState,
        executorFactory = ::BypassExecutor,
        reducer = reducer,
        exposedFunctions = exposedFunctions
    )

private class BypassExecutor<Intent : Any, in State : Any> : Executor<Intent, Nothing, State, Intent, Nothing> {
    private val callbacks = atomic<Executor.Callbacks<State, Intent, Nothing>>()

    override fun init(callbacks: Executor.Callbacks<State, Intent, Nothing>) {
        this.callbacks.initialize(callbacks)
    }

    override fun executeIntent(intent: Intent) {
        callbacks.requireValue().onMessage(intent)
    }

    override fun executeAction(action: Nothing) {
        // no-op
    }

    override fun dispose() {
        // no-op
    }
}
