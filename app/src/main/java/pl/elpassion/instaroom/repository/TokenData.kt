package pl.elpassion.instaroom.repository

import org.threeten.bp.ZonedDateTime

data class TokenData(var googleToken: String, var tokenExpirationDate: ZonedDateTime)
