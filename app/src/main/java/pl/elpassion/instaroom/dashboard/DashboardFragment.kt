package pl.elpassion.instaroom.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elpassion.android.commons.recycler.adapters.basicAdapterWithConstructors
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.ProgressDialogFragment
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.booking.BookingFragment
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.isBooked
import pl.elpassion.instaroom.util.isOwnBooked
import pl.elpassion.instaroom.util.replaceWith

class DashboardFragment : Fragment() {

    private val model by sharedViewModel<AppViewModel>()
    private val items = mutableListOf<DashboardItem>()
    private val bookingFragment by lazy {BookingFragment()}
    private val progressDialog by lazy {ProgressDialogFragment()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dashboard_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.dashboardState.observe(this, Observer(::updateView))
        setupMenu()
        setupList()
    }


    private fun setupMenu() {
        dashboardToolbar.inflateMenu(R.menu.dashboard_menu)
        dashboardToolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.sign_out_action) {
                model.dashboardActionS.accept(DashboardAction.SelectSignOut)
                findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
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
                    val room = item.room
                    when {
                        room.isOwnBooked -> R.layout.item_room_own_booked to { view: View ->
                            RoomOwnBookedViewHolder(view, ::onCalendarOpen)
                        }
                        room.isBooked -> R.layout.item_room_booked to { view: View ->
                            RoomBookedViewHolder(view, ::onCalendarOpen)
                        }
                        else -> R.layout.item_room_free to { view: View ->
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

    private fun updateView(state: DashboardState?) {
        state?:return

        when (state) {
            is DashboardState.RoomListState -> updateRoomList(state)
            is DashboardState.BookingDetailsState -> showBookingDetails()
            is DashboardState.BookingInProgressState -> showProgressDialog()
        }
    }



    private fun showProgressDialog() {
        progressDialog.show(fragmentManager, ProgressDialogFragment.PROGRESS_DIALOG_TAG)
    }

    private fun showBookingDetails() {
        if(!bookingFragment.isAdded){
            bookingFragment.show(fragmentManager, BookingFragment.TAG)
        }
    }

    private fun updateRoomList(state: DashboardState.RoomListState) {
        items.replaceWith(createItems(state.rooms, requireContext()))
        roomsRecyclerView.adapter?.notifyDataSetChanged()
        state.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        roomsSwipeRefresh.isRefreshing = state.isRefreshing
    }

    private fun onCalendarOpen(link: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(link)
        }
        startActivityForResult(intent, REQ_OPEN_CALENDAR)
    }

    private fun onBookingClicked(room: Room) { model.dashboardActionS.accept(DashboardAction.ShowBookingDetails(room)) }

    private fun onBookingCanceled() = model.dashboardActionS.accept(DashboardAction.CancelBooking)


    companion object {
        private const val REQ_OPEN_CALENDAR = 1
    }
}

sealed class DashboardItem

data class RoomItem(val room: Room) : DashboardItem()
data class HeaderItem(val name: String) : DashboardItem()

private fun createItems(rooms: List<Room>, context: Context): List<DashboardItem> {
    val yourBookings = rooms.filter { it.isBooked && it.isOwnBooked }.map(::RoomItem)
    val freeRooms = rooms.filter { !it.isBooked }.map(::RoomItem)
    val occupiedRooms = rooms.filter { it.isBooked }.map(::RoomItem)

    return mutableListOf<DashboardItem>().apply {
        if (yourBookings.isNotEmpty()) add(HeaderItem(context.getString(R.string.your_bookings)))
        addAll(yourBookings)
        if (freeRooms.isNotEmpty()) add(HeaderItem(context.getString(R.string.free_rooms)))
        addAll(freeRooms)
        if (occupiedRooms.isNotEmpty()) add(HeaderItem(context.getString(R.string.occupied_rooms)))
        addAll(occupiedRooms)
    }
}
