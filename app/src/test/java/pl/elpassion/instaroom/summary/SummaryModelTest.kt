package pl.elpassion.instaroom.summary

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import io.kotlintest.IsolationMode
import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pl.elpassion.instaroom.booking.emptyRoom
import pl.elpassion.instaroom.booking.getTime
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.smokk
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.resume

@InternalCoroutinesApi
class SummaryModelTest : FreeSpec() {

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

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

        val actualRefresh = smokk<Unit>()

        val mainJob = CoroutineScope(Dispatchers.Unconfined).launch{
            runSummaryFlow(
                actionS,
                state,
                data,
                calendarSync,
                event,
                room,
                actualRefresh::invoke
            )
        }


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
            actualRefresh.invocations shouldBe 1
        }

        "calendar refresh job should update state" {
            actualRefresh.resume(Unit)
            calendarSync.test()
                .awaitValue()
                .assertValue(SummaryCalendarSync(true, false))
        }

        "edit event click should set state as viewing event" {
            actionS.accept(SummaryAction.EditEvent)
            stateObserver.awaitValue().assertValue(SummaryState.ViewingEvent("link"))
        }

        "select dismiss sets state as dismissing" {
            actionS.accept(SummaryAction.SelectDismiss)
            stateObserver.awaitValue().assertValue(SummaryState.Dismissing)
        }

        "dismissing finishes job" {
            actionS.accept(SummaryAction.Dismiss)
            println("actualRefresh = $actualRefresh")
            // we have to manually resume smokk cancelled function
            actualRefresh.resumeWith(Result.failure(CancellationException()))
            actualRefresh.completeInvocations shouldBe 1
            mainJob.isCompleted shouldBe true
        }

        "dismissing cancels children" {
            actionS.accept(SummaryAction.Dismiss)
            actualRefresh.cancelInvocations shouldBe 1
        }

    }

    private fun assertChildrenCanceled(job: Job) {
        job.children.forEach {
            if(!it.isCancelled) {
                fail("Assertion failed")
            }
        }
    }



}
