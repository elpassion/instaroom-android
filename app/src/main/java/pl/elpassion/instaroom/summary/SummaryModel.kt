package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import pl.elpassion.instaroom.kalendar.Event

suspend fun runSummaryFlow(
    actionS: Observable<SummaryAction>,
    state: MutableLiveData<SummaryState>,
    event: Event
) {

}

sealed class SummaryAction

object SummaryState