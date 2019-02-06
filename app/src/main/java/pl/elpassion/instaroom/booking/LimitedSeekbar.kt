package pl.elpassion.instaroom.booking

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.jakewharton.rxbinding3.widget.changes
import io.reactivex.Observable

class LimitedSeekbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    var limit: Int? = null
    set(value) {
        value?.let {
            if (progress > value) progress = value
        }
        field = value
    }

    override fun setMax(max: Int) {
        super.setMax(max)
        if(limit == null) limit = max
    }

    fun limitedChanges(): Observable<Int> =
        changes().skipInitialValue()
            .doOnNext{ newValue ->
                limit?.let {
                    if (newValue > it) progress = it
                }
            }
            .filter{ newValue ->
                limit?.let {
                    it>=newValue
                } ?:true
            }

}