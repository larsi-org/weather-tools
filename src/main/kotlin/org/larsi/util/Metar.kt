package org.larsi.util

/**
 * Reads a METAR's text into the values `log` stores, the same way NOAA's GHCNh archive reads it.
 * Both live harvesters store from the raw METAR: GeoNames2 (GeoNames' `observation` field, whose
 * own interpreted fields are lossy: its `clouds` names only the lowest layer, and "n/a" means both
 * clear sky and no cloud group) and AviationWeather (AWC's `rawOb`). A leading report type
 * ("METAR KROC ...", as AWC sends it) is read past like the station and time.
 */
object Metar
{
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
	fun parse(metar: String): Map<Int, String> {
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
}
