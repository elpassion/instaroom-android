package pl.elpassion.instaroom.booking

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.view.clicks
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
import pl.elpassion.instaroom.util.TabLayoutUtils
import pl.elpassion.instaroom.util.selections
import java.util.concurrent.TimeUnit

class BookingFragment : RoundedBottomSheetDialogFragment() {

    private val model by sharedViewModel<AppViewModel>()

    private val unavailableTextStyle by lazy { R.style.TextLight_Unavailable_Small }
    private val activeTextStyle by lazy { R.style.TextBold_Primary_Small }
    private val inactiveTextStyle by lazy { R.style.TextBold_Grey_Small }

    private lateinit var bookingDurationTextViews: List<TextView>

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.booking_fragment, container, false)

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingDurationTextViews = listOf(
            bookingTimeOptionFirst, bookingTimeOptionSecond, bookingTimeOptionThird,
            bookingTimeOptionFourth, bookingTimeOptionFifth
        )

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
                if (fromTime)
                    BookingAction.ChangeBookingStartTime(newHourMinuteTime)
                else
                    BookingAction.ChangBookingEndTime(newHourMinuteTime)
            }

        timeChangesS.subscribe(model.bookingActionS)

        dialog.show(fragmentManager, TimePickerDialogFragment.TAG)
    }


    private fun setupAllDaySwitch() = bookingAllDaySwitch
        .checkedChanges()
        .skipInitialValue()
        .map { checked -> BookingAction.SwitchAllDayBooking(checked) }

    private fun setupDurationSeekBar(): Observable<BookingAction.SelectBookingDuration> {
        bookingTimeBar.max = BookingDuration.values().size - 1

        return bookingTimeBar.limitedChanges()
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
            is BookingState.Initializing ->
                initializeBookingView(bookingState)
            is BookingState.PickingTime ->
                showTimePickerDialog(
                    bookingState.fromTime,
                    bookingState.hourMinuteTime
                )
            BookingState.Dismissing -> dismiss()
            is BookingState.ConfiguringPreciseBooking ->
                configurePreciseBooking(bookingState)
            is BookingState.ChangingType.QuickBooking ->
                showBookingGroup(true)
            is BookingState.ChangingType.PreciseBooking -> showBookingGroup(false)
            is BookingState.ConfiguringQuickBooking -> selectBookingDurationText(
                bookingState.durationSelectedPos,
                bookingState.limit
            )
        }
    }

    private fun selectBookingDurationText(selectedPos: Int, limit: Int) {
        bookingDurationTextViews.forEachIndexed { index, textView ->
            val enabled = index <= limit
            val textStyle = if (enabled) inactiveTextStyle else unavailableTextStyle

            textView.setTextAppearance(textStyle)
        }

        bookingDurationTextViews[selectedPos].setTextAppearance(activeTextStyle)
    }

    private fun configurePreciseBooking(bookingState: BookingState.ConfiguringPreciseBooking) {
        bookingTimeFrom.setTime(bookingState.fromTime)
        bookingTimeTo.setTime(bookingState.toTime)
    }

    private fun initializeBookingView(bookingState: BookingState.Initializing) {
        enableTab(0, bookingState.quickAvailable)
        enableTab(1, bookingState.preciseAvailable)

        appointmentBookingTitle.text = bookingState.room.name
        appointmentBookingTitle.setTextColor(Color.parseColor(bookingState.room.titleColor))
        appointmentBookingBackground.setBackgroundResource(getRoomBackground(bookingState.room))

        bookingTitle.setText(bookingState.title)
        bookingAllDaySwitch.isChecked = bookingState.allDayBooking

        bookingFromNowFor.text = bookingState.fromText
        bookingTimeBar.limit = bookingState.limit
        bookingTimeBar.progress = bookingState.selectedDuration

        selectBookingDurationText(bookingState.selectedDuration, bookingState.limit)

        bookingState.fromTime?.let {
            bookingTimeFrom.setTime(bookingState.fromTime)
            bookingTimeTo.setTime(bookingState.toTime!!)
        }

        showBookingGroup(!bookingState.isPrecise)

        val activeTab = if (bookingState.isPrecise) 1 else 0
        appointmentBookingTabs.getTabAt(activeTab)?.select()
    }

    private fun enableTab(pos: Int, enable: Boolean) {
        TabLayoutUtils.enableTab(appointmentBookingTabs, pos, enable)
    }

    private fun showBookingGroup(quick: Boolean) {
        bookingQuickGroup.visibility = if (quick) View.VISIBLE else View.GONE
        bookingPreciseGroup.visibility = if (quick) View.GONE else View.VISIBLE
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        model.bookingActionS.accept(BookingAction.Dismiss)
    }

    companion object {
        const val TAG = "BOOKING_FRAGMENT_TAG"
    }
}