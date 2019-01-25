package pl.elpassion.instaroom.repository

import org.threeten.bp.ZonedDateTime

interface TokenRepository {

    var tokenData: TokenData?

    suspend fun getToken(): String?

    val isTokenValid: Boolean

}