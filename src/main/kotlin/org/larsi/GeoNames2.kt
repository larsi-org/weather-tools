package org.larsi

import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.google.gson.JsonObject
import com.google.gson.JsonParser

import org.larsi.util.MeteredDataConnector
import org.larsi.util.Ntfy

object GeoNames2
{
	/** The raw METAR GeoNames relays (its `observation` field) is the source of every stored
	 *  value: GeoNames' own interpreted fields are a lossy reading of the same text (its `clouds`
	 *  names only the lowest layer, and "n/a" means both clear sky and no cloud group), so the
	 *  METAR is parsed here, the same way NOAA's GHCNh archive reads it (see [parseMetar]).
	 *  Written into GeoNames2 for now; it moves to util/ once a second harvester needs it. */
	private val CLOUD_COVER = mapOf("FEW" to 2, "SCT" to 4, "BKN" to 6, "OVC" to 10, "VV" to 10)
	private val CLEAR_SKY = setOf("CLR", "SKC", "NCD", "NSC", "CAVOK")
	private val KNOTS_TO_MPS = 1852.0 / 3600.0

	private val WIND = Regex("""^(\d{3}|VRB)(\d{2,3})(?:G\d{2,3})?(KT|MPS)$""")
	private val BODY_TEMPERATURES = Regex("""^(M?\d{2})/(M?\d{2})?$""")
	private val CLOUD_LAYER = Regex("""^(FEW|SCT|BKN|OVC|VV)(\d{3}|///)""")
	private val REMARK_TEMPERATURES = Regex("""^T([01])(\d{3})(?:([01])(\d{3}))?$""")
	private val REMARK_SEA_LEVEL_PRESSURE = Regex("""^SLP(\d{3})$""")

	private fun tenths(value: Double): String = String.format(java.util.Locale.US, "%.1f", value).let { if (it == "-0.0") "0.0" else it }

	/** Relative humidity (%) from temperature and dew point (C), Bolton's (1980) saturation
	 *  vapor pressure over water, rounded half up, at most 100. Matches NOAA's derived GHCNh value
	 *  on 93% of kroc's 2026 METARs and is 1 lower on the rest (no standard formula matched NOAA's
	 *  exactly). */
	@JvmStatic
	fun relativeHumidity(temperature: Double, dewPoint: Double): Int {
		val rh = 100 * Math.exp(17.67 * dewPoint / (243.5 + dewPoint) - 17.67 * temperature / (243.5 + temperature))
		return minOf(100, Math.floor(rh + 0.5).toInt())
	}

	/** One METAR's values by sensor ID (0-6, see CLAUDE.md), store-ready; a value the METAR does
	 *  not carry is left out.
	 *  - 0 temperature, 1 dew point (C): the remark's T group in tenths (T00281056 -> 2.8, -5.6),
	 *    else the body's whole degrees (03/M06).
	 *  - 2 relative humidity (%): computed from those two ([relativeHumidity]).
	 *  - 3 sea level pressure (hPa): the remark's SLP group (SLP338 -> 1033.8, SLP998 -> 999.8);
	 *    SLPNO or none: left out.
	 *  - 4 wind direction (degrees), 5 wind speed (m/s, NOAA's rounding): dddssKT (or MPS), gusts ignored; a
	 *    variable (VRB) or calm (000) wind has a speed but no direction.
	 *  - 6 clouds: the most covered layer, i.e. the total cover (each METAR layer counts the ones
	 *    below it): FEW 2, SCT 4, BKN 6, OVC and VV (sky obscured) 10, CLR/SKC/NCD/NSC/CAVOK 0. No
	 *    cloud group: left out, not taken as clear.
	 *  Only the body (before RMK) is read for wind, temperatures and clouds, so remarks such as
	 *  "FEW CB" cannot pass for a layer. */
	@JvmStatic
	fun parseMetar(metar: String): Map<Int, String> {
		val tokens = metar.trim().split(Regex("""\s+"""))
		val rmk = tokens.indexOf("RMK").let { if (it < 0) tokens.size else it }
		val body = tokens.subList(0, rmk)
		val remarks = tokens.subList(rmk, tokens.size)
		val values = sortedMapOf<Int, String>()

		var temperature: Double? = null
		var dewPoint: Double? = null
		var cover: Int? = null
		for (token in body) {
			WIND.matchEntire(token)?.let { m ->
				// Rounded to hundredths first, then tenths, as NOAA's GHCNh does: 17 kt = 8.745 m/s ->
				// 8.75 -> 8.8 (one rounding would give 8.7).
				val speed = m.groupValues[2].toDouble() * (if (m.groupValues[3] == "KT") KNOTS_TO_MPS else 1.0)
				values[5] = tenths(Math.round(speed * 100) / 100.0)
				val direction = m.groupValues[1]
				if (direction != "VRB" && direction != "000") values[4] = direction.toInt().toString()
			}
			BODY_TEMPERATURES.matchEntire(token)?.let { m ->
				fun whole(s: String) = if (s.startsWith("M")) -s.substring(1).toDouble() else s.toDouble()
				temperature = whole(m.groupValues[1])
				dewPoint = m.groupValues[2].takeIf { it.isNotEmpty() }?.let { whole(it) }
			}
			CLOUD_LAYER.find(token)?.let { m -> cover = maxOf(cover ?: 0, CLOUD_COVER.getValue(m.groupValues[1])) }
			if (token in CLEAR_SKY) cover = maxOf(cover ?: 0, 0)
		}
		for (token in remarks) {
			REMARK_TEMPERATURES.matchEntire(token)?.let { m ->
				fun signed(sign: String, digits: String) = (if (sign == "1") -1 else 1) * digits.toInt() / 10.0
				temperature = signed(m.groupValues[1], m.groupValues[2])
				if (m.groupValues[3].isNotEmpty()) dewPoint = signed(m.groupValues[3], m.groupValues[4])
			}
			REMARK_SEA_LEVEL_PRESSURE.matchEntire(token)?.let { m ->
				val tenthsHpa = m.groupValues[1].toInt()
				values[3] = tenths((if (tenthsHpa < 500) 10000 + tenthsHpa else 9000 + tenthsHpa) / 10.0)
			}
		}

		temperature?.let { values[0] = tenths(it) }
		dewPoint?.let { values[1] = tenths(it) }
		val t = temperature
		val d = dewPoint
		if (t != null && d != null) values[2] = relativeHumidity(t, d).toString()
		cover?.let { values[6] = it.toString() }
		return values
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

		/** A `weatherObservation` is present but has no `datetime` or no METAR text (`observation`)
		 *  -- the station itself is down. */
		object Down : GeoNamesResult()

		/** A usable observation: `epoch` is its UTC timestamp, `values` maps sensor ID -> already
		 *  store-ready value, for whichever values its METAR carries ([parseMetar]). */
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
		val metar = obs.get("observation")?.asString ?: return GeoNamesResult.Down

		// The time is GeoNames' datetime: the METAR's own DDHHMMZ, with the year and month added.
		val epoch = (formatter.parse(datetime).time / 1000).toInt()
		return GeoNamesResult.Success(epoch, parseMetar(metar))
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
									// one statement for the whole observation, so readers never see
									// part of it (see insertLogRowsSQL2)
									if (result.values.isNotEmpty())
										md.addBatch(MeteredDataConnector.insertLogRowsSQL2(prefix, epoch, result.values))
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
