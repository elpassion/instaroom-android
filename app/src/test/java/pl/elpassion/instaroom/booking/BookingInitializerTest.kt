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

        "initialize booking variables (except time)" - {

            val defaultUserName = "User"
            val defaultEvents = listOf(
                eventWithTime("12:00", "12:15"),
                eventWithTime("12:30", "13:00"),
                eventWithTime("13:00", "15:00")
            )

            val roomWithEvents = emptyRoom.copy(events = defaultEvents)

            val initializedValues = initializeBookingVariables(
                defaultUserName,
                roomWithEvents,
                initTime
            )

            "hint is username with suffix" {
                assert(initializedValues.hint == "User's booking")
            }

            "title is empty" {
                assert(initializedValues.title.isEmpty())
            }

            "booking types available" - {

                "quick" {
                    assert(initializedValues.quickAvailable)
                }

                "precise" {
                    assert(initializedValues.preciseAvailable)
                }
            }

            "default type is quick" {
                assert(initializedValues.isPrecise == false)
            }

            "room didn't change" {
                assert(initializedValues.room == roomWithEvents)
            }
        }

        "initialize booking variables with events with no breaks" - {

            val defaultUserName = "User"
            val defaultEvents = listOf(
                eventWithTime("12:00", "12:15"),
                eventWithTime("12:15", "13:00"),
                eventWithTime("13:00", "15:00")
            )

            val roomWithEvents = emptyRoom.copy(events = defaultEvents)

            val initializedValues = initializeBookingVariables(
                defaultUserName,
                roomWithEvents,
                initTime
            )

            "quick booking is unavailable" {
                assert(initializedValues.quickAvailable == false)
            }

            "precise booking is unavailable" {
                assert(initializedValues.preciseAvailable == false)
            }
        }

        "initialize booking variables with events with only precise break" - {

            val defaultUserName = "User"
            val defaultEvents = listOf(
                eventWithTime("12:00", "12:15"),
                eventWithTime("12:25", "13:00"),
                eventWithTime("13:00", "15:00")
            )

            val roomWithEvents = emptyRoom.copy(events = defaultEvents)

            val initializedValues = initializeBookingVariables(
                defaultUserName,
                roomWithEvents,
                initTime
            )

            "quick booking is unavailable" {
                assert(initializedValues.quickAvailable == false)
            }

            "precise booking is available" {
                assert(initializedValues.preciseAvailable == true)
            }

            "default booking type is precise" {
                assert(initializedValues.isPrecise == true)
            }

            "precise from time as expected" {
                assert(initializedValues.preciseFromTime == getTime("12:15"))
            }

            "precise to time as expected" {
                assert(initializedValues.preciseToTime == getTime("12:25"))
            }
        }

        "initialize booking variables with events with quick and precise break" - {

            val defaultUserName = "User"

            val events = listOf(
                    eventWithTime("12:00", "12:15"),
                    eventWithTime("12:45", "13:00"),
                    eventWithTime("13:00", "15:00")
                )

            val roomWithEvents = emptyRoom.copy(events = events)
            val initializedValues = initializeBookingVariables(
                defaultUserName,
                roomWithEvents,
                initTime
            )

            "quick booking is available" {
                assert(initializedValues.quickAvailable == true)
            }

            "precise booking is available" {
                assert(initializedValues.preciseAvailable == true)
            }

            "default booking type is quick" {
                assert(initializedValues.isPrecise == false)
            }

            "precise from time as expected" {
                assert(initializedValues.preciseFromTime == getTime("12:15"))
            }

            "precise to time as expected" {
                assert(initializedValues.preciseToTime == getTime("12:45"))
            }

            "quick from time as expected" {
                assert(initializedValues.quickFromTime == getTime("12:15"))
            }

            "quick limit index as expected" {
                println("limit = ${initializedValues.limit}")
                assert(initializedValues.limit == BookingDuration.MIN_30.ordinal)
            }
        }
    }
}