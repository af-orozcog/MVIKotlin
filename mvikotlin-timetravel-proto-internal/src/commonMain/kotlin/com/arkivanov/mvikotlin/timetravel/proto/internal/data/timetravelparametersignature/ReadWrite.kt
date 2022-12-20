package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.readTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataReader
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.DataWriter
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readList
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.readString
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeEnum
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeLong
import com.arkivanov.mvikotlin.timetravel.proto.internal.io.writeString

internal fun DataWriter.writeTimeTravelParameterSignature(timeTravelParameterSignature: TimeTravelParameterSignature) {
    writeString(timeTravelParameterSignature.name)
    writeString(timeTravelParameterSignature.type)
}

internal fun DataReader.readTimeTravelParameterSignature(): TimeTravelParameterSignature =
    TimeTravelParameterSignature(
        name = readString()!!,
        type = readString()!!,
    )
