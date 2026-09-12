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

	/** GeoNames webservice exception codes (https://www.geonames.org/export/webservice-exception.html)
	 *  for its two credit-rate-limit cases -- the only ones [GeoNamesResult.NoObservation] callers
	 *  need to single out from an ordinary "no data for this ICAO" status. */
	private const val DAILY_LIMIT_EXCEEDED = 18
	private const val HOURLY_LIMIT_EXCEEDED = 19

	/** The outcome of parsing one station's GeoNames response. */
	sealed class GeoNamesResult
	{
		/** GeoNames has no `weatherObservation` at all for this ICAO. `statusCode`/`statusMessage`
		 *  come from the JSON `status` object GeoNames sends in its place, when present -- e.g.
		 *  code 15 ("no observation found") for an ICAO it just has no data for, but also code 18
		 *  ("daily limit ... exceeded") or 19 ("hourly limit ... exceeded") when the account's
		 *  credit quota is exhausted. GeoNames returns those as an ordinary HTTP 200 with this
		 *  `status` body, not as a thrown exception, so without checking `statusCode` a rate-limit
		 *  lockout is indistinguishable from a station GeoNames simply has no data for. */
		data class NoObservation(val statusCode: Int?, val statusMessage: String?) : GeoNamesResult()
		{
			val isRateLimited: Boolean get() = statusCode == DAILY_LIMIT_EXCEEDED || statusCode == HOURLY_LIMIT_EXCEEDED
		}

		/** A `weatherObservation` is present but has no `datetime` -- the station itself is down. */
		object Down : GeoNamesResult()

		/** A usable observation: `epoch` is its UTC timestamp, `values` maps sensor ID -> already
		 *  store-ready value, for whichever [SENSOR_KEYS] were actually present in the response. */
		data class Success(val epoch: Int, val values: Map<Int, String>) : GeoNamesResult()
	}

	/** Pure parsing of a `weatherIcaoJSON` response body -- no network or DB involved. */
	@JvmStatic
	fun parseObservation(response: JsonObject, formatter: DateFormat): GeoNamesResult {
		val obs = response.getAsJsonObject("weatherObservation") ?: run {
			val status = response.getAsJsonObject("status")
			return GeoNamesResult.NoObservation(
				statusCode = status?.get("value")?.asInt,
				statusMessage = status?.get("message")?.asString
			)
		}
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

				// Tracks GeoNames rate-limit hits across the whole run, so a single ntfy alert
				// can go out once per run below instead of once per rate-limited station.
				var rateLimitedCount = 0
				var rateLimitedStatus: String? = null

				// check all entries
				for ((i, prefix) in entries.withIndex()) {
					val icao = prefix.uppercase() // GeoNames' API and log lines want the conventional uppercase ICAO code; DB writes use prefix (lowercase) directly
					val header = "${String.format("%4s", i + 1)} / ${String.format("%4s", entries.size)} - $icao:"

					try {
						md.clearBatch()

						var updated = false
						var epoch = 0

						when (val result = parseObservation(fetch(icao), formatter)) {
							is GeoNamesResult.NoObservation -> {
								val detail = result.statusMessage?.let { " (GeoNames status ${result.statusCode}: $it)" } ?: ""
								if (result.isRateLimited) {
									println("$header RATE LIMITED$detail")
									rateLimitedCount++
									rateLimitedStatus = rateLimitedStatus ?: "${result.statusCode}: ${result.statusMessage}"
								} else {
									println("$header no observation available$detail")
								}
							}
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

				if (rateLimitedCount > 0) {
					Ntfy.publish(Zeus.NTFY_TOPIC, "GeoNames Rate Limited",
						"$rateLimitedCount/${entries.size} stations hit GeoNames' rate limit this run (status $rateLimitedStatus)",
						priority = "high", tags = "warning")
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
