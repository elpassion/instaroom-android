package pl.elpassion.instaroom.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.elpassion.android.commons.recycler.adapters.basicAdapterWithConstructors
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.LifecycleFragment
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.booking.BookingDialogFragment
import pl.elpassion.instaroom.dashboard.RoomItem.*
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.summary.BookingSummaryDialog
import pl.elpassion.instaroom.util.isBooked
import pl.elpassion.instaroom.util.isOwnBooked
import pl.elpassion.instaroom.util.replaceWith
import pl.elpassion.instaroom.util.viewEventInCalendar

class DashboardFragment : LifecycleFragment() {

    private val items = mutableListOf<DashboardItem>()
    private var bookingDialog: BookingDialogFragment? = null
    private var progressDialog: ProgressDialogFragment? = null
    private var summaryDialog: BookingSummaryDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.dashboard_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        restoreDialogs(savedInstanceState)

        model.dashboardStateD.observe(this, Observer(::updateView))
        model.dashboardRoomListD.observe(this, Observer(::updateRoomsList))
        model.dashboardRefreshingD.observe(this, Observer(::updateRefreshView))
        setupMenu()
        setupList()
    }

    private fun restoreDialogs(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            bookingDialog = fragmentManager?.getFragment(
                savedInstanceState,
                BookingDialogFragment.TAG
            ) as BookingDialogFragment?
            println("bookingDialog is null? ${bookingDialog == null}")
            progressDialog = fragmentManager?.getFragment(
                savedInstanceState,
                ProgressDialogFragment.TAG
            ) as ProgressDialogFragment?
            println("progressDialog is null? ${bookingDialog == null}")
            summaryDialog = fragmentManager?.getFragment(
                savedInstanceState,
                BookingSummaryDialog.TAG
            ) as BookingSummaryDialog?
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bookingDialog?.run {
            if(this.isAdded) fragmentManager?.putFragment(outState, BookingDialogFragment.TAG, this)
        }

        progressDialog?.run {
            if(this.isAdded) fragmentManager?.putFragment(outState, ProgressDialogFragment.TAG, this)
        }

        summaryDialog?.run {
            if(this.isAdded) fragmentManager?.putFragment(outState, BookingSummaryDialog.TAG, this)
        }
    }

    private fun updateRefreshView(dashboardRefreshing: DashboardRefreshing?) {
        dashboardRefreshing?.let {
            roomsSwipeRefresh.isRefreshing = dashboardRefreshing.isRefreshing
        }

    }

    private fun updateRoomsList(dashboardRoomList: DashboardRoomList?) {
        dashboardRoomList?.let {
            items.replaceWith(createItems(dashboardRoomList.rooms, requireContext()))
            roomsRecyclerView.adapter?.notifyDataSetChanged()
        }
    }


    private fun setupMenu() {
        dashboardToolbar.inflateMenu(R.menu.dashboard_menu)
        dashboardToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.sign_out_action) {
                model.dashboardActionS.accept(DashboardAction.SelectSignOut)
            }
            true
        }
    }

    private fun setupList() {
        roomsRecyclerView.adapter = basicAdapterWithConstructors(items) { position ->
            val item = items[position]

            when (item) {
                is HeaderItem -> R.layout.item_header to ::HeaderViewHolder
                is RoomItem -> {
                    when (item) {

                        is OwnBookedRoomItem -> R.layout.item_room_own_booked to { view: View ->
                            RoomOwnBookedViewHolder(view, ::onCalendarOpen, ::onDeleteEvent)
                        }
                        is BookedRoomItem -> R.layout.item_room_booked to { view: View ->
                            RoomBookedViewHolder(view, ::onCalendarOpen, ::onBookingClicked)
                        }
                        is FreeRoomItem -> R.layout.item_room_free to { view: View ->
                            RoomFreeViewHolder(view, ::onBookingClicked)
                        }
                    }
                }
            }
        }
        roomsRecyclerView.layoutManager = LinearLayoutManager(context)
        roomsSwipeRefresh.setOnRefreshListener {
            model.dashboardActionS.accept(DashboardAction.RefreshRooms)
        }
    }

    private fun onDeleteEvent(eventId: String) {
        model.dashboardActionS.accept(DashboardAction.DeleteEvent(eventId))
    }

    private fun updateView(state: DashboardState?) {
        state ?: return

        when (state) {
            is DashboardState.BookingDetailsState -> showBookingDetails()
            is DashboardState.BookingInProgressState -> showProgressDialog()
            is DashboardState.BookingSuccessState -> showSummaryDialog()
            is DashboardState.Default -> hideDialogs()
            is DashboardState.Error -> showError(state.errorMessage)
        }
    }

    private fun hideDialogs() {
        bookingDialog?.apply { if (this.isAdded) this.dismiss() }
        summaryDialog?.apply { if (this.isAdded) this.dismiss() }
        progressDialog?.apply { if (this.isAdded) this.dismiss() }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun showSummaryDialog() {
        if (summaryDialog != null && summaryDialog!!.isAdded) return

        progressDialog?.apply { if (this.isAdded) this.dismiss() }
        if(summaryDialog == null) summaryDialog = BookingSummaryDialog()
        summaryDialog!!.show(fragmentManager, BookingSummaryDialog.TAG)
    }


    private fun showProgressDialog() {
        if (progressDialog != null && progressDialog!!.isAdded) return
        if(progressDialog == null) progressDialog = ProgressDialogFragment()
        progressDialog!!.show(fragmentManager, ProgressDialogFragment.TAG)
    }

    private fun showBookingDetails() {
        if (bookingDialog != null && bookingDialog!!.isAdded) return

        if(bookingDialog == null) bookingDialog = BookingDialogFragment()
        bookingDialog!!.show(fragmentManager, BookingDialogFragment.TAG)
    }

    private fun onCalendarOpen(link: String) {
        viewEventInCalendar(link, REQ_OPEN_CALENDAR)
    }

    private fun onBookingClicked(room: Room) {
        model.dashboardActionS.accept(DashboardAction.ShowBookingDetails(room))
    }


    companion object {
        private const val REQ_OPEN_CALENDAR = 1
    }
}

sealed class DashboardItem

sealed class RoomItem : DashboardItem() {
    abstract val room: Room

    data class OwnBookedRoomItem(override val room: Room) : RoomItem()
    data class BookedRoomItem(override val room: Room) : RoomItem()
    data class FreeRoomItem(override val room: Room) : RoomItem()

}

data class HeaderItem(val name: String) : DashboardItem()

private fun createItems(rooms: List<Room>, context: Context): List<DashboardItem> {
    val yourBookings = rooms.filter { it.isOwnBooked }.map(::OwnBookedRoomItem)
    val freeRooms = rooms
        .filter { !it.isBooked }
        .map(::FreeRoomItem)
    val occupiedRooms = rooms.filter { it.isBooked }.map(::BookedRoomItem)

    return mutableListOf<DashboardItem>().apply {
        if (yourBookings.isNotEmpty()) add(HeaderItem(context.getString(R.string.your_bookings)))
        addAll(yourBookings)
        if (freeRooms.isNotEmpty()) add(HeaderItem(context.getString(R.string.free_rooms)))
        addAll(freeRooms)
        if (occupiedRooms.isNotEmpty()) add(HeaderItem(context.getString(R.string.occupied_rooms)))
        addAll(occupiedRooms)
    }
}
