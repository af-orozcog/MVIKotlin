package com.arkivanov.mvikotlin.timetravel.client.internal.client

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.value.ValueNode
import com.badoo.reaktive.subject.behavior.BehaviorObservable

interface TimeTravelClient {

    val models: BehaviorObservable<Model>

    fun onConnectClicked()
    fun onDisconnectClicked()
    fun onStartRecordingClicked()
    fun onStopRecordingClicked()
    fun onMoveToStartClicked()
    fun onStepBackwardClicked()
    fun onStepForwardClicked()
    fun onMoveToEndClicked()
    fun onCancelClicked()
    fun onDebugEventClicked()
    fun onEventSelected(listIndex: Int, eventIndex: Int)
    fun onExportEventsClicked()
    fun onImportEventsClicked()
    fun onDismissErrorClicked()

    data class Model(
        val events: List<List<String>>,
        val currentEventIndex: Int,
        val buttons: Buttons,
        val selectedEventListIndex: Int,
        val selectedEventIndex: Int,
        val selectedEventValue: ValueNode?,
        val errorText: String?
    ) {
        data class Buttons(
            val isConnectEnabled: Boolean,
            val isDisconnectEnabled: Boolean,
            val isStartRecordingEnabled: Boolean,
            val isStopRecordingEnabled: Boolean,
            val isMoveToStartEnabled: Boolean,
            val isStepBackwardEnabled: Boolean,
            val isStepForwardEnabled: Boolean,
            val isMoveToEndEnabled: Boolean,
            val isCancelEnabled: Boolean,
            val isDebugEventEnabled: Boolean,
            val isExportEventsEnabled: Boolean,
            val isImportEventsEnabled: Boolean
        )
    }
}
