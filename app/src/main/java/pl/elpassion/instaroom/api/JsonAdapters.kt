@file:Suppress("unused")

package pl.elpassion.instaroom.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson
import org.threeten.bp.ZonedDateTime

object ZonedDateTimeJsonAdapter {
    @FromJson
    fun fromJson(reader: JsonReader): ZonedDateTime? = (reader.readJsonValue() as? String)?.let(ZonedDateTime::parse)

    @ToJson
    fun toJson(value: ZonedDateTime?): String? = value?.toString()
}
