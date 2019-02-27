package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
) = coroutineScope {

    stateD.set(SummaryState.Default)
    dataD.set(SummaryData(event, room))

    event.htmlLink?.let {
        launch {
            syncD.set(SummaryCalendarSync(false, true))
            refresh()
            syncD.set(SummaryCalendarSync(true, false))
        }

    } ?: syncD.set(SummaryCalendarSync(false, false))

    loop@ while (true) {
        when (actionS.awaitFirst()) {
            is SummaryAction.SelectDismiss -> stateD.set(SummaryState.Dismissing)
            is SummaryAction.EditEvent -> {
                event.htmlLink?.let{
                    stateD.set(SummaryState.ViewingEvent(it))
                }
            }
            is SummaryAction.Dismiss -> break@loop
        }
    }

    coroutineContext.cancelChildren()
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
    data class ViewingEvent(val link: String) : SummaryState()
}
