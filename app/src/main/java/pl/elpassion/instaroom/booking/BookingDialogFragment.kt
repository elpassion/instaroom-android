package pl.elpassion.instaroom.booking

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
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
import pl.elpassion.instaroom.dashboard.getRoomBackground
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.HourMinuteTime
import pl.elpassion.instaroom.util.TabLayoutUtils
import pl.elpassion.instaroom.util.selections
import java.util.concurrent.TimeUnit

class BookingDialogFragment : RoundedBottomSheetDialogFragment() {

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

        model.bookingConstantsD.observe(this, Observer(::updateConstantView))
        model.bookingQuickTimeD.observe(this, Observer(::updateQuickTimeView))
        model.bookingPreciseTimeD.observe(this, Observer(::updatePreciseTimeView))
        model.bookingAllDayD.observe(this, Observer(::updateAllDayView))
        model.bookingTitleD.observe(this, Observer(::updateTitleView))
        model.bookingTypeD.observe(this, Observer(::updateTypeView))
        model.bookingStateD.observe(this, Observer(::updateView))

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

    private fun updateConstantView(bookingConstants: BookingConstants?) {
        bookingConstants?.let {
            enableTab(0, bookingConstants.quickBookingAvailable)
            enableTab(1, bookingConstants.preciseBookingAvailable)

            appointmentBookingTitle.text = bookingConstants.room.name

            try {
                appointmentBookingTitle.setTextColor(Color.parseColor(bookingConstants.room.titleColor))
            } catch (e: IllegalArgumentException) {
                println("Error parsing color")
            }

            bookingConstants.room.let {room ->
                appointmentBookingBackground.setBackgroundResource(getRoomBackground(room))
            }

            bookingTitle.hint = bookingConstants.hint

            bookingFromNowFor.text = bookingConstants.quickBookingTimeText
        }

    }

    private fun updateQuickTimeView(bookingQuickTime: BookingQuickTime?) {
        bookingQuickTime?.let {
            bookingTimeBar.limit = bookingQuickTime.limit
            bookingTimeBar.progress = bookingQuickTime.durationPosition

            selectBookingDurationText(bookingQuickTime.durationPosition, bookingQuickTime.limit)
        }

    }

    private fun updatePreciseTimeView(bookingPreciseTime: BookingPreciseTime?) {
        bookingPreciseTime?.let {
            bookingPreciseTime.fromText?. let { text -> bookingTimeFrom.setTime(text) }
            bookingPreciseTime.toText?. let { text -> bookingTimeTo.setTime(text) }
        }
    }

    private fun updateAllDayView(bookingAllDay: BookingAllDay?) {
        bookingAllDay?.let {
            bookingAllDaySwitch.isEnabled = bookingAllDay.enabled
        }
    }

    private fun updateTitleView(bookingTitleD: BookingTitle?) {
        bookingTitle.setText(bookingTitleD?.text)
    }

    private fun updateTypeView(bookingType: BookingType?) {
        bookingType?.let {
            showBookingGroup(bookingType.isQuick)
            val activeTab = if (bookingType.isQuick) 0 else 1
            appointmentBookingTabs.getTabAt(activeTab)?.select()
        }
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
            is BookingState.Default -> Unit
            is BookingState.PickingTime ->
                showTimePickerDialog(
                    bookingState.fromTime,
                    bookingState.hourMinuteTime
                )
            is BookingState.Dismissing -> dismiss()
            is BookingState.Error -> Unit // show error
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