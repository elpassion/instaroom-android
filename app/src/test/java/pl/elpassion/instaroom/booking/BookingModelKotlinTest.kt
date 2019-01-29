package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.IsolationMode
import io.kotlintest.shouldBe
import io.kotlintest.specs.AbstractFreeSpec
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Assert.*
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

class BookingModelKotlinTest : FreeSpec(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    private val job = Job()
    private val actionS = PublishRelay.create<BookingAction>()
    private val callDashboardAction = mock<(DashboardAction) -> Unit>()
    private val state = MutableLiveData<BookingState>()
    private val stateObserver = mock<Observer<BookingState>>()

    private val initialBookingState =
        BookingState.QuickBooking(BookingDuration.MIN_15, emptyRoom(), "", false)

    private val preciseBookingState = BookingState.PreciseBooking(
        ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES),
        ZonedDateTime.now().truncatedTo(
            ChronoUnit.MINUTES
        ).plusHours(1),
        emptyRoom(), "",
        false
    )

    init {
        executeTasksInstantly()
        launchBookingModel(actionS, callDashboardAction, state, mock())
        state.observeForever(stateObserver)

        actionS.accept(BookingAction.BookingRoomSelected(emptyRoom()))

        "should initialize with expected values" {
            verify(stateObserver).onChanged(initialBookingState)
        }

        "booking type selection when precise selected" - {
            //quick booking is set when launching model

            "do not set quick booking if it already is selected" {
                actionS.accept(BookingAction.QuickBookingSelected)
                verify(stateObserver, times(1)).onChanged(initialBookingState)
            }

            "set precise booking if quick booking is selected" {
                actionS.accept(BookingAction.PreciseBookingSelected)
                verify(stateObserver, times(1)).onChanged(preciseBookingState)
            }

        }

        "booking type selection when quick selected" - {
            actionS.accept(BookingAction.PreciseBookingSelected)

            "do not set precise booking if it already is selected" {
                actionS.accept(BookingAction.PreciseBookingSelected)
                verify(stateObserver, times(1)).onChanged(initialBookingState)
            }

            "set quick booking after click if precise is selected" {
                actionS.accept(BookingAction.QuickBookingSelected)
                verify(stateObserver, times(2)).onChanged(initialBookingState)
            }
        }

        "set room" {
            val selectedRoom = Room("custom", "123", emptyList(), "", "", "", "")
            actionS.accept((BookingAction.BookingRoomSelected(selectedRoom)))
            verify(stateObserver).onChanged(argThat { room == selectedRoom })
        }

        "set title" {
            val newTitle = "title"
            actionS.accept(BookingAction.TitleChanged(newTitle))
            verify(stateObserver).onChanged(argThat { title == newTitle })
        }

        "set quick booking duration" {
            val newDuration = BookingDuration.HOUR_1
            actionS.accept(BookingAction.BookingDurationSelected(newDuration))
            verify(stateObserver).onChanged(initialBookingState.copy(bookingDuration = newDuration))
        }

        "set precise booking time range" {
            actionS.accept(BookingAction.PreciseBookingSelected)
            val newFromTime = ZonedDateTime.now().plusDays(1)
            val newToTime = newFromTime.plusHours(1)
            val newPreciseBooking =
                preciseBookingState.copy(fromTime = newFromTime, toTime = newToTime)
            actionS.accept(BookingAction.BookingTimeRangeChanged(newFromTime, newToTime))
            verify(stateObserver).onChanged(newPreciseBooking)
        }

        "cancel booking call dashboard hide" {
            actionS.accept(BookingAction.CancelClicked)
            verify(callDashboardAction).invoke(DashboardAction.HideBookingDetails)
        }

        "set all day booking" {
            verify(stateObserver).onChanged(argThat { allDayBooking == false })

            actionS.accept(BookingAction.AllDayBookingClicked)
            verify(stateObserver).onChanged(argThat { allDayBooking == true })
        }


    }

}