package pl.elpassion.instaroom.repository

interface UserRepository {

    var userEmail: String?
    var userPhotoUrl: String?
    var userName: String?

    fun saveData()
}