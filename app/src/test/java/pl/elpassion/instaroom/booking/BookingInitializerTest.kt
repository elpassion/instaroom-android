package pl.elpassion.instaroom.booking

import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec

import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.util.BookingDuration

class BookingInitializerTest : FreeSpec() {

    init {

        val initTime = getTime("12:00")

        "find first free booking event" - {

            "with at least 15 min break in between events" - {
                val events: List<Event> =
                    listOf(
                        eventWithTime("12:00", "12:15"),
                        eventWithTime("12:45", "13:00"),
                        eventWithTime("13:00", "15:00")
                    )

                "quick booking range returns valid range" {
                    val result = findFirstFreeQuickBookingTime(
                        events,
                        initTime
                    )
                    assert(result == eventTimeRange("12:15", "12:45"))
                }

                "precise booking range returns valid range" {
                    val result = findFirstFreePreciseBookingTime(
                        events,
                        initTime
                    )
                    assert(result == eventTimeRange("12:15", "12:45"))
                }
            }

            "with less than 15 min break in between events" - {
                val events: List<Event> =
                    listOf(
                        eventWithTime("12:00", "12:15"),
                        eventWithTime("12:25", "13:00"),
                        eventWithTime("13:00", "15:00")
                    )

                "quick booking range throws exception" {
                    shouldThrow<BookingUnavailableException> {
                        findFirstFreeQuickBookingTime(
                            events,
                            initTime
                        )
                    }
                }

                "precise booking range returns valid range" {
                    val result = findFirstFreePreciseBookingTime(
                        events,
                        initTime
                    )
                    assert(result == eventTimeRange("12:15", "12:25"))
                }
            }

            "with at least 15 min break before events" - {
                val events = listOf(
                    eventWithTime("12:30", "12:45"),
                    eventWithTime("12:45", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

                println("getTime = $initTime\nevents = $events")

                "quick booking range returns valid range" {
                    val result = findFirstFreeQuickBookingTime(
                        events,
                        initTime
                    )
                    assert(result == eventTimeRange("12:00", "12:30"))
                }

                "precise booking range returns valid range" {
                    val result = findFirstFreePreciseBookingTime(
                        events,
                        initTime
                    )
                    assert(result == eventTimeRange("12:00", "12:30"))
                }
            }

            "with less than 15 min break before events" - {
                val events = listOf(
                    eventWithTime("12:10", "12:25"),
                    eventWithTime("12:25", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

                "quick booking range throws exception" {
                    shouldThrow<BookingUnavailableException> {
                        findFirstFreeQuickBookingTime(
                            events,
                            initTime
                        )
                    }
                }

                "precise booking range returns valid range" {
                    val result = findFirstFreePreciseBookingTime(
                        events,
                        initTime
                    )
                    val expected = eventTimeRange("12:00", "12:10")
                    println("result = $result\nexpected = $expected")
                    assert(result == eventTimeRange("12:00", "12:10"))
                }
            }

            "with no breaks" - {
                val events = listOf(
                    eventWithTime("12:00", "12:15"),
                    eventWithTime("12:15", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

                "quick booking range throws exception" {
                    shouldThrow<BookingUnavailableException> {
                        findFirstFreeQuickBookingTime(
                            events,
                            initTime
                        )
                    }
                }

                "precise booking range throws exception" {
                    shouldThrow<BookingUnavailableException> {
                        findFirstFreePreciseBookingTime(
                            events,
                            initTime
                        )
                    }
                }
            }
        }

        "calculate quick booking limit index" - {

            "within <15 minutes range break returns -1" {
                val result = calculateQuickBookingLimitIndex(
                    initTime,
                    initTime.plusMinutes(10)
                )
                assert(result == -1)
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
            val defaultBookingValues = BookingValues(
                true,
                true,
                false,
                emptyRoom,
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
                val events = listOf(
                    eventWithTime("12:00", "12:15"),
                    eventWithTime("12:15", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

                val roomWithEvents = emptyRoom.copy(events = events)

                val result = initializeBookingVariables(
                    roomWithEvents,
                    initTime
                )

                val expected = defaultBookingValues.copy(
                    room = roomWithEvents,
                    quickAvailable = false,
                    preciseAvailable = false,
                    isPrecise = true
                    )

                assert(result == expected)
            }

            "with events with only precise break" {
                val events = listOf(
                    eventWithTime("12:00", "12:15"),
                    eventWithTime("12:25", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

                val roomWithEvents = emptyRoom.copy(events = events)

                val result = initializeBookingVariables(
                    roomWithEvents,
                    initTime
                )
                val expected = defaultBookingValues.copy(
                    quickAvailable = false,
                    isPrecise = true,
                    room = roomWithEvents,
                    preciseFromTime = getTime("12:15"),
                    preciseToTime = getTime("12:25")
                )

                assert(result == expected)
            }

            "with events with quick and precise break" {
                val events = listOf(
                    eventWithTime("12:00", "12:15"),
                    eventWithTime("12:44", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

                val roomWithEvents = emptyRoom.copy(events = events)
                val result = initializeBookingVariables(
                    defaultUserName,
                    roomWithEvents,
                    initTime
                )
                val expected = defaultBookingValues.copy(
                    room = roomWithEvents,
                    quickFromTime = getTime("12:15"),
                    limit = BookingDuration.MIN_15.ordinal,
                    preciseFromTime = getTime("12:15"),
                    preciseToTime = getTime("12:44")
                    )
                assert(result == expected)
            }
        }
    }
}