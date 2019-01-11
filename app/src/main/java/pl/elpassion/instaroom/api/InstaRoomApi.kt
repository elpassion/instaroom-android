package pl.elpassion.instaroom.api

import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface InstaRoomApi {

    @GET("/rooms")
    fun getRooms(@Header("AccessToken") accessToken: String): Deferred<RoomsResponse>

    @POST("/book")
    fun bookRoom(
        @Header("AccessToken") accessToken: String,
        @Header("CalendarId") roomCalendarId: String
    ): Deferred<Unit>
}
