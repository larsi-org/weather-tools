package org.larsi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import org.larsi.util.Icao

/** Covers Ghcnh's pure parsing (station ids, quality codes, clouds, one PSV row) and the runs a
 *  load deletes; no file, network or database involved. */
class GhcnhTest
{
	private val header = listOf(
		"STATION", "DATE",
		"temperature", "temperature_Quality_Code", "temperature_Report_Type",
		"dew_point_temperature", "dew_point_temperature_Quality_Code", "dew_point_temperature_Report_Type",
		"relative_humidity", "relative_humidity_Quality_Code", "relative_humidity_Report_Type",
		"sea_level_pressure", "sea_level_pressure_Quality_Code", "sea_level_pressure_Report_Type",
		"wind_direction", "wind_direction_Quality_Code", "wind_direction_Report_Type",
		"wind_speed", "wind_speed_Quality_Code", "wind_speed_Report_Type",
		"sky_cover_summation_1", "sky_cover_summation_1_Report_Type",
		"sky_cover_summation_2", "sky_cover_summation_2_Report_Type")
	private val col = header.withIndex().associate { (i, name) -> name to i }

	private fun row(vararg pairs: Pair<String, String>): List<String> {
		val m = pairs.toMap()
		return header.map { m[it] ?: "" }
	}

	private fun metar(vararg overrides: Pair<String, String>) = row(
		"STATION" to "USW00014768", "DATE" to "2024-01-03T15:54:00",
		"temperature" to "1.7", "temperature_Quality_Code" to "5", "temperature_Report_Type" to "FM15",
		"dew_point_temperature" to "-1.1", "dew_point_temperature_Quality_Code" to "5", "dew_point_temperature_Report_Type" to "FM15",
		"relative_humidity" to "82", "relative_humidity_Report_Type" to "FM15",
		"sea_level_pressure" to "1016.0", "sea_level_pressure_Quality_Code" to "5", "sea_level_pressure_Report_Type" to "FM15",
		"wind_direction" to "230", "wind_direction_Quality_Code" to "5", "wind_direction_Report_Type" to "FM15",
		"wind_speed" to "3.1", "wind_speed_Quality_Code" to "5", "wind_speed_Report_Type" to "FM15",
		"sky_cover_summation_1" to "FEW:02;", "sky_cover_summation_1_Report_Type" to "FM15",
		"sky_cover_summation_2" to "BKN:07;", "sky_cover_summation_2_Report_Type" to "FM15",
		*overrides)

	@Test
	fun stationIdIsUsw000PlusWban() {
		assertEquals("USW00014768", Ghcnh.stationId(Icao.KROC))
	}

	@Test
	fun qualityCodes() {
		for (good in listOf("", "0", "1", "4", "5", "9", "A", "U", "R")) assertTrue(Ghcnh.goodQuality(good), good)
		for (bad in listOf("2", "3", "6", "7", "s", "c", "z")) assertFalse(Ghcnh.goodQuality(bad), bad)
	}

	@Test
	fun cloudsTakeTheHighestLayer() {
		assertEquals(0, Ghcnh.cloudCode(listOf("CLR:00;")))
		assertEquals(2, Ghcnh.cloudCode(listOf("FEW:02;")))
		assertEquals(4, Ghcnh.cloudCode(listOf("FEW:02;", "SCT:04;")))
		assertEquals(6, Ghcnh.cloudCode(listOf("SCT:04;", "BKN:07;")))
		assertEquals(10, Ghcnh.cloudCode(listOf("BKN:07;", "OVC:08;")))
		assertEquals(10, Ghcnh.cloudCode(listOf("VV:09;")))
		assertNull(Ghcnh.cloudCode(emptyList()))
	}

	@Test
	fun epochIsUtc() {
		assertEquals(1704297240, Ghcnh.parseEpoch("2024-01-03T15:54:00"))
	}

	@Test
	fun metarRowGivesAllSevenChannels() {
		val o = Ghcnh.parseRow(metar(), col)!!
		assertEquals(1704297240, o.epoch)
		assertEquals(mapOf(0 to "1.7", 1 to "-1.1", 2 to "82", 3 to "1016.0", 4 to "230", 5 to "3.1", 6 to "6"), o.values)
	}

	@Test
	fun nonMetarRowsAreSkipped() {
		assertNull(Ghcnh.parseRow(metar("temperature_Report_Type" to "FM12"), col))
		assertNull(Ghcnh.parseRow(row("DATE" to "2026-01-01T00:05:00"), col)) // a 5-minute precipitation row
	}

	@Test
	fun calmOrVariableWindHasNoDirection() {
		val o = Ghcnh.parseRow(metar("wind_direction" to "999", "wind_speed" to "0.0"), col)!!
		assertFalse(4 in o.values)
		assertEquals("0.0", o.values[5])
	}

	@Test
	fun failedOrMissingValuesAreDropped() {
		val o = Ghcnh.parseRow(metar("sea_level_pressure" to "", "dew_point_temperature_Quality_Code" to "s",
			"relative_humidity_Report_Type" to "FM16"), col)!!
		assertEquals(setOf(0, 4, 5, 6), o.values.keys)
	}

	@Test
	fun hourlyMetarsAreOneRun() {
		assertEquals(listOf(0..3 * 3600), Ghcnh.runs(listOf(0, 3600, 7200, 3 * 3600)))
	}

	@Test
	fun aMissingHourSplitsTheRuns() {
		// 1:00 missing: 0:00 and 2:00 are 7200 apart, so the hour between keeps what the DB has
		assertEquals(listOf(0..0, 7200..10800), Ghcnh.runs(listOf(0, 7200, 10800)))
	}

	@Test
	fun aSpecialOrLateMetarStaysInTheRun() {
		// 0:54, 1:30 (FM15 off the usual minute), 2:54: neighbors within MAX_STEP
		assertEquals(listOf(3240..10440), Ghcnh.runs(listOf(3240, 5400, 10440)))
	}

	@Test
	fun noEpochsNoRuns() {
		assertEquals(emptyList<IntRange>(), Ghcnh.runs(emptyList()))
		assertEquals(listOf(5..5), Ghcnh.runs(listOf(5)))
	}

	@Test
	fun `hourly keeps the hour's latest METAR, or the station's own minute`() {
		val h = 1_800_000_000 / 3600 * 3600
		fun o(min: Int, hour: Int = 0) = Ghcnh.Observation(h + hour * 3600 + min * 60, emptyMap())
		val twenty = listOf(o(15), o(35), o(55), o(48, 1), o(53, 1))   // 20-minute METARs; an extra before :53
		assertEquals(listOf(o(55), o(53, 1)), Ghcnh.hourly(twenty))
		val kcys = listOf(o(53), o(55), o(55, 1))                       // second instrument at :55
		assertEquals(listOf(o(53), o(55, 1)), Ghcnh.hourly(kcys, 53))   // :53 when the hour has it
	}
}
