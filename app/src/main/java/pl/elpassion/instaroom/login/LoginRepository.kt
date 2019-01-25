package pl.elpassion.instaroom.login

interface LoginRepository {
    var googleToken: String?
    var tokenExpirationTimestamp: String?

    suspend fun getToken(): String

    val isTokenValid: Boolean

}
