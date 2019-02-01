package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.rx2.awaitFirst
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.util.set

suspend fun runSummaryFlow(
    actionS: Observable<SummaryAction>,
    state: MutableLiveData<SummaryState>,
    event: Event
) {

    state.set(SummaryState.Initialized(event))

    while(true) {
        when (actionS.awaitFirst()) {
            is SummaryAction.SelectDismiss -> state.set(SummaryState.Dismiss)
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
    data class Initialized(val event: Event) : SummaryState()

    object Dismiss : SummaryState()
    data class ViewEvent(val link: String): SummaryState()
}
