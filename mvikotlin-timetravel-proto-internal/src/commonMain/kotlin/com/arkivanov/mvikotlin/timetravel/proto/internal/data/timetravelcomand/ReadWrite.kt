package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelcomand

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.TimeTravelFunction
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature.writeTimeTravelParameterSignature
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataReader
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataWriter
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readByteArray
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readInt
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readList
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readString
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeByteArray
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeCollection
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeInt
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeString

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

        is TimeTravelCommand.ApplyFunction -> {
            writeEnum(Type.APPLY_FUNCTION)
            writeInt(timeTravelCommand.listIndex)
            writeLong(timeTravelCommand.eventId)
            writeString(timeTravelCommand.functionName)
            writeCollection(timeTravelCommand.arguments) {
                writePairStringAny(it)
            }
        }

    }.let {}
}

internal fun DataWriter.writePairStringAny(pair: Pair<String,Any>) {
    writeString(pair.first)
    when (pair.first){
        "String" -> writeString(pair.second as String)
        "Int" -> writeInt(pair.second as Int)
        "Long" -> writeLong(pair.second as Long)
    }
}

internal fun DataReader.readPairStringAny():Pair<String,Any> {
    val type:String = readString()!!
    var value:Any = -1
    when (type){
        "String" -> value = readString()!!
        "Int" -> value = readInt()
        "Long" -> value = readLong()
    }
    return Pair(type,value)
}

internal fun DataReader.readListPairStringAny():List<Pair<String,Any>>{
    val use = readList { readPairStringAny() }
    return use as List<Pair<String,Any>>
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
        Type.APPLY_FUNCTION -> TimeTravelCommand.ApplyFunction(listIndex = readInt(), eventId = readLong(), functionName = readString()!!, arguments = readListPairStringAny())
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
    REPLICATE_EVENTS,
    APPLY_FUNCTION
}
