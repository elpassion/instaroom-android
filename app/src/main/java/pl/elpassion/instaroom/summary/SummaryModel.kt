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

    state.set(SummaryState(event))
}

sealed class SummaryAction

data class SummaryState(val event: Event)