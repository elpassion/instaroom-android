package pl.elpassion.instaroom.summary

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.booking_success_dialog.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.threeten.bp.format.DateTimeFormatter
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.endDateTime
import pl.elpassion.instaroom.util.startDateTime
import pl.elpassion.instaroom.util.viewEventInCalendar

class BookingSummaryDialog : DialogFragment() {

    private val model by sharedViewModel<AppViewModel>()

    private val hourMinuteTimeFormatter: DateTimeFormatter by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.booking_success_dialog, container, false)
    }

    override fun getTheme(): Int {
        return R.style.WideDialog
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.summaryStateD.observe(this, Observer(::updateViewState))
        model.summaryDataD.observe(this, Observer(::updateViewData))
        model.summaryCalendarSyncD.observe(this, Observer(::updateCalendarSyncView))

        Observable.mergeArray(
            setupDismissButton(),
            setupEditEventButton()
        ).subscribe(model.summaryActionS)
    }

    private fun setupDismissButton() = dismissButton
        .clicks()
        .map { SummaryAction.SelectDismiss }

    private fun setupEditEventButton() = editButton
        .clicks()
        .map { SummaryAction.EditEvent }

    private fun updateCalendarSyncView(summaryCalendarSync: SummaryCalendarSync?) {
        summaryCalendarSync?.let { sync ->
            if(sync.isSynced) enableEditButton() else disableEditButton()
            progressBar.visibility = if (sync.isSyncing) View.VISIBLE else View.GONE
        }
    }

    private fun updateViewData(summaryData: SummaryData?) {
        summaryData?.let { data ->
            val event = data.event
            bookingTitle.text = event.name
            bookingFromTime.setTime(event.startDateTime.format(hourMinuteTimeFormatter))
            bookingToTime.setTime(event.endDateTime.format(hourMinuteTimeFormatter))

            val room = data.room
            bookingRoomInfo.setTextColor(Color.parseColor(room.titleColor))
            bookingRoomInfo.text = "in ${room.name}"
        }
    }


    private fun updateViewState(summaryState: SummaryState?) {
        summaryState ?: return

        when (summaryState) {
            is SummaryState.Default -> Unit
            is SummaryState.Dismissing -> dismiss()
            is SummaryState.ViewingEvent -> showEventInCalendar(summaryState.link)
        }
    }

    private fun enableEditButton() {
        editButton.isEnabled = true
        editButton.alpha = 1f
    }

    private fun disableEditButton() {
        editButton.isEnabled = false
        editButton.alpha = 0.3f
    }

    private fun showEventInCalendar(link: String) {
        viewEventInCalendar(link, REQ_OPEN_CALENDAR)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        model.summaryActionS.accept(SummaryAction.Dismiss)
    }

    companion object {
        const val TAG = "BOOKING_SUMMARY_DIALOG"
        private const val REQ_OPEN_CALENDAR = 3452
    }

}