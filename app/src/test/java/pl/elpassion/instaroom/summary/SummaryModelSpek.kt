package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import pl.elpassion.instaroom.booking.emptyRoom
import pl.elpassion.instaroom.booking.getTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.mareklangiewicz.smokk.smokk
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek
import kotlin.coroutines.resume


@RunWith(USpekRunner::class)
class SummaryModelSpek {

    init {
        executeTasksInstantly()
    }

    @ExperimentalCoroutinesApi
    @Test
    fun summaryModelTests() {

        uspek {

            val actionS = PublishRelay.create<SummaryAction>()
            val stateD = MutableLiveData<SummaryState>()
            val dataD = MutableLiveData<SummaryData>()
            val syncStateD = MutableLiveData<SummaryCalendarSync>()

            val stateObs = stateD.test()
            val dataObs = dataD.test()
            val syncStateObs = syncStateD.test()

            val event = Event(
                "",
                "link",
                "Test event",
                getTime("12:00").toString(),
                getTime("12:30").toString(),
                true
            )
            val room = emptyRoom
            val refresh = smokk<Unit>()

            "On summaryModelFlow" o {
                val mainJob = GlobalScope.launch(Dispatchers.Unconfined) {
                    runSummaryFlow(
                    actionS,
                        stateD,
                        dataD,
                        syncStateD,
                        event,
                        room,
                        refresh::invoke
                    )
                }

                "set correct state" o { stateObs.assertValue(SummaryState.Default) }
                "set correct data" o { dataObs.assertValue(SummaryData(event, room)) }
                "set syncing state" o { syncStateObs.assertValue(SummaryCalendarSync(false, true)) }
                "start refreshing" o { refresh.invocations eq 1 }

                "On refresh success" o {
                    refresh.resume(Unit)

                    "set synced state" o { syncStateObs.assertValue(SummaryCalendarSync(true, false)) }

                    "On loop with refresh finished" o { /* TODO maybe: test different loop scenarios */ }
                }

                "On user actions while refreshing" o {
                    // TODO: later
                }
            }

            "On summaryModelFlow without event htmlLink" o {
                val mainJob = GlobalScope.launch(Dispatchers.Unconfined) {
                    runSummaryFlow(
                        actionS,
                        stateD,
                        dataD,
                        syncStateD,
                        event.copy(htmlLink = null),
                        room,
                        refresh::invoke
                    )
                }

                "set not syncing state" o { syncStateObs.assertValue(SummaryCalendarSync(false, false)) }
                "not refreshing" o { refresh.invocations eq 0 }
            }


        }
    }

}
