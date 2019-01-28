package pl.elpassion.instaroom.booking

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.booking_details_fragment.*
import kotlinx.android.synthetic.main.booking_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R

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
    }

    private fun setupTabs() {
        appointmentBookingTabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> model.bookingActionS.accept(BookingAction.QuickBookingSelected)
                    1 -> model.bookingActionS.accept(BookingAction.PreciseBookingSelected)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }


    private fun updateView(bookingState: BookingState?) {
        bookingState?: return

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