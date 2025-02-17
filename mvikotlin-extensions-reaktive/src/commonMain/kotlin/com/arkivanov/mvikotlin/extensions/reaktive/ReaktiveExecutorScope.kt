package com.arkivanov.mvikotlin.extensions.reaktive

import com.arkivanov.mvikotlin.core.annotations.MainThread
import com.arkivanov.mvikotlin.core.utils.ExperimentalMviKotlinApi
import com.badoo.reaktive.disposable.scope.DisposableScope

/**
 * Allows `Intent` and `Action` DSL handlers to launch asynchronous tasks,
 * read the current [State], [dispatch] ``[Message]s, and [publish] ``[Label]s.
 *
 * Implements [DisposableScope] that is disposed when the [Executor][com.arkivanov.mvikotlin.core.store.Executor] is disposed.
 *
 * @see reaktiveExecutorFactory
 */
@ExperimentalMviKotlinApi
@ReaktiveExecutorDslMaker
interface ReaktiveExecutorScope<out State : Any, in Message : Any, in Label : Any> : DisposableScope {

    /**
     * Returns the current [State] of the [Store][com.arkivanov.mvikotlin.core.store.Store].
     */
    val state: State

    /**
     * Dispatches the provided [Message] to the [Reducer][com.arkivanov.mvikotlin.core.store.Reducer].
     * The updated [State] is available immediately after this method returns.
     * Must be called on the main thread.
     *
     * @param message a [Message] to be dispatched to the [Reducer][com.arkivanov.mvikotlin.core.store.Reducer].
     */
    @MainThread
    fun dispatch(message: Message)

    /**
     * Sends the provided [Label] to the [Store][com.arkivanov.mvikotlin.core.store.Store] for publication.
     * Must be called on the main thread.
     *
     * @param label a [Label] to be published.
     */
    @MainThread
    fun publish(label: Label)
}
