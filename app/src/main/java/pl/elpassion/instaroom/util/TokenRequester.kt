package pl.elpassion.instaroom.util

import android.accounts.Account
import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import pl.elpassion.instaroom.login.LoginAction

class TokenRequester(private val context: Context) {

    fun refreshToken(): String? {
        val account = getLastSignedInAccount()
        return account?.let(::getNewToken)
    }

    private fun getLastSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context)?.account

    private fun getNewToken(account: Account)= GoogleAuthUtil.getToken(context, account, "oauth2:https://www.googleapis.com/auth/calendar.events")
}