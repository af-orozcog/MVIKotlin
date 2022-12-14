package com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetraveleventsupdate

import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelevent.TimeTravelEvent

sealed class TimeTravelEventsUpdate {

    data class All(val events: List<List<TimeTravelEvent>>) : TimeTravelEventsUpdate()
    data class NewList(val events: List<TimeTravelEvent>) : TimeTravelEventsUpdate()
    data class NewElement(val listIndex: Int, val events: List<TimeTravelEvent>) : TimeTravelEventsUpdate()
}
