package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import io.kotlintest.IsolationMode
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.*
import pl.elpassion.instaroom.booking.emptyRoom
import pl.elpassion.instaroom.booking.getTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.mareklangiewicz.smokk.smokk
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class SummaryModelTest : FreeSpec(), CoroutineScope {
    @ExperimentalCoroutinesApi
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    private val job = Job()
    private val actionS = PublishRelay.create<SummaryAction>()
    private val state = MutableLiveData<SummaryState>()
    private val data = MutableLiveData<SummaryData>()
    private val calendarSync = MutableLiveData<SummaryCalendarSync>()

    private val event = Event(
        "",
        "link",
        "Test event",
        getTime("12:00").toString(),
        getTime("12:30").toString(),
        true
    )

    private val room = emptyRoom

    init {
        executeTasksInstantly()

        val refresh = smokk<Unit>()

        val flow = GlobalScope.launch {
            runSummaryFlow(
                actionS,
                state,
                data,
                calendarSync,
                event,
                room,
                refresh::invoke
            )
        }

        assert(flow.isActive)

        val stateObserver = state.test()
        "should initialize with expected values" - {

            "with default state" {
                stateObserver
                    .awaitValue()
                    .assertValue(SummaryState.Default)
            }

            "with data" {
                data.test().awaitValue().assertValue(SummaryData(event, room))
            }

            "with sync state" {
                calendarSync.test().awaitValue().assertValue(SummaryCalendarSync(false, true))
            }
        }

        "should run calendar refresh job" {
            assert(refresh.invocations == 1)
        }

        "calendar refresh job should update state" {
            refresh.resume(Unit)
            calendarSync.test()
                .awaitNextValue()
                .assertValue(SummaryCalendarSync(true, false))
        }

        "edit event click should set state as viewing event" {
            actionS.accept(SummaryAction.EditEvent)
            stateObserver.awaitNextValue().assertValue(SummaryState.ViewingEvent("link"))
        }

        "select dismiss sets state as dismissing" {
            actionS.accept(SummaryAction.SelectDismiss)
            stateObserver.awaitNextValue().assertValue(SummaryState.Dismissing)
        }

        "dismissing finishes job" {
            actionS.accept(SummaryAction.Dismiss)
            //TODO: What to do to not use delay?
            delay(1)
            assert(flow.isCompleted)
        }

        "dismissing cancels refresh async" {
            actionS.accept(SummaryAction.Dismiss)
            //TODO: HOW TO?
            assert(false)
        }

    }

}