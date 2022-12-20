package com.arkivanov.mvikotlin.timetravel.server

import com.arkivanov.mvikotlin.timetravel.TimeTravelEvent
import com.arkivanov.mvikotlin.timetravel.TimeTravelState
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetraveleventsupdate.TimeTravelEventsUpdate
import com.arkivanov.mvikotlin.timetravel.proto.internal.data.timetravelstateupdate.TimeTravelStateUpdate

internal class StateDiff {

    private var previousState: TimeTravelState? = null

    operator fun invoke(state: TimeTravelState): TimeTravelStateUpdate {
        val previousState = previousState

        val update =
            TimeTravelStateUpdate(
                eventsUpdate = diffEvents(new = state.events, previous = previousState?.events),
                selectedEventIndex = state.selectedEventIndex,
                selectedListEventIndex = state.selectedListEventIndex,
                mode = state.mode.toProto()
            )

        this.previousState = state

        return update
    }

    private fun diffEvents(new: List<List<TimeTravelEvent>>, previous: List<List<TimeTravelEvent>>?): TimeTravelEventsUpdate {
        if(previous == null){
            return TimeTravelEventsUpdate.All(new.toProto())
        }
        else if(new.size > previous.size){
            return TimeTravelEventsUpdate.NewList(new[new.size-1].toProto())
        }
        else if(new.size == previous.size && new[new.size-1].size > previous[new.size-1].size){
            return TimeTravelEventsUpdate.NewElement(new.size-1,new[new.size-1].subList(previous[new.size-1].size,new[new.size-1].size).toProto())
        }
        else {
            return TimeTravelEventsUpdate.All(new.toProto())
        }
    }
}
