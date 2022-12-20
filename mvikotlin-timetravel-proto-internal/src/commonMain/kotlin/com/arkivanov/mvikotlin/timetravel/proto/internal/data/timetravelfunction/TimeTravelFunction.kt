package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelfunction
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.ProtoObject
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelparametersignature.TimeTravelParameterSignature

data class TimeTravelFunction(val name:String, val type:String, val parameters:List<TimeTravelParameterSignature>): ProtoObject
