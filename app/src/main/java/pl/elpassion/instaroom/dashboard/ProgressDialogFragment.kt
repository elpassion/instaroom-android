package pl.elpassion.instaroom.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import pl.elpassion.instaroom.R

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.booking_progress_dialog, container, true)


    companion object {
        const val TAG = "TAG"
    }
}