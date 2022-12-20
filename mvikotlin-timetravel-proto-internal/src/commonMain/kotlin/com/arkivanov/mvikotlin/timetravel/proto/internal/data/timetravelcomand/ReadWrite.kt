package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelcomand

import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataReader
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataWriter
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readByteArray
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readInt
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeByteArray
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeInt
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeLong

internal fun DataWriter.writeTimeTravelCommand(timeTravelCommand: TimeTravelCommand) {
    when (timeTravelCommand) {
        is TimeTravelCommand.StartRecording -> writeEnum(Type.START_RECORDING)
        is TimeTravelCommand.StopRecording -> writeEnum(Type.STOP_RECORDING)
        is TimeTravelCommand.MoveToStart -> writeEnum(Type.MOVE_TO_START)
        is TimeTravelCommand.StepBackward -> writeEnum(Type.STEP_BACKWARD)
        is TimeTravelCommand.StepForward -> writeEnum(Type.STEP_FORWARD)
        is TimeTravelCommand.MoveToEnd -> writeEnum(Type.MOVE_TO_END)
        is TimeTravelCommand.Cancel -> writeEnum(Type.CANCEL)
        is TimeTravelCommand.ReplicateEvents -> writeEnum(Type.REPLICATE_EVENTS)

        is TimeTravelCommand.DebugEvent -> {
            writeEnum(Type.DEBUG_EVENT)
            writeLong(timeTravelCommand.eventId)
        }

        is TimeTravelCommand.AnalyzeEvent -> {
            writeEnum(Type.ANALYZE_EVENT)
            writeInt(timeTravelCommand.listIndex)
            writeLong(timeTravelCommand.eventId)
        }

        is TimeTravelCommand.ExportEvents -> writeEnum(Type.EXPORT_EVENTS)

        is TimeTravelCommand.ImportEvents -> {
            writeEnum(Type.IMPORT_EVENTS)
            writeByteArray(timeTravelCommand.data)
        }
    }.let {}
}

internal fun DataReader.readTimeTravelCommand(): TimeTravelCommand =
    when (readEnum<Type>()) {
        Type.START_RECORDING -> TimeTravelCommand.StartRecording
        Type.STOP_RECORDING -> TimeTravelCommand.StopRecording
        Type.MOVE_TO_START -> TimeTravelCommand.MoveToStart
        Type.STEP_BACKWARD -> TimeTravelCommand.StepBackward
        Type.STEP_FORWARD -> TimeTravelCommand.StepForward
        Type.MOVE_TO_END -> TimeTravelCommand.MoveToEnd
        Type.CANCEL -> TimeTravelCommand.Cancel
        Type.DEBUG_EVENT -> TimeTravelCommand.DebugEvent(eventId = readLong())
        Type.ANALYZE_EVENT -> TimeTravelCommand.AnalyzeEvent(listIndex = readInt(), eventId = readLong())
        Type.EXPORT_EVENTS -> TimeTravelCommand.ExportEvents
        Type.IMPORT_EVENTS -> TimeTravelCommand.ImportEvents(data = readByteArray()!!)
        Type.REPLICATE_EVENTS -> TimeTravelCommand.ReplicateEvents
    }

private enum class Type {
    START_RECORDING,
    STOP_RECORDING,
    MOVE_TO_START,
    STEP_BACKWARD,
    STEP_FORWARD,
    MOVE_TO_END,
    CANCEL,
    DEBUG_EVENT,
    ANALYZE_EVENT,
    EXPORT_EVENTS,
    IMPORT_EVENTS,
    REPLICATE_EVENTS
}
