package pl.elpassion.instaroom.booking

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.changes
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import kotlinx.android.synthetic.main.booking_details_fragment.*
import kotlinx.android.synthetic.main.booking_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.HourMinuteTime
import pl.elpassion.instaroom.TimePickerDialogFragment
import pl.elpassion.instaroom.dashboard.getRoomBackground
import pl.elpassion.instaroom.util.selections
import java.util.concurrent.TimeUnit

class BookingFragment : RoundedBottomSheetDialogFragment() {

    private val model by sharedViewModel<AppViewModel>()

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.booking_fragment, container, false)

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.bookingState.observe(this, Observer(::updateView))
        
        Observable.mergeArray(
            setupTabs(),
            setupTitleEditText(),
            setupDurationSeekBar(),
            setupAllDaySwitch(),
            setupStartTimeButton(),
            setupEndTimeButton(),
            setupConfirmButton(),
            setupCancelButton()
        ).subscribe(model.bookingActionS)
    }

    private fun setupCancelButton() = appointmentBookingCancelButton
        .clicks()
        .map { BookingAction.CancelClicked }

    private fun setupConfirmButton() = appointmentBookingBookButton
        .clicks()
        .map { BookingAction.ConfirmClicked }

    private fun setupStartTimeButton() = bookingTimeFrom
        .clicks()
        .map { BookingAction.SelectBookingStartTime }

    private fun setupEndTimeButton() = bookingTimeTo
        .clicks()
        .map { BookingAction.SelectBookingEndTime }

    @SuppressLint("CheckResult")
    private fun showTimePickerDialog(
        fromTime: Boolean,
        hourMinuteTime: HourMinuteTime
    ) {
        val dialog = TimePickerDialogFragment.withTime(hourMinuteTime)

        val timeChangesS = dialog.timeChanges()
            .map { newHourMinuteTime ->
                if(fromTime)
                    BookingAction.ChangeBookingStartTime(newHourMinuteTime)
                else
                    BookingAction.ChangBookingEndTime(newHourMinuteTime)
            }

        val dismissesS = dialog.dismisses()
            .map { BookingAction.DismissTimePicker }

        Observable.merge(timeChangesS, dismissesS).subscribe(model.bookingActionS)

        dialog.show(fragmentManager, TimePickerDialogFragment.TAG)
    }


    private fun setupAllDaySwitch() = bookingAllDaySwitch
        .checkedChanges()
        .skipInitialValue()
        .map { checked -> BookingAction.SwitchAllDayBooking(checked) }

    private fun setupDurationSeekBar(): Observable<BookingAction.SelectBookingDuration> {
        bookingTimeBar.max = BookingDuration.values().size - 1

        return bookingTimeBar.changes()
            .skipInitialValue()
            .map { value -> BookingAction.SelectBookingDuration(BookingDuration.values()[value]) }
    }

    private fun setupTitleEditText() =
        bookingTitle.textChanges()
            .debounce(100, TimeUnit.MILLISECONDS)
            .filter { text -> text.isNotBlank() }
            .map { text -> BookingAction.ChangeTitle(text.toString()) }


    private fun setupTabs() =
//      https://github.com/JakeWharton/RxBinding/issues/495
//      original selections method doesn't work until this issue will be resolved, so i copied that class and fixed it
        appointmentBookingTabs.selections().map { tab ->
            when (tab.position) {
                0 -> BookingAction.SelectQuickBooking
                1 -> BookingAction.SelectPreciseBooking
                else -> throw IllegalArgumentException()
            }
        }

    private fun updateView(bookingState: BookingState?) {
        bookingState ?: return

        when (bookingState) {
            is BookingState.Configuring -> updateBookingState(bookingState)
            is BookingState.TimePicking -> showTimePickerDialog(
                bookingState.fromTime,
                bookingState.hourMinuteTime
            )
            BookingState.Dismissing -> dismiss()
        }
    }

    private fun updateBookingState(bookingState: BookingState.Configuring) {
        appointmentBookingTitle.text = bookingState.room.name
        appointmentBookingTitle.setTextColor(Color.parseColor(bookingState.room.titleColor))
        appointmentBookingBackground.setBackgroundResource(getRoomBackground(bookingState.room))

        when (bookingState) {
            is BookingState.Configuring.QuickBooking -> showQuickBookingGroup(bookingState)
            is BookingState.Configuring.PreciseBooking -> showPreciseBookingGroup(bookingState)
        }
    }


    private fun showQuickBookingGroup(bookingState: BookingState.Configuring.QuickBooking) {
        bookingQuickGroup.visibility = View.VISIBLE
        bookingPreciseGroup.visibility = View.GONE

    }

    private fun showPreciseBookingGroup(bookingState: BookingState.Configuring.PreciseBooking) {
        bookingQuickGroup.visibility = View.GONE
        bookingPreciseGroup.visibility = View.VISIBLE

        bookingTimeFrom.text = "${bookingState.fromTime.hour}:${bookingState.fromTime.minute}"
        bookingTimeTo.text = "${bookingState.toTime.hour}:${bookingState.toTime.minute}"
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        model.bookingActionS.accept(BookingAction.Dismiss)
    }
    companion object {
        const val TAG = "BOOKING_FRAGMENT_TAG"
    }
}