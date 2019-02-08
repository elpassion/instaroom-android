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

class GoogleAccountProvider(private val context: Context) {

    fun userGoogleAccount() = GoogleSignIn.getLastSignedInAccount(context)
}