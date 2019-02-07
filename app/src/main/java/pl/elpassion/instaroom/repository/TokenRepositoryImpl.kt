package pl.elpassion.instaroom.repository

import android.app.Application
import android.preference.PreferenceManager
import com.elpassion.android.commons.sharedpreferences.asProperty
import com.elpassion.android.commons.sharedpreferences.createSharedPrefs
import com.elpassion.sharedpreferences.moshiadapter.moshiConverterAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.ZonedDateTime
import java.lang.Exception

class TokenRepositoryImpl(application: Application, private val tokenRequester: GoogleApiWrapper) :
    TokenRepository {

    private val sharedPreferencesProvider =
        { PreferenceManager.getDefaultSharedPreferences(application) }
    private val moshi = Moshi.Builder()
        .add(ZonedDateTimeJsonAdapter)
        .build()

    private val jsonAdapter = moshiConverterAdapter<TokenData>(moshi)
    private val repository = createSharedPrefs(sharedPreferencesProvider, jsonAdapter)

    override var tokenData: TokenData? by repository.asProperty(TOKEN_DATA)

    override val isTokenValid: Boolean
        get() = tokenData?.let { (_, tokenExpirationDate) ->
            tokenExpirationDate.isAfter(ZonedDateTime.now())
        } ?: false


    override suspend fun getToken(): String = withContext(Dispatchers.IO) {
        if (!isTokenValid) refreshToken()
        tokenData?.googleToken ?: ""
    }

    override fun refreshToken() {
        val refreshedToken = tokenRequester.refreshToken()
        if (refreshedToken != null) {
            tokenData = TokenData(refreshedToken, expirationDate())
        } else {
            tokenData = null
        }
    }

    private fun expirationDate() = ZonedDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES)

    companion object {
        private const val EXPIRATION_TIME_IN_MINUTES = 59L
        private const val TOKEN_DATA = "token data"
    }
}