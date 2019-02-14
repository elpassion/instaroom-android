package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import io.kotlintest.IsolationMode
import io.kotlintest.specs.FreeSpec
import kotlinx.coroutines.*
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.executeTasksInstantly
import kotlin.coroutines.CoroutineContext

class BookingModelTest : FreeSpec(), CoroutineScope {
    @ExperimentalCoroutinesApi
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined + job

    override fun isolationMode(): IsolationMode? {
        return IsolationMode.InstancePerLeaf
    }

    private val job = Job()
    private val actionS = PublishRelay.create<BookingAction>()
    private val stateD = MutableLiveData<BookingState>()
    private val titleD = MutableLiveData<BookingTitle>()
    private val typeD = MutableLiveData<BookingType>()
    private val preciseTimeD = MutableLiveData<BookingPreciseTime>()
    private val quickTimeD = MutableLiveData<BookingQuickTime>()
    private val allDayD = MutableLiveData<BookingAllDay>()
    private val constantsD = MutableLiveData<BookingConstants>()

    suspend fun initBookingFlow(bookingValues: BookingValues): BookingEvent? {
        return runBookingFlow(
            actionS,
            stateD,
            titleD,
            typeD,
            preciseTimeD,
            quickTimeD,
            allDayD,
            constantsD,
            DateTimeFormatter.ofPattern("hh:mm a"),
            bookingValues
        )
    }

    init {
        executeTasksInstantly()

        "should initialize with expected values" - {

            val room = emptyRoom

            val initialBookingValues = BookingValues (
                true,
                false,
                room,
                "initial title",
                "initial hint",
                getTime("12:00"),
                getTime("12:15"),
                BookingDuration.MIN_15,
                BookingDuration.MIN_30.ordinal,
                getTime("12:15"),
                getTime("12:45"),
                false
            )


            launch {
                initBookingFlow(initialBookingValues)
            }

            "with default state" {
                stateD.test().awaitValue().assertValue(BookingState.Default)
            }

            "with initial title" {
                titleD.test().awaitValue().assertValue(BookingTitle("initial title"))
            }

            "with initial constants" - {

                val testValue = constantsD.test().awaitValue().value()

                "room" {
                    assert(testValue.room == room)
                }

                "hint" {
                    assert(testValue.hint == "initial hint")
                }

                "quickAvailable" {
                    assert(testValue.quickBookingAvailable == true)
                }

                "quick booking text" {
                    assert(testValue.quickBookingTimeText == "From 12:15 PM for")
                }
            }

            "with initial quick booking values" - {
                val testValue = quickTimeD.test().awaitValue().value()

                "booking duration ordinal" {
                    assert(testValue.durationPosition == BookingDuration.MIN_15.ordinal)
                }

                "booking duration limit" {
                    assert(testValue.limit == BookingDuration.MIN_30.ordinal)
                }
            }

            "with initial precise booking values" - {
                val testValue = preciseTimeD.test().awaitValue().value()

                "from time" {
                    assert(testValue.fromText == "12:15 PM")
                }

                "to time" {
                    assert(testValue.toText == "12:45 PM")
                }

            }

            "with initial all day switch value" {
                allDayD.test().awaitValue().assertValue(BookingAllDay(false))
            }

            "with initial booking type value" {
                typeD.test().awaitValue().assertValue(BookingType(true))
            }
        }

        "actions work as expected" - {
            val room = emptyRoom.copy(calendarId = "calendarId")
            val initialBookingValues = BookingValues(
                true,
                false,
                room,
                "initial title",
                "initial hint",
                getTime("12:00"),
                getTime("12:15"),
                BookingDuration.MIN_15,
                BookingDuration.MIN_30.ordinal,
                getTime("12:15"),
                getTime("12:45"),
                false
            )

            var result: BookingEvent? = null
            launch {
                result = initBookingFlow(initialBookingValues)
            }

            val typeObserver = typeD.test()

            "booking type selection when precise selected" - {
                //quick booking is set when launching model

                "do not set quick booking if it already is selected" {
                    actionS.accept(BookingAction.SelectQuickBooking)
                    typeObserver.assertHistorySize(1)
                }

                "set precise booking if quick booking is selected" {
                    actionS.accept(BookingAction.SelectPreciseBooking)
                    typeObserver.awaitValue().assertValue(BookingType(false))
                }

            }

            "booking type selection when quick selected" - {
                actionS.accept(BookingAction.SelectPreciseBooking)
                val activeTypeObserver = typeObserver.awaitValue()

                "do not set precise booking if it already is selected" {
                    actionS.accept(BookingAction.SelectPreciseBooking)
                    activeTypeObserver.assertHistorySize(2)
                }

                "set quick booking after click if precise is selected" {
                    actionS.accept(BookingAction.SelectQuickBooking)
                    activeTypeObserver.awaitValue().assertValue(BookingType(true))
                }
            }

            "title change sets new title state" {
                actionS.accept(BookingAction.ChangeTitle("title"))
                titleD.test().awaitValue().assertValue(BookingTitle("title"))
            }

            "booking duration change" - {

                "changes if is in limit" {
                    actionS.accept(BookingAction.SelectBookingDuration(BookingDuration.MIN_30))
                    quickTimeD.test().awaitValue().assertValue(BookingQuickTime(BookingDuration.MIN_30.ordinal, initialBookingValues.limit))
                }

                "doesn't change if is over limit" {
                    actionS.accept(BookingAction.SelectBookingDuration(BookingDuration.HOUR_1))
                    quickTimeD.test().assertHistorySize(1)
                }
            }

            "switch all day booking sets expected allDay state" {
                actionS.accept(BookingAction.SwitchAllDayBooking(true))
                allDayD.test().awaitValue().assertValue(BookingAllDay(true))
            }

            "select precise booking start time sets PickingTime state with expected values" {
                actionS.accept(BookingAction.SelectBookingStartTime)
                stateD.test().awaitValue().assertValue(pickingTimeStateWithText(true, "12:15"))
            }

            "select precise booking end time sets PickingTime state with expected values" {
                actionS.accept(BookingAction.SelectBookingEndTime)
                stateD.test().awaitValue().assertValue(pickingTimeStateWithText(false, "12:45"))
            }

            "change booking start time update precise time as expected" {
                actionS.accept(BookingAction.ChangeBookingStartTime(hourMinuteWithTime("12:30")))
                val value = preciseTimeD.test().awaitValue().value()
                assert(value.fromText == "12:30 PM")
            }

            "change booking end time update precise time as expected" {
                actionS.accept(BookingAction.ChangBookingEndTime(hourMinuteWithTime("13:30")))
                val value = preciseTimeD.test().awaitValue().value()
                assert(value.toText == "01:30 PM")
            }

            "cancel click sets state to dismissing" {
                actionS.accept(BookingAction.CancelClicked)
                stateD.test().awaitValue().assertValue(BookingState.Dismissing)
            }

            "booking dialog dismiss returns null" {
                actionS.accept(BookingAction.Dismiss)
                assert(result == null)
            }

            "confirm booking" - {

                "sets state as dismissing" {
                    actionS.accept(BookingAction.ConfirmClicked)
                    stateD.test().awaitValue().assertValue(BookingState.Dismissing)
                }

                "as precise booking finishes flow with bookingEvent with expected initial values" {
                    actionS.accept(BookingAction.SelectPreciseBooking)
                    actionS.accept(BookingAction.ConfirmClicked)
                    actionS.accept(BookingAction.Dismiss)

                    assert(result == BookingEvent(
                        "calendarId",
                        "initial title",
                        "calendarId",
                        getDateTime("12:15"),
                        getDateTime("12:45")
                    ))
                }

                "as quick booking finishes flow with bookingEvent with expected initial values" {
                    actionS.accept(BookingAction.ConfirmClicked)
                    actionS.accept(BookingAction.Dismiss)

                    assert(result == BookingEvent(
                        "calendarId",
                        "initial title",
                        "calendarId",
                        getDateTime("12:15"),
                        getDateTime("12:30")
                    ))
                }
            }
        }

        "other cases of booking flow" - {

            val room = emptyRoom.copy(calendarId = "calendarId")
            val initialBookingValues = BookingValues(
                false,
                true,
                room,
                "",
                "title hint",
                getTime("12:00"),
                getTime("12:00"),
                BookingDuration.MIN_15,
                BookingDuration.MIN_45.ordinal,
                getTime("12:00"),
                getTime("12:45"),
                false
            )

            launch {
                initBookingFlow(initialBookingValues)
            }

            "quick booking time text as expected for booking time starting 'now'" {
                val testValues = constantsD.test().awaitValue().value()
                assert(testValues.quickBookingTimeText == "From now for")
            }

            "select quick booking time do not change type state" {
                actionS.accept(BookingAction.SelectQuickBooking)
                typeD.test().awaitValue().assertValue(BookingType(false)).assertHistorySize(1)
            }
        }

    }

}
