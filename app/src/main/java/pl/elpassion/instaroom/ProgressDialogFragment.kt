package pl.elpassion.instaroom

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment

class ProgressDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.booking_progress_dialog, container, true)


    companion object {
        const val PROGRESS_DIALOG_TAG = "PROGRESS_DIALOG_TAG"
    }
}