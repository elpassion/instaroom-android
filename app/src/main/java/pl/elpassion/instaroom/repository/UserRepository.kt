package pl.elpassion.instaroom.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface UserRepository {

    var userEmail: String?
    var userPhotoUrl: String?
    var userName: String?

    fun saveData()
}