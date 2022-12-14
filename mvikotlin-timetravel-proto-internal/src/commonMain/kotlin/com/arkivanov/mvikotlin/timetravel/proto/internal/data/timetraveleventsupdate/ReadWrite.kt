package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetraveleventsupdate

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.readTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.writeTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataReader
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataWriter
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readInt
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readList
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readListOfList
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeCollection
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeCollectionOfCollection
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeInt

//region Write

internal fun DataWriter.writeTimeTravelEventsUpdate(timeTravelEventsUpdate: TimeTravelEventsUpdate) {
    when (timeTravelEventsUpdate) {
        is TimeTravelEventsUpdate.All -> writeTimeTravelEventsUpdateAll(timeTravelEventsUpdate)
        is TimeTravelEventsUpdate.NewList -> writeTimeTravelEventsUpdateNewList(timeTravelEventsUpdate)
        is TimeTravelEventsUpdate.NewElement -> writeTimeTravelEventsUpdateNewElement(timeTravelEventsUpdate)
    }.let {}
}

private fun DataWriter.writeTimeTravelEventsUpdateAll(timeTravelEventsUpdate: TimeTravelEventsUpdate.All) {
    writeEnum(Type.ALL)
    writeCollectionOfCollection(timeTravelEventsUpdate.events) {
        writeTimeTravelEvent(it)
    }
}

private fun DataWriter.writeTimeTravelEventsUpdateNewList(timeTravelEventsUpdate: TimeTravelEventsUpdate.NewList) {
    writeEnum(Type.NEWELEMENT)

    writeCollection(timeTravelEventsUpdate.events) {
        writeTimeTravelEvent(it)
    }
}

private fun DataWriter.writeTimeTravelEventsUpdateNewElement(timeTravelEventsUpdate: TimeTravelEventsUpdate.NewElement) {
    writeEnum(Type.NEWELEMENT)
    writeInt(timeTravelEventsUpdate.listIndex)
    writeCollection(timeTravelEventsUpdate.events) {
        writeTimeTravelEvent(it)
    }
}

//endregion

//region Read

internal fun DataReader.readTimeTravelEventsUpdate(): TimeTravelEventsUpdate =
    when (readEnum<Type>()) {
        Type.ALL -> readTimeTravelEventsUpdateAll()
        Type.NEWLIST -> readTimeTravelEventsUpdateNewList()
        Type.NEWELEMENT -> readTimeTravelEventsUpdateNewElement()
    }

private fun DataReader.readTimeTravelEventsUpdateAll(): TimeTravelEventsUpdate.All =
    TimeTravelEventsUpdate.All(
        events = readListOfList { readTimeTravelEvent() }!!
    )

private fun DataReader.readTimeTravelEventsUpdateNewList(): TimeTravelEventsUpdate.NewList =
    TimeTravelEventsUpdate.NewList(
        events = readList { readTimeTravelEvent() }!!
    )

private fun DataReader.readTimeTravelEventsUpdateNewElement(): TimeTravelEventsUpdate.NewElement =
    TimeTravelEventsUpdate.NewElement(
        listIndex = readInt(),
        events = readList { readTimeTravelEvent() }!!
    )

//endregion

private enum class Type {

    ALL, NEWLIST,NEWELEMENT
}
