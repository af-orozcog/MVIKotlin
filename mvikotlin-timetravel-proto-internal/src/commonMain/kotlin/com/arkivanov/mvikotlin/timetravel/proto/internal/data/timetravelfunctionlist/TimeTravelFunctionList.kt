package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunctionlist
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.ProtoObject
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction.TimeTravelFunction

data class TimeTravelFunctionList(val functions:List<TimeTravelFunction>): ProtoObject
