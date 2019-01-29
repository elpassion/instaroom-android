package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxrelay2.PublishRelay
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.IsolationMode
import io.kotlintest.matchers.date.before
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

fun ZonedDateTime.withHourMinute(hourMinuteTime: HourMinuteTime) =
    this.withHour(hourMinuteTime.hour).withMinute(hourMinuteTime.minute)

class BookingModelKotlinTest : FreeSpec(), CoroutineScope {
    @ExperimentalCoroutinesApi
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    private val job = Job()
    private val actionS = PublishRelay.create<BookingAction>()
    private val callDashboardAction = mock<(DashboardAction) -> Unit>()
    private val state = MutableLiveData<ViewState>()
    private val stateObserver = mock<Observer<ViewState>>()

    private val initialBookingState =
        ViewState.BookingState.QuickBooking(BookingDuration.MIN_15, emptyRoom(), "", false)

    private val preciseBookingState = ViewState.BookingState.PreciseBooking(
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
                reset(stateObserver)
                actionS.accept(BookingAction.QuickBookingSelected)
                verify(stateObserver, never()).onChanged(initialBookingState)
            }

            "set precise booking if quick booking is selected" {
                actionS.accept(BookingAction.PreciseBookingSelected)
                verify(stateObserver).onChanged(preciseBookingState)
            }

        }

        "booking type selection when quick selected" - {
            actionS.accept(BookingAction.PreciseBookingSelected)

            "do not set precise booking if it already is selected" {
                reset(stateObserver)
                actionS.accept(BookingAction.PreciseBookingSelected)
                verify(stateObserver, never()).onChanged(initialBookingState)
            }

            "set quick booking after click if precise is selected" {
                reset(stateObserver)
                actionS.accept(BookingAction.QuickBookingSelected)
                verify(stateObserver).onChanged(initialBookingState)
            }
        }

        "set room" {
            val selectedRoom = Room("custom", "123", emptyList(), "", "", "", "")
            actionS.accept((BookingAction.BookingRoomSelected(selectedRoom)))
            verify(stateObserver).onChanged(initialBookingState.copy(room = selectedRoom))
        }

        "set title" {
            val newTitle = "title"
            actionS.accept(BookingAction.TitleChanged(newTitle))
            verify(stateObserver).onChanged(initialBookingState.copy(title = newTitle))
        }

        "set quick booking duration" {
            val newDuration = BookingDuration.HOUR_1
            actionS.accept(BookingAction.BookingDurationSelected(newDuration))
            verify(stateObserver).onChanged(initialBookingState.copy(bookingDuration = newDuration))
        }

        "set dialog dismissed sets booking state" {
            reset(stateObserver)
            actionS.accept(BookingAction.TimePickerDismissed)
            verify(stateObserver).onChanged(initialBookingState)
        }

        "to time button click shows timePickDialog" {
            actionS.accept(BookingAction.BookingTimeToClicked)
            verify(stateObserver).onChanged(
                ViewState.PickTime(
                    false,
                    preciseBookingState.toTime.hourMinuteTime
                )
            )
        }

        "from time button click shows timePickDialog" {
            actionS.accept(BookingAction.BookingTimeFromClicked)

            verify(stateObserver).onChanged(
                ViewState.PickTime(
                    true,
                    preciseBookingState.fromTime.hourMinuteTime
                )
            )
        }

        "set precise booking time range" - {
            actionS.accept(BookingAction.PreciseBookingSelected)
            val hourMinuteTime = HourMinuteTime(14, 0)

            "set start time" {
                actionS.accept(BookingAction.BookingStartTimeChanged(hourMinuteTime))
                actionS.accept(BookingAction.TimePickerDismissed)

                verify(stateObserver).onChanged(
                    preciseBookingState.copy(
                        fromTime = preciseBookingState.fromTime.withHourMinute(hourMinuteTime)
                    )
                )
            }

            "set end time" {
                actionS.accept(BookingAction.BookingEndTimeChanged(hourMinuteTime))
                actionS.accept(BookingAction.TimePickerDismissed)

                verify(stateObserver).onChanged(
                    preciseBookingState.copy(
                        toTime = preciseBookingState.toTime.withHourMinute(hourMinuteTime)
                    )
                )
            }
        }

        "cancel booking call dashboard hide" {
            actionS.accept(BookingAction.CancelClicked)
            verify(callDashboardAction).invoke(DashboardAction.HideBookingDetails)
        }

        "set all day booking" {
            verify(stateObserver).onChanged(initialBookingState)

            actionS.accept(BookingAction.AllDayBookingSwitched(checked = true))
            verify(stateObserver).onChanged(initialBookingState.copy(allDayBooking = true))
        }


    }

}