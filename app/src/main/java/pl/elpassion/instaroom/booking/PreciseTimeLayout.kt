package pl.elpassion.instaroom.booking

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.setPadding
import kotlinx.android.synthetic.main.precise_time_layout.view.*
import pl.elpassion.instaroom.R

class PreciseTimeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.precise_time_layout, this)
    }

    fun setTime(timeText: String) {
        val textParts = timeText.split(" ")

        timeTextView.text = textParts[0]
        periodTextView.text = textParts[1]
    }


}