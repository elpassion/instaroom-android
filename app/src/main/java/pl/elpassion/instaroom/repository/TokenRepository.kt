package pl.elpassion.instaroom.repository

interface TokenRepository {

    var tokenData: TokenData?
    val isUserSignedIn: Boolean
    val isTokenValid: Boolean
    suspend fun getToken(): String?


}