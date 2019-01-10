package pl.elpassion.instaroom.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.elpassion.android.commons.recycler.adapters.basicAdapterWithLayoutAndBinder
import com.elpassion.android.commons.recycler.basic.ViewHolderBinder
import kotlinx.android.synthetic.main.dashboard_fragment.*
import kotlinx.android.synthetic.main.item_room_booked.view.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.api.Room
import pl.elpassion.instaroom.util.replaceWith

class DashboardFragment : Fragment() {

    private val model by sharedViewModel<AppViewModel>()
    private val rooms = mutableListOf<Room>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.dashboard_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.dashboardState.observe(this, Observer(::updateView))
        setUpList()
    }

    private fun setUpList() {
        roomsRecyclerView.adapter = basicAdapterWithLayoutAndBinder(rooms, R.layout.item_room_booked, ::bindRoom)
        roomsRecyclerView.layoutManager = LinearLayoutManager(context)
        roomsSwipeRefresh.setOnRefreshListener {
            model.dashboardActionS.accept(DashboardAction.RefreshRooms)
        }

    }

    private fun updateView(state: DashboardState?) {
        rooms.replaceWith(state?.rooms.orEmpty())
        roomsRecyclerView.adapter?.notifyDataSetChanged()
        state?.errorMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        roomsSwipeRefresh.isRefreshing = state?.isRefreshing ?: false
    }

    private fun bindRoom(holder: ViewHolderBinder<Room>, item: Room) = with(holder.itemView) {
        itemRoomBookingName.text = item.name
        itemRoomBookingTitle.text = item.events.firstOrNull()?.name
    }
}
