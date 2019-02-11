package pl.elpassion.instaroom.booking

import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.DateTime
import io.reactivex.Observable
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx2.awaitFirst
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.*

suspend fun runBookingFlow(
    actionS: Observable<BookingAction>,
    stateD: MutableLiveData<BookingState>,
    titleD: MutableLiveData<BookingTitle>,
    typeD: MutableLiveData<BookingType>,
    preciseTimeD: MutableLiveData<BookingPreciseTime>,
    quickTimeD: MutableLiveData<BookingQuickTime>,
    allDayD: MutableLiveData<BookingAllDay>,
    constantsD: MutableLiveData<BookingConstants>,
    room: Room,
    userName: String?,
    hourMinuteTimeFormatter: DateTimeFormatter
): BookingEvent? {

    val bookingValues = initializeBookingVariables(userName, room)

    bookingValues.run {

        var bookingEvent: BookingEvent? = null

        fun updateBookingTitle(enteredTitle: String) {
            title = enteredTitle
            titleD.value = BookingTitle(title)
        }

        fun updateBookingDuration(newBookingDuration: BookingDuration) {
            bookingDuration = newBookingDuration
            quickTimeD.set(BookingQuickTime(newBookingDuration.ordinal, limit))
        }

        fun updateBookingPreciseTime() {
            preciseTimeD.set(
                BookingPreciseTime
                    (preciseFromTime.format(hourMinuteTimeFormatter),
                    preciseToTime.format(hourMinuteTimeFormatter)
                )
            )
        }

        fun updateBookingStartTime(
            newHourMinuteTime: HourMinuteTime
        ) {
            preciseFromTime =
                preciseFromTime.withHour(newHourMinuteTime.hour)
                    .withMinute(newHourMinuteTime.minute)
            updateBookingPreciseTime()
        }

        fun updateBookingEndTime(
            newHourMinuteTime: HourMinuteTime
        ) {
            preciseToTime =
                preciseToTime.withHour(newHourMinuteTime.hour).withMinute(newHourMinuteTime.minute)
            updateBookingPreciseTime()
        }

        fun dismissBooking() {
            stateD.set(BookingState.Dismissing)
        }

        fun createBookingEvent(): BookingEvent {
            val startDate: DateTime
            val endDate: DateTime

            if (isPrecise) {
                startDate = DateTime(preciseFromTime.toEpochMilliSecond())
                endDate = DateTime(preciseToTime.toEpochMilliSecond())
            } else {
                startDate = DateTime(quickFromTime.toEpochMilliSecond())
                endDate =
                    DateTime(quickFromTime.toEpochMilliSecond() + bookingDuration.timeInMillis)
            }

            val finalTitle = if (title.isNotBlank()) title else hint

            return BookingEvent(room.calendarId, finalTitle, room.calendarId, startDate, endDate)
        }

        fun updateBookingType() {
            typeD.value = BookingType(!isPrecise)
        }

        fun disablePreciseBooking() {
            if (!quickAvailable || !isPrecise) return

            isPrecise = false
            updateBookingType()
        }

        fun enablePreciseBooking() {
            if (!preciseAvailable || isPrecise) return

            isPrecise = true
            updateBookingType()
        }

        fun updateAllDayBooking(checked: Boolean) {
            isAllDay = checked
            allDayD.value = BookingAllDay(isAllDay)
        }

        fun showTimePickerDialog(isFromTime: Boolean) {
            val hourMinuteTime =
                if (isFromTime) preciseFromTime.toHourMinuteTime() else preciseToTime.toHourMinuteTime()
            stateD.set(BookingState.PickingTime(isFromTime, hourMinuteTime))
        }

        fun getQuickBookingFromText(): String {
            return if (quickFromTime == now)
                "From now for"
            else {
                val time = quickFromTime.format(hourMinuteTimeFormatter)
                "From $time for"
            }
        }

        fun noBookingAvailable(): Boolean = !quickAvailable && !preciseAvailable

        if (noBookingAvailable()) {
            stateD.set(BookingState.Error("Booking in unavailable in this room now."))
            delay(1000)
            stateD.set(BookingState.Dismissing)
            return bookingEvent
        }

        stateD.set(BookingState.Default)
        titleD.set(BookingTitle(title))
        preciseTimeD.set(BookingPreciseTime(
            preciseFromTime.format(hourMinuteTimeFormatter),
            preciseToTime.format(hourMinuteTimeFormatter))
        )
        quickTimeD.set(BookingQuickTime(bookingDuration.ordinal, limit))
        constantsD.set(
            BookingConstants(
            room,
            hint,
            quickAvailable,
            preciseAvailable,
            getQuickBookingFromText())
        )
        allDayD.set(BookingAllDay(isAllDay))
        typeD.set(BookingType(!isPrecise))

        while (true) {
            val action = actionS.awaitFirst()

            when (action) {
                is BookingAction.SelectQuickBooking -> disablePreciseBooking()
                is BookingAction.SelectPreciseBooking -> enablePreciseBooking()
                is BookingAction.ChangeTitle -> updateBookingTitle(action.title)
                is BookingAction.SelectBookingDuration -> updateBookingDuration(action.bookingDuration)
                is BookingAction.SwitchAllDayBooking -> updateAllDayBooking(action.checked)

                is BookingAction.ChangeBookingStartTime -> updateBookingStartTime(action.newHourMinuteTime)
                is BookingAction.ChangBookingEndTime -> updateBookingEndTime(action.newHourMinuteTime)
                is BookingAction.SelectBookingStartTime -> showTimePickerDialog(true)
                is BookingAction.SelectBookingEndTime -> showTimePickerDialog(false)

                is BookingAction.Dismiss -> return bookingEvent

                is BookingAction.CancelClicked -> {
                    dismissBooking()
                }

                is BookingAction.ConfirmClicked -> {
                    bookingEvent = createBookingEvent()
                    dismissBooking()
                }
            }
        }
    }


}

class BookingUnavailableException : Exception()

sealed class BookingAction {

    object SelectQuickBooking : BookingAction()
    object SelectPreciseBooking : BookingAction()
    data class SwitchAllDayBooking(val checked: Boolean) : BookingAction()

    data class ChangeTitle(val title: String) : BookingAction()
    data class SelectBookingDuration(val bookingDuration: BookingDuration) : BookingAction()
    data class ChangeBookingStartTime(val newHourMinuteTime: HourMinuteTime) : BookingAction()
    data class ChangBookingEndTime(val newHourMinuteTime: HourMinuteTime) : BookingAction()

    object CancelClicked : BookingAction()
    object ConfirmClicked : BookingAction()

    object SelectBookingStartTime : BookingAction()
    object SelectBookingEndTime : BookingAction()

    object Dismiss : BookingAction()
}

data class BookingTitle(val text: String)
data class BookingPreciseTime(val fromText: String?, val toText: String?)
data class BookingAllDay(val enabled: Boolean)
data class BookingQuickTime(val durationPosition: Int, val limit: Int)
data class BookingConstants(
    val room: Room,
    val hint: String,
    val quickBookingAvailable: Boolean,
    val preciseBookingAvailable: Boolean,
    val quickBookingTimeText: String?
)
data class BookingType(val isQuick: Boolean)

sealed class BookingState {

    data class Error(val message: String) : BookingState()
    object Default : BookingState()

    data class PickingTime(
        val fromTime: Boolean,
        val hourMinuteTime: HourMinuteTime
    ) : BookingState()

    object Dismissing : BookingState()

}





