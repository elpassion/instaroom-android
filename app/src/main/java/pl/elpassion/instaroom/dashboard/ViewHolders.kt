package pl.elpassion.instaroom.dashboard

import android.view.View
import com.elpassion.android.commons.recycler.basic.ViewHolderBinder
import kotlinx.android.synthetic.main.item_room_booked.view.*
import kotlinx.android.synthetic.main.item_room_free.view.*
import kotlinx.android.synthetic.main.item_room_own_booked.view.*
import pl.elpassion.instaroom.api.Room

class RoomFreeViewHolder(itemView: View) : ViewHolderBinder<Room>(itemView) {

    override fun bind(item: Room) = with(itemView) {
        itemRoomFreeName.text = item.name
    }
}

class RoomBookedViewHolder(itemView: View) : ViewHolderBinder<Room>(itemView) {

    override fun bind(item: Room) = with(itemView) {
        itemRoomBookedName.text = item.name
        itemRoomBookedTitle.text = item.events.firstOrNull()?.name
    }
}

class RoomOwnBookedViewHolder(itemView: View) : ViewHolderBinder<Room>(itemView) {

    override fun bind(item: Room) = with(itemView) {
        itemRoomOwnBookedRoomName.text = item.name
        itemRoomOwnBookedRoomEventTitle.text = item.events.firstOrNull()?.name
    }
}
