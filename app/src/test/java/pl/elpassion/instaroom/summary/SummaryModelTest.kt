package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.mock
import io.kotlintest.IsolationMode
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.*
import pl.elpassion.instaroom.booking.emptyRoom
import pl.elpassion.instaroom.booking.getTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.mareklangiewicz.smokk.smokk
import java.time.ZonedDateTime
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

        val d = GlobalScope.launch {
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

        assert(d.isActive)

        "should initialize with expected values" - {

            "with default state" {
                state
                    .test()
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

//        "dismiss click should dismiss dialog" {
//            actionS.accept(SummaryAction.SelectDismiss)
//            testObserver.awaitValue().assertValue(SummaryState.Dismissing)
//        }
//
//        "edit in calendar click shows event" {
//            actionS.accept(SummaryAction.EditEvent)
//            testObserver.awaitValue().assertValue(SummaryState.ViewEvent(event.htmlLink!!))
//        }
//
//        "dismissing dialog ends task" {
//            assert(!taskFinished)
//            actionS.accept(SummaryAction.Dismiss)
//            assert(taskFinished)
//        }

    }

}