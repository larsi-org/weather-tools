package org.larsi

import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.google.gson.JsonObject
import com.google.gson.JsonParser

import org.larsi.util.MeteredDataConnector
import org.larsi.util.Ntfy

object GeoNames2
{
	/** GeoNames `weatherObservation` JSON keys, indexed by the sensor ID each is stored under
	 *  (0-6, see CLAUDE.md's sensor ID table). A key missing from the response (e.g.
	 *  `windDirection` during variable wind) just means that sensor isn't written this run. */
	val SENSOR_KEYS = listOf(
		"temperature",      // 0
		"dewPoint",         // 1
		"humidity",         // 2
		"seaLevelPressure", // 3
		"windDirection",    // 4
		"windSpeed",        // 5
		"clouds"            // 6
	)

	private const val WIND_SPEED_SENSOR_ID = 5
	private const val CLOUDS_SENSOR_ID = 6

	private val knotsToMPS = 1852.0 / 3600.0
	private val WIND_SPEED_FORMAT: NumberFormat = DecimalFormat("0.0")

	/** Converts one raw GeoNames field value into what actually gets stored for the given sensor
	 *  ID: wind speed (knots -> m/s) and clouds (free text -> a coarse coverage number) need
	 *  translating; every other sensor is stored as-is. */
	@JvmStatic
	fun transformValue(sensorID: Int, value: String): String = when (sensorID) {
		WIND_SPEED_SENSOR_ID -> WIND_SPEED_FORMAT.format(knotsToMPS * value.toFloat())
		CLOUDS_SENSOR_ID -> when (val lower = value.lowercase()) {
			"n/a" -> "0"
			"few clouds" -> "2"
			"scattered clouds" -> "4"
			"broken clouds" -> "7"
			"overcast" -> "10"
			else -> lower
		}
		else -> value
	}

	/** The outcome of parsing one station's GeoNames response. */
	sealed class GeoNamesResult
	{
		/** GeoNames has no `weatherObservation` at all for this ICAO (e.g. an unrecognized code,
		 *  or the JSON `status` error shape). */
		object NoObservation : GeoNamesResult()

		/** A `weatherObservation` is present but has no `datetime` -- the station itself is down. */
		object Down : GeoNamesResult()

		/** A usable observation: `epoch` is its UTC timestamp, `values` maps sensor ID -> already
		 *  store-ready value, for whichever [SENSOR_KEYS] were actually present in the response. */
		data class Success(val epoch: Int, val values: Map<Int, String>) : GeoNamesResult()
	}

	/** Pure parsing of a `weatherIcaoJSON` response body -- no network or DB involved. */
	@JvmStatic
	fun parseObservation(response: JsonObject, formatter: DateFormat): GeoNamesResult {
		val obs = response.getAsJsonObject("weatherObservation") ?: return GeoNamesResult.NoObservation
		val datetime = obs.get("datetime")?.asString ?: return GeoNamesResult.Down

		val epoch = (formatter.parse(datetime).time / 1000).toInt()
		val values = SENSOR_KEYS.withIndex().mapNotNull { (sensorID, key) ->
			obs.get(key)?.asString?.let { sensorID to transformValue(sensorID, it) }
		}.toMap()

		return GeoNamesResult.Success(epoch, values)
	}

	/** Fetches and parses one ICAO's current observation from GeoNames' JSON weather endpoint. */
	@JvmStatic
	fun fetch(icao: String): JsonObject {
		val connection = URL("http://api.geonames.org/weatherIcaoJSON?username=larsi&ICAO=$icao").openConnection() as HttpURLConnection
		connection.connectTimeout = 20_000
		connection.readTimeout = 20_000
		return connection.inputStream.use { JsonParser.parseReader(InputStreamReader(it, Charsets.UTF_8)) }.asJsonObject
	}

	@JvmStatic
	fun check() {
		val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		formatter.timeZone = TimeZone.getTimeZone("GMT")

		try {
			MeteredDataConnector("larsi-weather2").use { md ->
				/** Get ICAO entries */
				val entries = md.queryList("SELECT prefix FROM location;") { it.getString(1) }

				// check all entries
				for ((i, prefix) in entries.withIndex()) {
					val icao = prefix.uppercase() // GeoNames' API and log lines want the conventional uppercase ICAO code; DB writes use prefix (lowercase) directly
					val header = "${String.format("%4s", i + 1)} / ${String.format("%4s", entries.size)} - $icao:"

					try {
						md.clearBatch()

						var updated = false
						var epoch = 0

						when (val result = parseObservation(fetch(icao), formatter)) {
							is GeoNamesResult.NoObservation -> println("$header no observation available")
							is GeoNamesResult.Down -> println("$header down")
							is GeoNamesResult.Success -> {
								if (result.epoch <= md.getMaxDateTimeLog2(prefix, "0,1,2,3,4,5,6")) {
									println("$header up to date")
								} else {
									println("$header updating...")
									updated = true
									epoch = result.epoch
									for ((sensorID, value) in result.values)
										md.addBatch(md.insertLogSQL2(prefix, epoch, sensorID, value))
								}
							}
						}

						md.executeBatch()

						if (updated) {
							md.executeUpdate(md.updateLastEpochSQL2(prefix, epoch))
						}
					} catch (e: Exception) {
						println("$header did not receive / could not parse data!")
					}
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			Ntfy.publish(Zeus.NTFY_TOPIC, "GeoNames DB Error", "DB connection was refused (larsi-weather2)", priority = "urgent", tags = "rotating_light")
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		check()
		println("Done")
	}

}
