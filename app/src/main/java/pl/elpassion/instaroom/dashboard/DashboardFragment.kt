package pl.elpassion.instaroom.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.api.Room

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
        /*FIXME
        dashboard_recycler_view.adapter = basicAdapterWithLayoutAndBinder(rooms, R.layout.item_room) { holder, item ->
            holder.itemView.item_room_name_tv.text = item.name
            holder.itemView.item_room_meeting_title_tv.text = item.events.firstOrNull()?.name
            holder.itemView.setOnClickListener {
                onRoomClicked(item)
            }
        }*/
        dashboard_recycler_view.layoutManager = LinearLayoutManager(context)
    }

    private fun updateView(state: DashboardState?) {
        rooms.run { clear(); addAll(state?.rooms.orEmpty()) }
        dashboard_recycler_view.adapter?.notifyDataSetChanged()
        state?.errorMessage?.let { context?.toast(it) }
    }

    fun onRoomClicked(room: Room) {

    }
}
