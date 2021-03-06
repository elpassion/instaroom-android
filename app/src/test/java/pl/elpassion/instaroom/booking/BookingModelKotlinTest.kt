package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.kotlintest.IsolationMode
import io.kotlintest.shouldNot
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.dashboard.DashboardAction
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.HourMinuteTime
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.elpassion.instaroom.util.toHourMinuteTime
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
        launchBookingModel(actionS, callDashboardAction, state)
        state.observeForever(stateObserver)

        actionS.accept(BookingAction.BookingRoomSelected(emptyRoom()))

        "should initialize with expected values" {
            state.test().awaitValue().assertValue(initialBookingState)
        }

        var testObserver = state.test()

        "booking type selection when precise selected" - {
            //quick booking is set when launching model

            "do not set quick booking if it already is selected" {
                actionS.accept(BookingAction.SelectQuickBooking)
                testObserver.assertHistorySize(1)
            }

            "set precise booking if quick booking is selected" {
                actionS.accept(BookingAction.SelectPreciseBooking)
                testObserver.awaitValue().assertValue(preciseBookingState)
            }

        }

        "booking type selection when quick selected" - {
            actionS.accept(BookingAction.SelectPreciseBooking)
            testObserver = testObserver.awaitValue()

            "do not set precise booking if it already is selected" {
                actionS.accept(BookingAction.SelectPreciseBooking)
                testObserver.assertHistorySize(2)
            }

            "set quick booking after click if precise is selected" {
                actionS.accept(BookingAction.SelectQuickBooking)
                testObserver.awaitValue().assertValue(initialBookingState)
            }
        }

        "set room" {
            val selectedRoom = Room("custom", "123", emptyList(), "", "", "", "")
            actionS.accept((BookingAction.BookingRoomSelected(selectedRoom)))
            testObserver.awaitValue().assertValue(initialBookingState.copy(room = selectedRoom))
        }

        "set title" {
            val newTitle = "title"
            actionS.accept(BookingAction.ChangeTitle(newTitle))
            testObserver.awaitValue().assertValue(initialBookingState.copy(title = newTitle))
        }

        "set quick booking duration" {
            val newDuration = BookingDuration.HOUR_1
            actionS.accept(BookingAction.SelectBookingDuration(newDuration))
            testObserver.awaitValue()
                .assertValue(initialBookingState.copy(bookingDuration = newDuration))
        }

        "set dialog dismissed sets booking state" {
            actionS.accept(BookingAction.DismissTimePicker)
            testObserver.awaitValue().assertValue(initialBookingState).assertHistorySize(2)
        }

        "to time button click shows timePickDialog" {
            actionS.accept(BookingAction.SelectBookingEndTime)
            testObserver.awaitValue().assertValue(
                ViewState.PickTime(
                    false,
                    preciseBookingState.toTime.toHourMinuteTime()
                )
            )
        }

        "from time button click shows timePickDialog" {
            actionS.accept(BookingAction.SelectBookingStartTime)

            testObserver.awaitValue().assertValue(
                ViewState.PickTime(
                    true,
                    preciseBookingState.fromTime.toHourMinuteTime()
                )
            )
        }

        "set precise booking time range" - {
            actionS.accept(BookingAction.SelectPreciseBooking)
            val hourMinuteTime = HourMinuteTime(14, 0)

            "set start time" {
                actionS.accept(BookingAction.ChangeBookingStartTime(hourMinuteTime))
                actionS.accept(BookingAction.DismissTimePicker)

                testObserver.awaitValue().assertValue(
                    preciseBookingState.copy(
                        fromTime = preciseBookingState.fromTime.withHourMinute(hourMinuteTime)
                    )
                )
            }

            "set end time" {
                actionS.accept(BookingAction.ChangBookingEndTime(hourMinuteTime))
                actionS.accept(BookingAction.DismissTimePicker)

                testObserver.awaitValue().assertValue(
                    preciseBookingState.copy(
                        toTime = preciseBookingState.toTime.withHourMinute(hourMinuteTime)
                    )
                )
            }
        }

        "cancel booking sets state to dismiss" {
            actionS.accept(BookingAction.CancelClicked)
            testObserver.awaitValue().assertValue(ViewState.BookingDismissing)
        }

        "confirm booking" - {
            actionS.accept(BookingAction.ConfirmClicked)

            "dismisses booking dialog" {
                testObserver.awaitValue().assertValue(ViewState.BookingDismissing)
            }

            "sends book action to dashboard" {
                verify(callDashboardAction).invoke(DashboardAction.BookRoom(any()))
            }
        }

        "set all day booking" {
            testObserver.awaitValue()
                .assertValue { (it as ViewState.BookingState.QuickBooking).allDayBooking == false }

            actionS.accept(BookingAction.SwitchAllDayBooking(checked = true))
            testObserver.awaitValue().assertValue(initialBookingState.copy(allDayBooking = true))
        }


    }

}
