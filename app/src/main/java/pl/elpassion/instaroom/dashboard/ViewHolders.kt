package pl.elpassion.instaroom.dashboard

import android.view.View
import com.elpassion.android.commons.recycler.basic.ViewHolderBinder
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_room_booked.view.*
import kotlinx.android.synthetic.main.item_room_free.view.*
import kotlinx.android.synthetic.main.item_room_own_booked.view.*
import pl.elpassion.instaroom.api.Room

class RoomFreeViewHolder(itemView: View, private val onBook: (Room) -> Unit) :
    ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomFreeName.text = item.room.name
        itemRoomFreeBookButton.setOnClickListener { onBook(item.room) }
    }
}

class RoomBookedViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomBookedName.text = item.room.name
        itemRoomBookedTitle.text = item.room.events.firstOrNull()?.name
    }
}

class RoomOwnBookedViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomOwnBookedRoomName.text = item.room.name
        itemRoomOwnBookedRoomEventTitle.text = item.room.events.firstOrNull()?.name
    }
}

class HeaderViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as HeaderItem
        itemHeaderTitle.text = item.name
    }
}
