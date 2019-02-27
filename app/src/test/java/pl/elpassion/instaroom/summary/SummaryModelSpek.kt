package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import io.kotlintest.shouldBe
import kotlinx.coroutines.*
import org.junit.Test
import org.junit.runner.RunWith
import pl.elpassion.instaroom.booking.emptyRoom
import pl.elpassion.instaroom.booking.getTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.smokk
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@RunWith(USpekRunner::class)
class SummaryModelSpek {

    init {
        executeTasksInstantly()
    }

    @InternalCoroutinesApi
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
                "start refreshing" o { refresh.isActive eq true }

                "On refresh success" o {
                    refresh.resume(Unit)

                    "set synced state" o { syncStateObs.assertValue(SummaryCalendarSync(true, false)) }
                    "refresh is completed" o { refresh.isCompleted eq true}

                    "On loop with refresh finished" o {

                        "On select dismiss" o {
                            actionS.accept(SummaryAction.SelectDismiss)

                            "set state to dismissing" o { stateObs.assertValue(SummaryState.Dismissing) }
                        }

                        "On edit event" o {
                            actionS.accept(SummaryAction.EditEvent)

                            "set state to viewingEvent with correct link" o {
                                stateObs.assertValue(SummaryState.ViewingEvent("link")) }
                        }

                        "On dismiss" o {
                            actionS.accept(SummaryAction.Dismiss)

                            "refresh is not cancelled" o { refresh.isCancelled eq false }
                            "refresh is completed" o { refresh.isCompleted eq true }
                            "flow is completed" o { mainJob.isCompleted eq true }
                        }
                    }
                }

                "On user actions while refreshing" o {

                    "On select dismiss" o {
                        actionS.accept(SummaryAction.SelectDismiss)

                        "set state to dismissing" o { stateObs.assertValue(SummaryState.Dismissing) }
                    }

                    "On edit event" o {
                        actionS.accept(SummaryAction.EditEvent)

                        "set state to viewingEvent with correct link" o {
                            stateObs.assertValue(SummaryState.ViewingEvent("link")) }
                    }

                    "On dismiss" o {
                        actionS.accept(SummaryAction.Dismiss)

                        "refresh is cancelled" o { refresh.isCancelled eq true}

                        "On refresh resumed with CancellationException" o {
                            refresh.resumeWithException(CancellationException())

                            "refresh is completed" o { refresh.isCompleted}
                            "flow is completed" o { refresh.isCompleted eq true }
                        }
                    }
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
                "not refreshing" o { refresh.isActive shouldBe false }

                "On user actions without refreshing" o {

                    "On select dismiss" o {
                        actionS.accept(SummaryAction.SelectDismiss)

                        "set state to dismissing" o { stateObs.assertValue(SummaryState.Dismissing) }
                    }

                    "On edit event" o {
                        actionS.accept(SummaryAction.EditEvent)

                        "do not set state to viewingEvent" o {
                            stateObs.assertNever{ state -> state == SummaryState.ViewingEvent("link")}
                        }
                    }

                    "On dismiss" o {
                        actionS.accept(SummaryAction.Dismiss)

                        "refresh is not cancelled" o { refresh.isCancelled eq false}
                        "flow is completed" o { mainJob.isCompleted eq true }
                    }
                }
            }
        }
    }

}
