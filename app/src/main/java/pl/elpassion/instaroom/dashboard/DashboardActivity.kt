package pl.elpassion.instaroom.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.elpassion.android.commons.recycler.adapters.basicAdapterWithLayoutAndBinder
import kotlinx.android.synthetic.main.activity_dashboard.dashboard_recycler_view
import kotlinx.android.synthetic.main.item_room.view.item_room_meeting_title_tv
import kotlinx.android.synthetic.main.item_room.view.item_room_name_tv
import org.koin.android.viewmodel.ext.android.viewModel
import pl.elpassion.instaroom.AppViewModel
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.api.Room

class DashboardActivity : AppCompatActivity() {

    private val model by viewModel<AppViewModel>()
    private val rooms = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        model.dashboardState.observe(this, Observer(::updateView))

        setUpList()
    }

    private fun setUpList() {
        dashboard_recycler_view.adapter = basicAdapterWithLayoutAndBinder(rooms, R.layout.item_room) { holder, item ->
            holder.itemView.item_room_name_tv.text = item.name
            holder.itemView.item_room_meeting_title_tv.text = item.events.firstOrNull()?.name
            holder.itemView.setOnClickListener {
                onRoomClicked(item)
            }
        }
        dashboard_recycler_view.layoutManager = LinearLayoutManager(this)
    }

    private fun updateView(state: DashboardState?) {
        rooms.run { clear(); addAll(state?.rooms.orEmpty()) }
        dashboard_recycler_view.adapter?.notifyDataSetChanged()
        state?.errorMessage?.let { toast(it) }
    }

    fun onRoomClicked(room: Room) {

    }
}
