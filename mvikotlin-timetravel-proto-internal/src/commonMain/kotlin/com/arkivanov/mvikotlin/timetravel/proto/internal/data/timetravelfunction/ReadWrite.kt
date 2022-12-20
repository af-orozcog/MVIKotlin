package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.readTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.writeTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature.readTimeTravelParameterSignature
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature.writeTimeTravelParameterSignature
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataReader
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataWriter
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readList
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readString
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeCollection
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeString

internal fun DataWriter.writeTimeTravelFunction(timeTravelFunction: TimeTravelFunction) {
    writeString(timeTravelFunction.name)
    writeString(timeTravelFunction.type)
    writeCollection(timeTravelFunction.parameters) {
        writeTimeTravelParameterSignature(it)
    }
}

internal fun DataReader.readTimeTravelFunction(): TimeTravelFunction =
    TimeTravelFunction(
        name = readString()!!,
        type = readString()!!,
        parameters = readList { readTimeTravelParameterSignature() }!!
    )
