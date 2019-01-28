package pl.elpassion.instaroom.booking

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.widget.textChanges
import kotlinx.android.synthetic.main.booking_details_fragment.*
import kotlinx.android.synthetic.main.booking_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.util.selections
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

class BookingFragment : BottomSheetDialogFragment() {

    private val model by sharedViewModel<AppViewModel>()

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.booking_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.bookingState.observe(this, Observer(::updateView))
        setupTabs()
        setupTitleEditText()
    }

    @SuppressLint("CheckResult")
    private fun setupTitleEditText() {
        bookingTitle.textChanges()
            .debounce(100, TimeUnit.MILLISECONDS)
            .filter { text -> text.isNotBlank() }
            .map { text -> BookingAction.TitleChanged(text.toString()) }
            .subscribe(model.bookingActionS)
    }

    @SuppressLint("CheckResult")
    private fun setupTabs() {
//      https://github.com/JakeWharton/RxBinding/issues/495
//      original selections method doesn't work until this issue will be resolved, so i copied that class and fixed it

        appointmentBookingTabs.selections().map { tab ->
            println("tab = ${tab.position}")
            when (tab.position) {
                0 -> BookingAction.QuickBookingSelected
                1 -> BookingAction.PreciseBookingSelected
                else -> {
                    throw IllegalArgumentException()
                }
            }
        }.subscribe(model.bookingActionS)
    }


    private fun updateView(bookingState: BookingState?) {
        bookingState ?: return

        appointmentBookingTitle.text = bookingState.room.name
        appointmentBookingTitle.setTextColor(Color.parseColor(bookingState.room.titleColor))
        appointmentBookingBackground.setBackgroundColor(Color.parseColor(bookingState.room.backgroundColor))

        when (bookingState) {
            is BookingState.QuickBooking -> showQuickBookingGroup(bookingState)
            is BookingState.PreciseBooking -> showPreciseBookingGroup(bookingState)
        }

    }


    private fun showQuickBookingGroup(bookingState: BookingState.QuickBooking) {
        bookingQuickGroup.visibility = View.VISIBLE
        bookingPreciseGroup.visibility = View.GONE
    }

    private fun showPreciseBookingGroup(bookingState: BookingState.PreciseBooking) {
        bookingQuickGroup.visibility = View.GONE
        bookingPreciseGroup.visibility = View.VISIBLE
    }


}