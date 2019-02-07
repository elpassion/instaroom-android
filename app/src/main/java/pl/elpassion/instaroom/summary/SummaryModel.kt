package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.CalendarInitializer
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.set
import kotlinx.coroutines.*
import kotlin.system.*

suspend fun runSummaryFlow(
    actionS: Observable<SummaryAction>,
    state: MutableLiveData<SummaryState>,
    event: Event,
    room: Room,
    calendarInitializer: CalendarInitializer
) {

    var isSynced = false

    state.set(SummaryState.Initialized(event, room, isSynced))

    val async = GlobalScope.async {
        println("summary async")
        calendarInitializer.refreshCalendar()
        isSynced = true
        println("summary refreshed")
        state.set(SummaryState.Initialized(event, room, isSynced))
    }


    loop@ while(true) {
        println("summary loop")
        when (actionS.awaitFirst()) {
            is SummaryAction.SelectDismiss -> state.set(SummaryState.Dismissing)
            is SummaryAction.EditEvent -> state.set(SummaryState.ViewEvent(event.htmlLink!!))
            is SummaryAction.Dismiss -> break@loop
        }
    }

    async.cancel()
}

sealed class SummaryAction {
    object SelectDismiss : SummaryAction()
    object EditEvent : SummaryAction()
    object Dismiss : SummaryAction()
}

sealed class SummaryState {
    data class Initialized(
        val event: Event,
        val room: Room,
        val isSynced: Boolean
    ) : SummaryState()

    object Dismissing : SummaryState()
    data class ViewEvent(val link: String): SummaryState()
}
