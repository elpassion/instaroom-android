package pl.elpassion.instaroom.dashboard

import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxrelay2.PublishRelay
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.runner.RunWith
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import pl.elpassion.instaroom.booking.*
import pl.elpassion.instaroom.calendar.CalendarRefresher
import pl.elpassion.instaroom.kalendar.BookingEvent
import pl.elpassion.instaroom.kalendar.Event
import pl.elpassion.instaroom.kalendar.Room
import pl.elpassion.instaroom.repository.UserRepository
import pl.elpassion.instaroom.smokk
import pl.elpassion.instaroom.util.executeTasksInstantly
import pl.mareklangiewicz.uspek.USpekRunner
import pl.mareklangiewicz.uspek.eq
import pl.mareklangiewicz.uspek.o
import pl.mareklangiewicz.uspek.uspek
import java.net.UnknownHostException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.sign


@RunWith(USpekRunner::class)
class DashboardModelTest {

    init {
        executeTasksInstantly()
    }

    @InternalCoroutinesApi
    @Test
    fun dashboardModelTests() {

        uspek {

            val actionS = PublishRelay.create<DashboardAction>()
            val stateD = MutableLiveData<DashboardState>()
            val roomListD = MutableLiveData<DashboardRoomList>()
            val refreshingD = MutableLiveData<DashboardRefreshing>()
            val userRepository = mock<UserRepository>()
            whenever(userRepository.userName).then { defaultUserName }

            val bookingFlow = smokk<BookingValues, BookingEvent?>()
            val summaryFlow = smokk<Event, Room, Unit>()
            val signOut = smokk<Unit>()
            val deleteEvent = smokk<String, Unit>()
            val getRooms = smokk<List<Room>>()
            val bookRoom = smokk<BookingEvent, Event?>()
            val initializeBookingVariables =
                mock<(userName: String?, room: Room, currentTime: ZonedDateTime) -> BookingValues?>()
            val refreshCalendar = smokk<Unit>()

            val stateObs = stateD.test()
            val roomListObs = roomListD.test()
            val refreshingObs = refreshingD.test()

            val zoneId = ZoneId.systemDefault()
            val instant = Instant.from(ZonedDateTime.of(2000, 1, 1, 12, 0, 0, 0, zoneId))
            val clock = Clock.fixed(instant, zoneId)

            "On dashboardModelFlow" o {

                val mainJob = GlobalScope.launch(Dispatchers.Unconfined) {
                    runDashboardFlow(
                        actionS,
                        stateD,
                        roomListD,
                        refreshingD,
                        userRepository,
                        { bookingValues -> bookingFlow.invoke(bookingValues) },
                        { event, room -> summaryFlow.invoke(event, room) },
                        signOut::invoke,
                        getRooms::invoke,
                        bookRoom::invoke,
                        deleteEvent::invoke,
                        initializeBookingVariables,
                        refreshCalendar::invoke,
                        clock
                    )
                }

                "start loading rooms" o {

                    "set refreshing state" o { refreshingObs.assertValue(DashboardRefreshing(true)) }
                    "start getRooms" o { getRooms.invocations eq 1 }

                    "On getRooms success" o {
                        val fetchedRoomList = listOf(emptyRoom)
                        getRooms.resume(fetchedRoomList)

                        "set room list" o {
                            roomListObs.assertValue(
                                DashboardRoomList(
                                    fetchedRoomList
                                )
                            )
                        }
                        "set refreshed state" o {
                            refreshingObs.assertValue(
                                DashboardRefreshing(
                                    false
                                )
                            )
                        }

                        "On user actions after refresh" o {

                            "On select refresh rooms" o {
                                actionS.accept(DashboardAction.RefreshRooms)

                                "getRooms is invoked second time" o { getRooms.invocations eq 2 }
                                //otherwise it works as in other tests
                            }

                            "On select sign out" o {
                                actionS.accept(DashboardAction.SelectSignOut)

                                "clear room list" o {
                                    roomListObs.assertValue(
                                        DashboardRoomList(
                                            emptyList()
                                        )
                                    )
                                }
                                "stated set to default" o { stateObs.assertValue(DashboardState.Default) }
                                "signOut is active" o { signOut.invocations eq 1 }

                                "On signOut success" o {
                                    signOut.resume(Unit)

                                    "signOut is completed" o { signOut.completeInvocations eq 1 }
                                    "flow is completed" o { mainJob.isCompleted }
                                }
                            }

                            "On ShowBookingDetails" o {

                                "runs initialize bookingValues" o {
                                    actionS.accept(DashboardAction.ShowBookingDetails(emptyRoom))
                                    verify(initializeBookingVariables).invoke(
                                        userRepository.userName,
                                        emptyRoom,
                                        ZonedDateTime.now(clock)
                                    )
                                }

                                "On initializer of bookingValues returns null" o {
                                    whenever(
                                        initializeBookingVariables(
                                            userRepository.userName,
                                            emptyRoom,
                                            ZonedDateTime.now(clock)
                                        )
                                    ).thenReturn(null)
                                    actionS.accept(DashboardAction.ShowBookingDetails(emptyRoom))


                                    "set error state with expected message" o {
                                        stateObs.assertValue(DashboardState.Error("Booking is unavailable in this room at the moment..."))
                                    }
                                }

                                "On initializeBookingValues returns correct values" o {
                                    whenever(
                                        initializeBookingVariables(
                                            userRepository.userName,
                                            emptyRoom,
                                            ZonedDateTime.now(clock)
                                        )
                                    ).thenReturn(testBookingValues)
                                    actionS.accept(DashboardAction.ShowBookingDetails(emptyRoom))

                                    "set state to showing booking details" o {
                                        stateObs.assertValue(
                                            DashboardState.BookingDetailsState
                                        )
                                    }
                                    "booking flow is active" o { bookingFlow.invocations.size eq 1 }

                                    "On booking flow returns null" o {
                                        bookingFlow.resume(null)

                                        "booking room is not invoked" o {
                                            println("invocations = ${bookRoom.invocations}")
                                            bookRoom.invocations.size eq 0
                                        }
                                    }

                                    "On booking flow returns BookingEvent" o {
                                        val bookingEvent = mock<BookingEvent>()
                                        bookingFlow.resume(bookingEvent)

                                        "booking flow is completed" o { bookingFlow.completeInvocations eq 1 }

                                        "set state to booking in progress" o {
                                            stateObs.assertValue(
                                                DashboardState.BookingInProgressState
                                            )
                                        }

                                        "book room is active" o { bookRoom.invocations.size eq 1 }

                                        "On book room return valid event" o {
                                            val newEvent = mock<Event>()
                                            bookRoom.resume(newEvent)

                                            "set state to booking success" o {
                                                stateObs.assertValue(
                                                    DashboardState.BookingSuccessState
                                                )
                                            }

                                            "summary flow is active" o { summaryFlow.invocations.size eq 1 }

                                            "On summary flow finished" o {
                                                summaryFlow.resume(Unit)

                                                "state set to default" o {
                                                    stateObs.assertValue(
                                                        DashboardState.Default
                                                    )
                                                }
                                                "start getRooms" o { getRooms.invocations eq 2 }
                                            }
                                        }

                                        "On book room error throws UnknownHostException" o {
                                            bookRoom.resumeWithException(UnknownHostException())

                                            "send error message" o {
                                                stateObs.assertValue(
                                                    DashboardState.Error("Network exception...")
                                                )
                                            }

                                            "summary flow was NOT started" o { summaryFlow.invocations.size eq 0 }

                                        }

                                        "On book room return null" o {
                                            bookRoom.resume(null)

                                            "send error message" o {
                                                stateObs.assertValue(
                                                    DashboardState.Error("Booking error...")
                                                )
                                            }

                                            "summary flow was NOT started" o { summaryFlow.invocations.size eq 0 }

                                        }
                                    }
                                }
                            }

                            "On delete event" o {
                                val eventId = "1"
                                actionS.accept(DashboardAction.DeleteEvent(eventId))

                                "set state to booking progress" o { stateObs.assertValue(DashboardState.BookingInProgressState) }

                                "delete event is run with correct arg" o { deleteEvent.invocations eq listOf(eventId) }

                                "On delete event success" o {
                                    deleteEvent.resume(Unit)

                                    "run calendar refresh" o { refreshCalendar.invocations eq 1 }
                                    "state is set to default" o { stateObs.assertValue(DashboardState.Default) }
                                    "getRooms is invoked second time" o { getRooms.invocations eq 2 }
                                }
                            }

                        }

                    }

                    "On getRooms failure with UnknownHostException" o {
                        getRooms.resumeWithException(UnknownHostException())

                        "set error state with expected message" o { stateObs.assertValue(DashboardState.Error("Network exception...")) }
                        "set refreshed state" o {refreshingObs.assertValue(DashboardRefreshing(false))}
                    }

                    "On getRooms failure with other exception" o {
                        getRooms.resumeWithException(RuntimeException())

                        "mainJob is cancelled" o { mainJob.isCancelled eq true }
                    }

                    "On user actions during refreshing" o {

                        "On select refresh rooms" o {
                            actionS.accept(DashboardAction.RefreshRooms)

                            "current getRooms is cancelled" o { getRooms.cancelInvocations eq 1 }
                            "current getRooms is invoked second time" o { getRooms.invocations eq 2 }
                            //otherwise it works as in other tests
                        }

                        "On select sign out" o {
                            actionS.accept(DashboardAction.SelectSignOut)

                            "clear room list" o { roomListObs.assertValue(
                                DashboardRoomList(
                                    emptyList())
                            )}
                            "stated set to default" o { stateObs.assertValue(DashboardState.Default) }
                            "signOut is active" o { signOut.invocations eq 1}

                            "On signOut success" o {
                                signOut.resume(Unit)

                                "signOut is completed" o { signOut.completeInvocations eq 1}
                                "current getRooms is cancelled" o { getRooms.cancelInvocations eq 1}
                                "flow is completed" o { mainJob.isCompleted }
                            }
                        }

                    }

                }

            }
        }
//    init {
//        val getToken = mock<suspend () -> String?>()
//        GlobalScope.runDashboar   dFlow(
//            mock(),
//            mock(),
//            mock(),
//            mock(),
//            getToken
//        )
//        verifyBlocking(getToken) { invoke() }
//    }
    }
}
