package pl.elpassion.instaroom.booking

import android.annotation.SuppressLint
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
import pl.elpassion.instaroom.util.TimePickerFragmentDialog
import pl.elpassion.instaroom.util.selections
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class BookingFragment : BottomSheetDialogFragment() {

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
            setupEndTimeButton()
        ).subscribe(model.bookingActionS)
    }

    private fun setupStartTimeButton() = bookingTimeFrom
        .clicks()
        .map { BookingAction.BookingTimeFromClicked }

    private fun setupEndTimeButton() = bookingTimeTo
        .clicks()
        .map { BookingAction.BookingTimeToClicked }

    @SuppressLint("CheckResult")
    private fun showTimePickerDialog(
        fromTime: Boolean,
        hourMinuteTime: HourMinuteTime
    ) {
        val dialog = TimePickerFragmentDialog.withTime(hourMinuteTime)

        val timeChangesS = dialog.timeChanges()
            .map { newHourMinuteTime ->
                if(fromTime)
                    BookingAction.BookingStartTimeChanged(newHourMinuteTime)
                else
                    BookingAction.BookingEndTimeChanged(newHourMinuteTime)
            }

        val dismissesS = dialog.dismisses()
            .map { BookingAction.TimePickerDismissed }

        Observable.merge(timeChangesS, dismissesS).subscribe(model.bookingActionS)

        dialog.show(fragmentManager, TimePickerFragmentDialog.TAG)
    }


    private fun setupAllDaySwitch() = bookingAllDaySwitch
        .checkedChanges()
        .skipInitialValue()
        .map { checked -> BookingAction.AllDayBookingSwitched(checked) }

    private fun setupDurationSeekBar(): Observable<BookingAction.BookingDurationSelected> {
        bookingTimeBar.max = BookingDuration.values().size - 1

        return bookingTimeBar.changes()
            .skipInitialValue()
            .map { value -> BookingAction.BookingDurationSelected(BookingDuration.values()[value]) }
    }

    private fun setupTitleEditText() =
        bookingTitle.textChanges()
            .debounce(100, TimeUnit.MILLISECONDS)
            .filter { text -> text.isNotBlank() }
            .map { text -> BookingAction.TitleChanged(text.toString()) }


    private fun setupTabs() =
//      https://github.com/JakeWharton/RxBinding/issues/495
//      original selections method doesn't work until this issue will be resolved, so i copied that class and fixed it
        appointmentBookingTabs.selections().map { tab ->
            when (tab.position) {
                0 -> BookingAction.QuickBookingSelected
                1 -> BookingAction.PreciseBookingSelected
                else -> throw IllegalArgumentException()
            }
        }

    private fun updateView(viewState: ViewState?) {
        viewState ?: return

        when (viewState) {
            is ViewState.BookingState -> updateBookingState(viewState)
            is ViewState.PickTime -> showTimePickerDialog(
                viewState.fromTime,
                viewState.hourMinuteTime
            )
        }
    }

    private fun updateBookingState(bookingState: ViewState.BookingState) {
        appointmentBookingTitle.text = bookingState.room.name
        appointmentBookingTitle.setTextColor(Color.parseColor(bookingState.room.titleColor))
        appointmentBookingBackground.setBackgroundColor(Color.parseColor(bookingState.room.backgroundColor))

        when (bookingState) {
            is ViewState.BookingState.QuickBooking -> showQuickBookingGroup(bookingState)
            is ViewState.BookingState.PreciseBooking -> showPreciseBookingGroup(bookingState)
        }
    }


    private fun showQuickBookingGroup(bookingState: ViewState.BookingState.QuickBooking) {
        bookingQuickGroup.visibility = View.VISIBLE
        bookingPreciseGroup.visibility = View.GONE

    }

    private fun showPreciseBookingGroup(bookingState: ViewState.BookingState.PreciseBooking) {
        bookingQuickGroup.visibility = View.GONE
        bookingPreciseGroup.visibility = View.VISIBLE

        bookingTimeFrom.text = "${bookingState.fromTime.hour}:${bookingState.fromTime.minute}"
        bookingTimeTo.text = "${bookingState.toTime.hour}:${bookingState.toTime.minute}"
    }


}