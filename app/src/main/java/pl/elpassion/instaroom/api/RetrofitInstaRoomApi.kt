package pl.elpassion.instaroom.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstaRoomApi : InstaRoomApi {

    private val moshi = Moshi.Builder()
        .add(ZonedDateTimeJsonAdapter)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pacific-lowlands-76710.herokuapp.com")
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(InstaRoomApi::class.java)

//    override fun getRooms(accessToken: String): Deferred<RoomsResponse> =
//        service.getRooms(accessToken)
//
//    override fun bookRoom(accessToken: String, roomCalendarId: String): Deferred<Unit> =
//        service.bookRoom(accessToken, roomCalendarId)
}
