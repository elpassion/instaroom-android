package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.clearTaskExecutorDelegate
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

//class BookingModelJUnitTest : CoroutineScope {

//    override val coroutineContext: CoroutineContext
//        get() = Dispatchers.Unconfined + job
//
//    private val job = Job()
//    private val actionS = PublishRelay.create<BookingAction>()
//    private val state = MutableLiveData<BookingState>()
//    private val stateObserver = mock<Observer<BookingState>>()
//
//    private fun customRoom() = Room("custom", "123", emptyList(), "", "", "", "")
//
//    // initial state sa wspolne, jak to ogarnac
//    private val initialBookingState =
//        BookingState.QuickBooking(BookingDuration.MIN_15, emptyRoom(), "", false)
//
//    private val preciseBookingState = BookingState.PreciseBooking(
//        ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES),
//        ZonedDateTime.now().truncatedTo(
//            ChronoUnit.MINUTES
//        ).plusHours(1),
//        emptyRoom(), "", false
//    )
//
//    @Before
//    fun setup() {
//        executeTasksInstantly()
//        launchBookingModel(actionS, mock(), state, mock())
//        state.observeForever(stateObserver)
//    }
//
//    @After
//    fun teardown() {
//        job.cancel()
//        clearTaskExecutorDelegate()
//    }
//
//    @Test
//    fun `set initial state`() {
//        verify(stateObserver).onChanged(initialBookingState)
//    }
//
//    @Test
//    fun `set room when booking room selected`() {
//        val selectedRoom = customRoom()
//        actionS.accept((BookingAction.BookingRoomSelected(selectedRoom)))
//        verify(stateObserver).onChanged(initialBookingState.copy(room = selectedRoom))
//    }
//
//    @Test
//    fun `do not set quick booking if it is current state`() {
//        actionS.accept(BookingAction.QuickBookingSelected)
//        verify(stateObserver, only()).onChanged(initialBookingState)
//    }
//
//    @Test
//    fun `set quick booking if it was precise`() {
//        actionS.accept(BookingAction.PreciseBookingSelected)
//        verify(stateObserver).onChanged(preciseBookingState)
//
//        actionS.accept(BookingAction.QuickBookingSelected)
//        verify(stateObserver, times(2)).onChanged(initialBookingState)
//    }
//
//    @Test
//    fun `precise booking clicked`() {
//        actionS.accept(BookingAction.PreciseBookingSelected)
//        verify(stateObserver).onChanged(preciseBookingState)
//    }
//
//    @Test
//    fun `title text changed`() {
//        val newTitle = "title"
//        actionS.accept(BookingAction.TitleChanged(newTitle))
//        verify(stateObserver).onChanged(argThat { title == newTitle })
//    }
//
//    @Test
//    fun `booking duration changed`() {
//        val newDuration = BookingDuration.HOUR_1
//        actionS.accept(BookingAction.BookingDurationSelected(newDuration))
//        verify(stateObserver).onChanged(initialBookingState.copy(bookingDuration = newDuration))
//    }
//
//    @Test
//    fun `booking time range changed`() {
//        actionS.accept(BookingAction.PreciseBookingSelected)
//        val newFromTime = ZonedDateTime.now().plusDays(1)
//        val newToTime = newFromTime.plusHours(1)
//        val newPreciseBooking = preciseBookingState.copy(fromTime = newFromTime, toTime = newToTime)
//        actionS.accept(BookingAction.BookingStartTimeChanged(newFromTime, newToTime))
//        verify(stateObserver).onChanged(newPreciseBooking)
//    }


//}

