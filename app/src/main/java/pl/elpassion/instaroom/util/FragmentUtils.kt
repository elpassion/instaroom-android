package pl.elpassion.instaroom.util

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

fun Fragment.viewEventInCalendar(htmlLink: String, requestCode: Int) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(htmlLink)
    }
    startActivityForResult(intent, requestCode)
}