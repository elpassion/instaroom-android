package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Test
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.clearTaskExecutorDelegate
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

class BookingModelTest : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    private val job = Job()
    private val actionS = PublishRelay.create<BookingAction>()
    private val state = MutableLiveData<BookingState>()
    private val stateObserver = mock<Observer<BookingState>>()

    private fun emptyRoom() = Room("", "", emptyList(), "", "", "", "")
    private fun customRoom() = Room("custom", "123", emptyList(), "", "", "", "")

    private val defaultRoom = emptyRoom()
    private val defaultType = BookingType.QUICK
    private val defaultTitle = ""

    @Before
    fun setup() {
        executeTasksInstantly()
        launchBookingModel(actionS, mock(), state, mock())
        state.observeForever(stateObserver)
    }

    @After
    fun teardown() {
        job.cancel()
        clearTaskExecutorDelegate()
    }

    @Test
    fun `show room on book clicked`() {
        val selectedRoom = customRoom()
        actionS.accept((BookingAction.BookingInitialized(selectedRoom)))
        verify(stateObserver).onChanged(BookingState(selectedRoom, defaultType, defaultTitle))
    }

    @Test
    fun `quick booking clicked`() {
        actionS.accept(BookingAction.QuickBookingSelected)
        verify(stateObserver).onChanged(BookingState(defaultRoom, BookingType.QUICK, defaultTitle))
    }

    @Test
    fun `precise booking clicked`() {
        actionS.accept(BookingAction.PreciseBookingSelected)
        verify(stateObserver).onChanged(BookingState(defaultRoom, BookingType.PRECISE, defaultTitle))
    }

    @Test
    fun `title text changed` () {
        val newTitle = "title"
        actionS.accept(BookingAction.TitleChanged(newTitle))
        verify(stateObserver).onChanged(BookingState(defaultRoom, defaultType, newTitle))
    }

}

