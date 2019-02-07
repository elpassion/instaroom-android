package pl.elpassion.instaroom.repository

interface TokenRepository {

    var tokenData: TokenData?

    val isUserSignedIn: Boolean
    get() = tokenData!=null

    val isTokenValid: Boolean

    suspend fun getToken(): String
    fun refreshToken()


}