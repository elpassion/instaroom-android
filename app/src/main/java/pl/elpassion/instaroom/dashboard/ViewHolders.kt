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
        val event = item.events.first()
        itemRoomUpcomingBookingTitle.text = event.name
        itemRoomUpcomingBookingTimeBegin.text = event.startTime.format(DateTimeFormatters.time)
        itemRoomUpcomingBookingTimeEnd.text = event.endTime.format(DateTimeFormatters.time)
    }
}

class RoomBookedViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomBookedName.text = item.room.name
        itemRoomBookedTitle.text = event.name
        itemRoomBookedTimeBegin.text = event.startTime.format(DateTimeFormatters.time)
        itemRoomBookedTimeEnd.text = event.endTime.format(DateTimeFormatters.time)
        item.events.getOrNull(1)?.let { nextEvent ->
            itemRoomNextBookingTitle.text = nextEvent.name
            itemRoomNextBookingTimeBegin.text = nextEvent.startTime.format(DateTimeFormatters.time)
            itemRoomNextBookingTimeEnd.text = nextEvent.endTime.format(DateTimeFormatters.time)
        }
    }
}

class RoomOwnBookedViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomOwnBookedRoomName.text = item.room.name
        val event = item.events.first()
        itemRoomOwnBookedRoomEventTitle.text = event.name
        itemRoomOwnBookedRoomEventTimeBegin.text = event.startTime.format(DateTimeFormatters.time)
        itemRoomOwnBookedRoomEventTimeEnd.text = event.endTime.format(DateTimeFormatters.time)
    }
}

class HeaderViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as HeaderItem
        itemHeaderTitle.text = item.name
    }
}
