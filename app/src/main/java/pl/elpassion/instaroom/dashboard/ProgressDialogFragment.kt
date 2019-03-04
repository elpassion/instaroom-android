package pl.elpassion.instaroom.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.booking_progress_dialog.*
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.booking.TimePickerDialogFragment
import pl.elpassion.instaroom.util.HourMinuteTime

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.booking_progress_dialog, container, true)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ProgressDialogFragment.MESSAGE_TAG)?.let {message ->
            bookingProgressMessage.text = message
        }
    }


    companion object {
        const val TAG = "ProgressDialogFragment"
        const val MESSAGE_TAG = "message"

        fun withMessage(message: String): ProgressDialogFragment {
            val fragment = ProgressDialogFragment()
            fragment.arguments = Bundle().apply {
                putString(ProgressDialogFragment.MESSAGE_TAG, message)
            }

            return fragment
        }
    }
}