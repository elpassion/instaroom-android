package pl.elpassion.instaroom.repository

import android.accounts.Account
import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import org.koin.android.ext.koin.androidApplication
import pl.elpassion.instaroom.R

class GoogleApiWrapper(private val context: Context) {

    fun refreshToken(): String? {
        return userGoogleAccount()?.account?.let(::getNewToken)
    }

    fun getEmail(): String? {
        return userGoogleAccount()?.email
    }

    fun getUserPhotoUrl(): Uri? {
        return userGoogleAccount()?.photoUrl
    }

    fun getUserName(): String {
        return userGoogleAccount()?.displayName ?: ""
    }

    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.resources.getString(R.string.server_client_id))
        .requestEmail()
        .requestScopes(Scope("profile"), Scope("https://www.googleapis.com/auth/calendar.events"))
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    private fun userGoogleAccount() = GoogleSignIn.getLastSignedInAccount(context)

    private fun getNewToken(account: Account)= GoogleAuthUtil.getToken(context, account, "oauth2:https://www.googleapis.com/auth/calendar.events")
}