package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunctionlist

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.readTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.writeTimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.readTimeTravelFunction
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.writeTimeTravelFunction
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

internal fun DataWriter.writeTimeTravelFunctionList(timeTravelFunction: TimeTravelFunctionList) {
    writeCollection(timeTravelFunction.functions) {
        writeTimeTravelFunction(it)
    }
}

internal fun DataReader.readTimeTravelFunctionList(): TimeTravelFunctionList =
    TimeTravelFunctionList(
        functions = readList { readTimeTravelFunction() }!!
    )
