package pl.elpassion.instaroom.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.TimePickerDialogFragment
import pl.elpassion.instaroom.kalendar.Event

class BookingSummaryDialog : DialogFragment() {

    private val model by sharedViewModel<AppViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.booking_success_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}