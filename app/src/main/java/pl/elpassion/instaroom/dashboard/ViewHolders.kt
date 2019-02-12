package pl.elpassion.instaroom.dashboard

import android.graphics.Color
import android.view.View
import com.elpassion.android.commons.recycler.basic.ViewHolderBinder
import kotlinx.android.synthetic.main.item_header.view.*
import kotlinx.android.synthetic.main.item_room_booked.view.*
import kotlinx.android.synthetic.main.item_room_free.view.*
import kotlinx.android.synthetic.main.item_room_own_booked.view.*
import pl.elpassion.instaroom.R
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.DateTimeFormatters
import pl.elpassion.instaroom.util.endDateTime
import pl.elpassion.instaroom.util.startDateTime
import pl.elpassion.instaroom.util.timeLeft

class RoomFreeViewHolder(itemView: View, private val onBook: (room: Room) -> Unit) :
    ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomFreeName.setTextColor(Color.parseColor(item.room.titleColor))
        itemRoomFreeName.setBackgroundResource(getRoomBackground(item.room))
        itemRoomFreeName.text = item.room.name
        itemRoomFreeBookButton.setOnClickListener { onBook(item.room) }
        val event = item.room.events.first()
        itemRoomUpcomingBookingTitle.text = event.name
        itemRoomUpcomingBookingTimeBegin.text = event.startDateTime.format(DateTimeFormatters.time)
        itemRoomUpcomingBookingTimeEnd.text = event.endDateTime.format(DateTimeFormatters.time)
        itemRoomStatusInfo.text = event.startDateTime.timeLeft()
    }
}

class RoomBookedViewHolder(
    itemView: View,
    private val onOpenCalendar: (String) -> Unit,
    private val onBook: (room: Room) -> Unit
) :
    ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomBookedName.setTextColor(Color.parseColor(item.room.titleColor))
        itemRoomBookedName.setBackgroundResource(getRoomBackground(item.room))
        itemRoomBookedName.text = item.room.name
        val event = item.room.events.first()
        itemRoomBookButton.setOnClickListener { onBook(item.room) }
        itemRoomBookedIcon.setOnClickListener { event.htmlLink?.let { link -> onOpenCalendar(link) } }
        itemRoomBookedTitle.text = event.name
        itemRoomBookedTimeBegin.text = event.startDateTime.format(DateTimeFormatters.time)
        itemRoomBookedTimeEnd.text = event.endDateTime.format(DateTimeFormatters.time)
        itemRoomBookStatusInfo.text = event.endDateTime.timeLeft()
        item.room.events.getOrNull(1)?.let { nextEvent ->
            itemRoomNextBookingTitle.text = nextEvent.name
            itemRoomNextBookingTimeBegin.text =
                nextEvent.startDateTime.format(DateTimeFormatters.time)
            itemRoomNextBookingTimeEnd.text = nextEvent.endDateTime.format(DateTimeFormatters.time)
        }
        Unit
    }
}

class RoomOwnBookedViewHolder(itemView: View, private val onOpenCalendar: (String) -> Unit,
                              private val onDeleteEvent: (String) -> Unit) :
    ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as RoomItem
        itemRoomOwnBookedRoomName.setTextColor(Color.parseColor(item.room.titleColor))
        itemRoomOwnBookedRoomName.setBackgroundResource(getRoomBackground(item.room))
        itemRoomOwnBookedRoomName.text = item.room.name
        val event = item.room.events.first { it.isOwnBooked }

        itemRoomOwnBookedRoomEventIcon.setOnClickListener {
            event.htmlLink?.let { link ->
                onOpenCalendar(link)
            }
        }

        itemRoomOwnBookedRoomDeleteButton.setOnClickListener{
            onDeleteEvent(event.id)
        }

        itemRoomOwnBookedRoomEventTitle.text = event.name
        itemRoomOwnBookedRoomEventTimeBegin.text =
            event.startDateTime.format(DateTimeFormatters.time)
        itemRoomOwnBookedRoomEventTimeEnd.text = event.endDateTime.format(DateTimeFormatters.time)
    }
}

class HeaderViewHolder(itemView: View) : ViewHolderBinder<DashboardItem>(itemView) {

    override fun bind(item: DashboardItem) = with(itemView) {
        item as HeaderItem
        itemHeaderTitle.text = item.name
    }
}

fun getRoomBackground(room: Room): Int = when (room.backgroundColor) {
    "#FFE9F9F0" -> R.drawable.background_green_room
    "#FFFFF1DF" -> R.drawable.background_yellow_room
    "#FFE6DEDA" -> R.drawable.background_people_room
    "#FFD9F4FE" -> R.drawable.background_sales_room
    "#FFEBE4FF" -> R.drawable.background_ui_room
    else -> R.drawable.background_dev_room
}
