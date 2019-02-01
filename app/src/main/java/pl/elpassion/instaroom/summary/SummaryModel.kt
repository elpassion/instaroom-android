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

    fun dismissDialog() {
        state.set(SummaryState.Dismiss)
    }

    state.set(SummaryState.Initialized(event))

    while(true) {
        val action = actionS.awaitFirst()
        println("action = $action")
        when (action) {
            is SummaryAction.SelectDismiss -> dismissDialog()
        }
    }
}

sealed class SummaryAction {
    object SelectDismiss : SummaryAction()
    object EditEvent : SummaryAction()
}

sealed class SummaryState {
    data class Initialized(val event: Event) : SummaryState()

    object Dismiss : SummaryState()
    object ViewEvent: SummaryState()
}
