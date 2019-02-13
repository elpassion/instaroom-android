//package pl.elpassion.instaroom.summary
//
//import androidx.lifecycle.MutableLiveData
//import com.jakewharton.rxrelay2.PublishRelay
//import com.jraska.livedata.test
//import com.nhaarman.mockitokotlin2.mock
//import io.kotlintest.IsolationMode
//import io.kotlintest.specs.FreeSpec
//import kotlinx.coroutines.*
//import pl.elpassion.instaroom.kalendar.Event
//import pl.elpassion.instaroom.util.executeTasksInstantly
//import java.time.ZonedDateTime
//import kotlin.coroutines.CoroutineContext
//
//class SummaryModelTest : FreeSpec(), CoroutineScope {
//    @ExperimentalCoroutinesApi
//    override val coroutineContext: CoroutineContext
//        get() = Dispatchers.Unconfined + job
//
//    override fun isolationMode(): IsolationMode? {
//        return IsolationMode.InstancePerLeaf
//    }
//
//    private val job = Job()
//    private val actionS = PublishRelay.create<SummaryAction>()
//    private val state = MutableLiveData<SummaryState>()
//
//    private val event = Event(
//        "",
//        "link",
//        "Test event",
//        ZonedDateTime.now().toString(),
//        ZonedDateTime.now().plusHours(1).toString()
//    )
//
//    private val initialState = SummaryState.Initialized(event, room)
//
//    init {
//        executeTasksInstantly()
//        var taskFinished = false
//        launch {
//            runSummaryFlow(actionS, state, event)
//            taskFinished = true
//        }
//        state.observeForever(mock())
//
//        val testObserver = state.test().awaitValue()
//
//        "should initialize with expected state (event)" {
//            testObserver.awaitValue().assertValue(initialState)
//        }
//
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
//
//    }
//
//}