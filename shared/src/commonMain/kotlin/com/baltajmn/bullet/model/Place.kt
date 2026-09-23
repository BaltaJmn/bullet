package com.baltajmn.bullet.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Where an entry lives. In [Monthly] and [Future], [day] present is the line of the calendar;
 * absent, the list of tasks without a day (docs/tecnico.md 4.1).
 */
@Serializable(with = PlaceSerializer::class)
sealed interface Place {
    data class Daily(val date: LocalDate) : Place
    data class Monthly(val month: YearMonth, val day: Int? = null) : Place
    data class Future(val month: YearMonth, val day: Int? = null) : Place
    data class InCollection(val id: String) : Place
}

/**
 * Writes and reads [Place] as an object with exactly one place key, plus an optional [day] for
 * [Place.Monthly] and [Place.Future] (docs/tecnico.md 4.1):
 *
 * `{"daily": "2026-09-22"}`, `{"monthly": "2026-10"}`, `{"monthly": "2026-10", "day": 3}`,
 * `{"future": "2027-02", "day": 14}`, `{"collection": "c-1d2e7a40"}`.
 */
object PlaceSerializer : KSerializer<Place> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Place", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Place) {
        val json = encoder as? JsonEncoder ?: throw SerializationException("Place solo se serializa a JSON")
        val obj = buildJsonObject {
            when (value) {
                is Place.Daily -> put("daily", value.date.toString())
                is Place.Monthly -> {
                    put("monthly", value.month.toString())
                    value.day?.let { put("day", it) }
                }
                is Place.Future -> {
                    put("future", value.month.toString())
                    value.day?.let { put("day", it) }
                }
                is Place.InCollection -> put("collection", value.id)
            }
        }
        json.encodeJsonElement(obj)
    }

    override fun deserialize(decoder: Decoder): Place {
        val json = decoder as? JsonDecoder ?: throw SerializationException("Place solo se deserializa de JSON")
        val obj = json.decodeJsonElement() as? JsonObject ?: throw SerializationException("Place no es un objeto")
        val key = setOf("daily", "monthly", "future", "collection").filter { it in obj }.singleOrNull()
            ?: throw SerializationException("Place necesita exactamente una clave de lugar")
        val day = obj["day"]?.jsonPrimitive?.int
        if (day != null && key !in setOf("monthly", "future")) {
            throw SerializationException("day solo es válido en monthly y future")
        }
        return when (key) {
            "daily" -> Place.Daily(LocalDate.parse(obj.getValue("daily").jsonPrimitive.content))
            "monthly", "future" -> {
                val month = YearMonth.parse(obj.getValue(key).jsonPrimitive.content)
                if (day != null && day !in 1..month.numberOfDays) {
                    throw SerializationException("day $day no cabe en $month")
                }
                if (key == "monthly") Place.Monthly(month, day) else Place.Future(month, day)
            }
            else -> Place.InCollection(obj.getValue("collection").jsonPrimitive.content)
        }
    }
}
