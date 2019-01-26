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

    private fun customRoom() = Room("custom", "123", emptyList(), "", "", "", "")

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
        verify(stateObserver).onChanged(BookingState(room = selectedRoom))
    }

    @Test
    fun `quick booking clicked`() {
        actionS.accept(BookingAction.QuickBookingSelected)
        verify(stateObserver).onChanged(BookingState(bookingType = QuickBooking(BookingDuration.MIN_15)))
    }

    @Test
    fun `precise booking clicked`() {
        actionS.accept(BookingAction.PreciseBookingSelected)
        verify(stateObserver).onChanged(BookingState(bookingType = PreciseBooking()))
    }

    @Test
    fun `title text changed` () {
        val newTitle = "title"
        actionS.accept(BookingAction.TitleChanged(newTitle))
        verify(stateObserver).onChanged(BookingState(title = newTitle))
    }

    @Test
    fun `booking duration changed`() {
        val newDuration = BookingDuration.HOUR_1
        actionS.accept(BookingAction.BookingDurationSelected(newDuration))
        verify(stateObserver).onChanged(BookingState(bookingType = QuickBooking(newDuration)))
    }


}

