package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.set

suspend fun runSummaryFlow(
    actionS: Observable<SummaryAction>,
    stateD: MutableLiveData<SummaryState>,
    dataD: MutableLiveData<SummaryData>,
    syncD: MutableLiveData<SummaryCalendarSync>,
    event: Event,
    room: Room,
    refresh: suspend () -> Unit
) {

    stateD.set(SummaryState.Default)
    dataD.set(SummaryData(event, room))

    var async: Deferred<Unit>? = null
    event.htmlLink?.let {
        async = GlobalScope.async {
            syncD.set(SummaryCalendarSync(false, true))
            refresh()
            syncD.set(SummaryCalendarSync(true, false))
        }
    } ?: syncD.set(SummaryCalendarSync(false, false))


    loop@ while(true) {
        when (actionS.awaitFirst()) {
            is SummaryAction.SelectDismiss -> stateD.set(SummaryState.Dismissing)
            is SummaryAction.EditEvent -> stateD.set(SummaryState.ViewingEvent(event.htmlLink!!))
            is SummaryAction.Dismiss -> break@loop
        }
    }

    async?.cancel()
}

sealed class SummaryAction {
    object SelectDismiss : SummaryAction()
    object EditEvent : SummaryAction()
    object Dismiss : SummaryAction()
}

data class SummaryData(
    val event: Event,
    val room: Room
)

data class SummaryCalendarSync(
    val isSynced: Boolean,
    val isSyncing: Boolean
)

sealed class SummaryState {
    object Default : SummaryState()
    object Dismissing : SummaryState()
    data class ViewingEvent(val link: String): SummaryState()
}

suspend fun refreshCalendarForSummary(
    syncD: MutableLiveData<SummaryCalendarSync>,
    calendarRefresher: CalendarRefresher
) {
    syncD.set(SummaryCalendarSync(false, true))
    calendarRefresher.refresh()
    syncD.set(SummaryCalendarSync(true, false))
}
