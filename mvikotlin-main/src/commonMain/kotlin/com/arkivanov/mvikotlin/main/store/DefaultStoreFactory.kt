package com.arkivanov.mvikotlin.main.store

import com.arkivanov.mvikotlin.core.store.Bootstrapper
import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunctionlist.TimeTravelFunctionList

/**
 * An implementation of the [StoreFactory] that creates default implementations of the [Store].
 */
class DefaultStoreFactory : StoreFactory {

    override fun <Intent : Any, Action : Any, Message : Any, State : Any, Label : Any> create(
        name: String?,
        autoInit: Boolean,
        initialState: State,
        bootstrapper: Bootstrapper<Action>?,
        executorFactory: () -> Executor<Intent, Action, State, Message, Label>,
        reducer: Reducer<State, Message>,
        exposedFunctionsSignature:TimeTravelFunctionList,
        exposedFunctions:Map<String,(arguments:List<Any>) -> Unit>
    ): Store<Intent, State, Label> =
        DefaultStore(
            initialState = initialState,
            bootstrapper = bootstrapper,
            executor = executorFactory(),
            reducer = reducer,
            exposedFunctionsSignature = exposedFunctionsSignature,
            exposedFunctions = exposedFunctions
        ).apply {
            if (autoInit) {
                init()
            }
        }
}
