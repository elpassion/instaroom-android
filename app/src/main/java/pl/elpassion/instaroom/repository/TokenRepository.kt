package pl.elpassion.instaroom.repository

interface TokenRepository {

    var tokenData: TokenData?

    suspend fun getToken(): String?

    val isTokenValid: Boolean

}