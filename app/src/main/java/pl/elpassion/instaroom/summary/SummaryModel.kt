package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.util.set

suspend fun runSummaryFlow(
    actionS: Observable<SummaryAction>,
    state: MutableLiveData<SummaryState>,
    event: Event
) {

    state.set(SummaryState.Initialized(event))
}

sealed class SummaryAction {
    object SelectDismiss : SummaryAction()
}

sealed class SummaryState {
    data class Initialized(val event: Event) : SummaryState()

    object Dismiss : SummaryState()
}
