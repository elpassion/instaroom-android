package pl.elpassion.instaroom.util

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.threeten.bp.ZonedDateTime
import pl.elpassion.instaroom.booking.HourMinuteTime
import pl.elpassion.instaroom.booking.hourMinuteTime

class TimePickerFragmentDialog : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    private val hourMinuteTimeRelay = PublishRelay.create<HourMinuteTime>()
    private val dismissRelay = PublishRelay.create<DismissEvent>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val initTime = arguments?.getSerializable(TIME_KEY) as HourMinuteTime?
            ?: ZonedDateTime.now().hourMinuteTime

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, initTime.hour, initTime.minute, true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hourMinuteTimeRelay.accept(HourMinuteTime(hourOfDay, minute))
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        println("onDismiss")
        dismissRelay.accept(DismissEvent)
    }

    fun dismisses(): Observable<DismissEvent> {
        return dismissRelay
    }

    fun timeChanges(): Observable<HourMinuteTime> {
        return hourMinuteTimeRelay
    }

    companion object {
        const val TAG = "TIME_PICKER_DIALOG_TAG"

        private const val TIME_KEY = "HOUR_MINUTE_TIME_KEY"

        fun withTime(hourMinuteTime: HourMinuteTime): TimePickerFragmentDialog {
            val fragment = TimePickerFragmentDialog()
            fragment.arguments = Bundle().apply {
                putSerializable(TIME_KEY, hourMinuteTime)
            }

            return fragment
        }
    }
}

object DismissEvent