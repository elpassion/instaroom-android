//package pl.elpassion.instaroom.booking
//
//import androidx.lifecycle.MutableLiveData
//import com.google.api.client.util.DateTime
//import com.jakewharton.rxrelay2.PublishRelay
//import com.jraska.livedata.test
//import com.nhaarman.mockitokotlin2.mock
//import io.kotlintest.IsolationMode
//import io.kotlintest.specs.FreeSpec
//import kotlinx.coroutines.*
//import org.threeten.bp.Clock
//import org.threeten.bp.Clock.fixed
//import org.threeten.bp.Instant
//import org.threeten.bp.ZoneId
//import org.threeten.bp.ZonedDateTime
//import org.threeten.bp.format.DateTimeFormatter
//import pl.elpassion.instaroom.kalendar.BookingEvent
//import pl.elpassion.instaroom.kalendar.Room
//import pl.elpassion.instaroom.util.BookingDuration
//import pl.elpassion.instaroom.util.HourMinuteTime
//import pl.elpassion.instaroom.util.executeTasksInstantly
//import kotlin.coroutines.CoroutineContext
//
//fun ZonedDateTime.withHourMinute(hourMinuteTime: HourMinuteTime) =
//    this.withHour(hourMinuteTime.hour).withMinute(hourMinuteTime.minute)
//
//class BookingModelTest : FreeSpec(), CoroutineScope {
//    @ExperimentalCoroutinesApi
//    override val coroutineContext: CoroutineContext
//        get() = Dispatchers.Unconfined + job
//
//    override fun isolationMode(): IsolationMode? {
//        return IsolationMode.InstancePerLeaf
//    }
//
//    private val job = Job()
//    private val actionS = PublishRelay.create<BookingAction>()
//    private val stateD = MutableLiveData<BookingState>()
//    private val titleD = MutableLiveData<BookingTitle>()
//    private val typeD = MutableLiveData<BookingType>()
//    private val preciseTimeD = MutableLiveData<BookingPreciseTime>()
//    private val quickTimeD = MutableLiveData<BookingQuickTime>()
//    private val allDayD = MutableLiveData<BookingAllDay>()
//    private val constantsD = MutableLiveData<BookingConstants>()
//
//    init {
//        executeTasksInstantly()
//        var result: BookingEvent? = null
//        launch {
//            result = runBookingFlow(
//                actionS,
//                stateD,
//                titleD,
//                typeD,
//                preciseTimeD,
//                quickTimeD,
//                allDayD,
//                constantsD,
//                roomWithEvents,
//                defaultUserName,
//                fixed(Instant.now(), ZoneId.of("UTC")),
//                DateTimeFormatter.ofPattern("hh:mm a")
//                )
//        }
//
//        stateD.observeForever(mock())
//
//        "should initialize with expected values" {
//            stateD.test().awaitValue().assertValue(initialBookingState)
//        }
//
//        var testObserver = stateD.test()
//
//        "booking type selection when precise selected" - {
//            //quick booking is set when launching model
//
//            "do not set quick booking if it already is selected" {
//                actionS.accept(BookingAction.SelectQuickBooking)
//                testObserver.assertHistorySize(1)
//            }
//
//            "set precise booking if quick booking is selected" {
//                actionS.accept(BookingAction.SelectPreciseBooking)
//                testObserver.awaitValue().assertValue(preciseBookingState)
//            }
//
//        }
//
//        "booking type selection when quick selected" - {
//            actionS.accept(BookingAction.SelectPreciseBooking)
//            testObserver = testObserver.awaitValue()
//
//            "do not set precise booking if it already is selected" {
//                actionS.accept(BookingAction.SelectPreciseBooking)
//                testObserver.assertHistorySize(2)
//            }
//
//            "set quick booking after click if precise is selected" {
//                actionS.accept(BookingAction.SelectQuickBooking)
//                testObserver.awaitValue().assertValue(initialBookingState)
//            }
//        }
//
//        "set title" {
//            val newTitle = "title"
//            actionS.accept(BookingAction.ChangeTitle(newTitle))
//            testObserver.awaitValue().assertValue(initialBookingState.copy(title = newTitle))
//        }
//
//        "set quick booking duration" {
//            val newDuration = BookingDuration.HOUR_1
//            actionS.accept(BookingAction.SelectBookingDuration(newDuration))
//            testObserver.awaitValue()
//                .assertValue(initialBookingState.copy(bookingDuration = newDuration))
//        }
//
//        "set dialog dismissed sets booking stateD" {
//            actionS.accept(BookingAction.DismissTimePicker)
//            testObserver.awaitValue().assertValue(initialBookingState).assertHistorySize(2)
//        }
//
//        "to time button click shows timePickDialog" {
//            actionS.accept(BookingAction.SelectBookingEndTime)
//            testObserver.awaitValue().assertValue(
//                BookingState.PickingTime(
//                    false,
//                    preciseBookingState.toTime.toHourMinuteTime()
//                )
//            )
//        }
//
//        "from time button click shows timePickDialog" {
//            actionS.accept(BookingAction.SelectBookingStartTime)
//
//            testObserver.awaitValue().assertValue(
//                BookingState.PickingTime(
//                    true,
//                    preciseBookingState.fromTime.toHourMinuteTime()
//                )
//            )
//        }
//
//        "set precise booking time range" - {
//            actionS.accept(BookingAction.SelectPreciseBooking)
//            val hourMinuteTime = HourMinuteTime(14, 0)
//
//            "set start time" {
//                actionS.accept(BookingAction.ChangeBookingStartTime(hourMinuteTime))
//                actionS.accept(BookingAction.DismissTimePicker)
//
//                testObserver.awaitValue().assertValue(
//                    preciseBookingState.copy(
//                        fromTime = preciseBookingState.fromTime.withHourMinute(hourMinuteTime)
//                    )
//                )
//            }
//
//            "set end time" {
//                actionS.accept(BookingAction.ChangBookingEndTime(hourMinuteTime))
//                actionS.accept(BookingAction.DismissTimePicker)
//
//                testObserver.awaitValue().assertValue(
//                    preciseBookingState.copy(
//                        toTime = preciseBookingState.toTime.withHourMinute(hourMinuteTime)
//                    )
//                )
//            }
//        }
//
//        "cancel booking sets stateD to dismissing" {
//            actionS.accept(BookingAction.CancelClicked)
//            testObserver.awaitValue().assertValue(BookingState.Dismissing)
//        }
//
//        "confirm booking" - {
//
//            "dismisses booking dialog" {
//                actionS.accept(BookingAction.ConfirmClicked)
//                testObserver.awaitValue().assertValue(BookingState.Dismissing)
//            }
//
//            "as precise booking returns event" {
//                actionS.accept(BookingAction.SelectPreciseBooking)
//                actionS.accept(BookingAction.ConfirmClicked)
//                actionS.accept(BookingAction.Dismiss)
//
//                val event = basicExpectedBookingEvent.copy(
//                    startDate = DateTime(preciseBookingState.fromTime.toEpochMilliSecond()),
//                    endDate = DateTime(preciseBookingState.toTime.toEpochMilliSecond())
//                )
//                assert(result == event)
//            }
//
//            "as quick booking returns event" {
//                actionS.accept(BookingAction.ConfirmClicked)
//                actionS.accept(BookingAction.Dismiss)
//
//                val event =
//                    basicExpectedBookingEvent.copy(endDate = DateTime(now.toEpochMilliSecond() + BookingDuration.MIN_15.timeInMillis))
//                assert(result == event)
//            }
//        }
//
//        "dialog dismiss finishes with null" {
//            actionS.accept(BookingAction.Dismiss)
//            assert(result == null)
//        }
//
//        "set all day booking" {
//            testObserver.awaitValue()
//                .assertValue { (it as BookingState.Configuring.QuickBooking).allDayBooking == false }
//
//            actionS.accept(BookingAction.SwitchAllDayBooking(checked = true))
//            testObserver.awaitValue().assertValue(initialBookingState.copy(allDayBooking = true))
//        }
//
//    }
//
//}
