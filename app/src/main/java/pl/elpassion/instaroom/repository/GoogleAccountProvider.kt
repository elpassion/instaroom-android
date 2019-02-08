package pl.elpassion.instaroom.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn

class GoogleAccountProvider(private val context: Context) {

    fun userGoogleAccount() = GoogleSignIn.getLastSignedInAccount(context)
}