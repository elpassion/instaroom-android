package pl.elpassion.instaroom.booking

import io.kotlintest.matchers.future.completedExceptionally
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec
import org.junit.Test

import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.util.BookingDuration
import pl.elpassion.instaroom.util.endDateTime
import pl.elpassion.instaroom.util.startDateTime

class BookingInitializerTest : FreeSpec() {

    private val room = Room("", "", emptyList(), "", "", "", "")
    private val emptyEvent = Event("", "", "EVENT 1", "", "", false)
    private val initTime = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)

    private fun createEventsWithXMinutesBreakAfterYEvent(
        breakDurationInMinutes: Int,
        pos: Int
    ): List<Event> {
        val result = mutableListOf<Event>()
        var previousEventEndTime: ZonedDateTime
        var currentEventEndTime = initTime
        for (i in 0..3) {
            previousEventEndTime = if (i == pos) {
                currentEventEndTime.plusMinutes(breakDurationInMinutes.toLong())
            } else {
                currentEventEndTime
            }
            currentEventEndTime = previousEventEndTime.plusMinutes(15)

            result.add(
                emptyEvent.copy(
                    startTime = previousEventEndTime.toString(),
                    endTime = currentEventEndTime.toString()
                )
            )
        }
        return result
    }

    private val eventsWithMoreThan15MinutesBreakAfterFirst: List<Event> =
        createEventsWithXMinutesBreakAfterYEvent(16, 1)
    private val eventsWithMoreThan15MinutesBreakBeforeFirst =
        createEventsWithXMinutesBreakAfterYEvent(16, 0)
    private val eventsWith10MinutesBreakAfterFirst: List<Event> =
        createEventsWithXMinutesBreakAfterYEvent(10, 1)
    private val eventsWithNoBreaks: List<Event> =
        createEventsWithXMinutesBreakAfterYEvent(0, 1)

    private val eventsWith10MinutesBreakBeforeFirst =
        createEventsWithXMinutesBreakAfterYEvent(10, 0)

    init {

        "find first free quick booking" - {

            "with at least 15 minutes break after first event returns expected time" {
                val events = eventsWithMoreThan15MinutesBreakAfterFirst
                val result = findFirstFreeQuickBookingTime(
                    events,
                    initTime
                )
                assert(result == Pair(events[0].endDateTime, events[1].startDateTime))
            }

            "with at least 15 minutes break before events returns expected time" {
                val events = eventsWithMoreThan15MinutesBreakBeforeFirst
                val result = findFirstFreeQuickBookingTime(
                    events,
                    initTime
                )
                assert(result == Pair(initTime, events[0].startDateTime))
            }

            "with less than 15 minutes break after first event throws exception" {
                shouldThrow<BookingUnavailableException> {
                    val events = eventsWith10MinutesBreakAfterFirst
                    findFirstFreeQuickBookingTime(
                        events,
                        initTime
                    )
                }
            }

            "with less than 15 minutes break before first event throws exception" {
                shouldThrow<BookingUnavailableException> {
                    val events = eventsWith10MinutesBreakBeforeFirst
                    findFirstFreeQuickBookingTime(
                        events,
                        initTime
                    )
                }
            }

            "with no breaks throws exception" {
                shouldThrow<BookingUnavailableException> {
                    val events = eventsWithNoBreaks
                    findFirstFreeQuickBookingTime(
                        events,
                        initTime
                    )
                }
            }

        }

        "find first free precise booking" - {

            "with at least 15 minutes break after first event returns expected time" {
                val events = eventsWithMoreThan15MinutesBreakAfterFirst
                val result = findFirstFreePreciseBookingTime(
                    events,
                    initTime
                )
                assert(result == Pair(events[0].endDateTime, events[1].startDateTime))
            }

            "with at least 15 minutes break before events returns expected time" {
                val events = eventsWithMoreThan15MinutesBreakBeforeFirst
                val result = findFirstFreePreciseBookingTime(
                    events,
                    initTime
                )
                assert(result == Pair(initTime, events[0].startDateTime))
            }

            "with less than 15 minutes break after first event returns expected time" {
                val events = eventsWith10MinutesBreakAfterFirst
                val result = findFirstFreePreciseBookingTime(
                    events,
                    initTime
                )
                assert(result == Pair(events[0].endDateTime, events[1].startDateTime))
            }

            "with less than 15 minutes break before first event returns expected time" {
                val events = eventsWith10MinutesBreakBeforeFirst
                val result = findFirstFreePreciseBookingTime(
                    events,
                    initTime
                )
                assert(result == Pair(initTime, events[0].startDateTime))
            }

            "with no breaks throws exception" {
                shouldThrow<BookingUnavailableException> {
                    val events = eventsWithNoBreaks
                    findFirstFreePreciseBookingTime(
                        events,
                        initTime
                    )
                }
            }

        }

        "calculate quick booking limit index" - {

            "within <15 minutes range break returns -1" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(16)
                )
                assert(result == BookingDuration.MIN_15.ordinal)
            }

            "within <15,30) minutes range break returns MIN_15 ordinal" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(16)
                )
                assert(result == BookingDuration.MIN_15.ordinal)
            }

            "within <30,45) minutes range break returns MIN_30 ordinal" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(31)
                )
                assert(result == BookingDuration.MIN_30.ordinal)
            }

            "within <45,60) minutes range break returns MIN_45 ordinal" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(46)
                )
                assert(result == BookingDuration.MIN_45.ordinal)
            }

            "within <60,120) minutes range break returns HOUR_1 ordinal" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(61)
                )
                assert(result == BookingDuration.HOUR_1.ordinal)
            }

            "with >=120 minutes break returns HOUR_2 ordinal" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(121)
                )
                assert(result == BookingDuration.HOUR_2.ordinal)
            }
        }

        "initialize booking variables" - {
            val defaultUserName = "Name"
            val defaultResult = BookingValues(
                true,
                true,
                false,
                room,
                "",
                "$defaultUserName's booking",
                initTime,
                initTime,
                BookingDuration.MIN_15,
                -1,
                initTime,
                initTime,
                false
            )

            "with events with no break" {
                val roomWithEvents = room.copy(events = eventsWithNoBreaks)
                val result = initializeBookingVariables(
                    defaultUserName,
                    roomWithEvents
                )
                val expected = defaultResult.copy(
                    quickAvailable = false,
                    preciseAvailable = false,
                    isPrecise = true,
                    room = roomWithEvents)

                assert(result == expected)
            }

            "with events with only precise break" {
                val events = eventsWith10MinutesBreakBeforeFirst
                val roomWithEvents = room.copy(events = events)
                val result = initializeBookingVariables(
                    defaultUserName,
                    roomWithEvents
                )
                val expected = defaultResult.copy(
                    quickAvailable = false,
                    preciseAvailable = true,
                    isPrecise = true,
                    room = roomWithEvents,
                    preciseFromTime = initTime,
                    preciseToTime = events[0].startDateTime)

                assert(result == expected)
            }

            "with events with quick and precise break" {
                val events = eventsWithMoreThan15MinutesBreakBeforeFirst
                val roomWithEvents = room.copy(events = events)
                val result = initializeBookingVariables(
                    defaultUserName,
                    roomWithEvents
                )
                val expected = defaultResult.copy(
                    room = roomWithEvents,
                    preciseToTime = events[0].startDateTime,
                    limit = BookingDuration.MIN_15.ordinal)

                assert(result == expected)
            }
        }
    }
}