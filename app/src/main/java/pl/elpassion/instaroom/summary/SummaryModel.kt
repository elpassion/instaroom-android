package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.set

suspend fun runSummaryFlow(
    actionS: Observable<SummaryAction>,
    state: MutableLiveData<SummaryState>,
    event: Event,
    room: Room
) {

    state.set(SummaryState.Initialized(event, room))

    while(true) {
        when (actionS.awaitFirst()) {
            is SummaryAction.SelectDismiss -> state.set(SummaryState.Dismissing)
            is SummaryAction.EditEvent -> state.set(SummaryState.ViewEvent(event.htmlLink!!))
            is SummaryAction.Dismiss -> return
        }
    }
}

sealed class SummaryAction {
    object SelectDismiss : SummaryAction()
    object EditEvent : SummaryAction()
    object Dismiss : SummaryAction()
}

sealed class SummaryState {
    data class Initialized(
        val event: Event,
        val room: Room
    ) : SummaryState()

    object Dismissing : SummaryState()
    data class ViewEvent(val link: String): SummaryState()
}
