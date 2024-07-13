package com.etfrogers.myenergiklient

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
internal data class MeterPoint(
    @SerialName("imp") val imported : Int = 0, // the 4 quantities are in some unit converted to W below
    @SerialName("exp") val export : Int = 0,
    @SerialName("h1d") val diverted : Int = 0,
    @SerialName("h1b") val boosted : Int = 0,
    val pect1: Int = 0,
    val nect1: Int = 0,
    val pect2: Int = 0,
    val nect2: Int = 0,
    val hsk: Int = 0,
    @SerialName("v1") val voltage: Float = 0f, // volts * 10
    @SerialName("frq") val frequency: Float = 0f, // appears to be Hz*100
    @SerialName("yr") private val year: Int,
    @SerialName("mon") private val month: Int,
    @SerialName("dom") private val dayOfMonth: Int,
    @SerialName("dow") private val dayOfWeekStr: String,
    @SerialName("hr") private val hour: Int = 0,
    @SerialName("min") private val minute: Int = 0,
    var timezone: TimeZone? = null
){
    val timestamp: LocalDateTime
        get() = compensateTimestamp(
            LocalDateTime(year, month, dayOfMonth, hour, minute, 0),
            timezone
        )
}

data class DetailHistory(
    val timestamps: List<LocalDateTime>,
    val voltage: List<Float>,
    val frequency: List<Float>,
    val importPower: List<Float>,
    val exportPower: List<Float>,
    val divertPower: List<Float>,
    val boostPower: List<Float>,
    val totalPower: List<Float>,
){
    companion object {
        internal fun fromMeters(meters: List<MeterPoint>): DetailHistory{
            val timestamps = mutableListOf<LocalDateTime>()
            val voltage = mutableListOf<Float>()
            val frequency = mutableListOf<Float>()
            val importPower = mutableListOf<Float>()
            val exportPower = mutableListOf<Float>()
            val divertPower = mutableListOf<Float>()
            val boostPower = mutableListOf<Float>()
            val totalPower= mutableListOf<Float>()

            meters.forEach {
                val volts = it.voltage/10f
                timestamps.add(it.timestamp)
                voltage.add(volts)
                frequency.add(it.frequency/100f)
                val toWatts = 4/volts // taken from python code, in turn taken from mec
                importPower.add(it.imported * toWatts)
                exportPower.add(it.export * toWatts)
                divertPower.add(it.diverted * toWatts)
                boostPower.add(it.boosted * toWatts)
                totalPower.add((it.diverted + it.boosted) * toWatts)
            }

            return DetailHistory(
                timestamps,
                voltage,
                frequency,
                importPower,
                exportPower,
                divertPower,
                boostPower,
                totalPower,
            )
        }
    }
}

data class HourData(
    val timestamps: List<LocalDateTime>,
    val importReading: List<Int>,
    val exportReading: List<Int>,
    val divertReading: List<Int>,
    val boostReading: List<Int>,
) {
    companion object {
        internal fun fromMeters(meters: List<MeterPoint>): HourData {
            val timestamps = mutableListOf<LocalDateTime>()
            val importReading = mutableListOf<Int>()
            val exportReading = mutableListOf<Int>()
            val divertReading = mutableListOf<Int>()
            val boostReading = mutableListOf<Int>()

            meters.forEach {
                timestamps.add(it.timestamp)
                importReading.add(it.imported)
                exportReading.add(it.export)
                divertReading.add(it.diverted)
                boostReading.add(it.boosted)
            }
            return HourData(
                timestamps, importReading, exportReading, divertReading, boostReading
            )
        }
    }
}

internal fun compensateTimestamp(timestamp: LocalDateTime, timezone: TimeZone?): LocalDateTime {
    return if (timezone == null) {
        timestamp
    } else {
        timestamp.toInstant(TimeZone.UTC).toLocalDateTime(timezone)
    }
}

internal fun decodeHistory(sno: String, data: String, timezone: TimeZone): List<MeterPoint> {
    val json = Json.parseToJsonElement(data)
    val metersJsonObj = json.jsonObject
    if (metersJsonObj.size != 1){
        throw SerializationException("Only implemented for one data set per return")
    }
    val metersJsonData = metersJsonObj["U$sno"] ?: throw SerializationException("Expected serial number not found")
    val meters = Json.decodeFromJsonElement<List<MeterPoint>>(metersJsonData)
    meters.map { it.timezone = timezone }
    return meters
}