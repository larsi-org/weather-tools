package org.larsi

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.google.gson.JsonParser

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Covers GeoNames2's pure `weatherIcaoJSON` parsing logic (transformValue/parseObservation) --
 * no network or database involved. The response bodies below are real captures from
 * `http://api.geonames.org/weatherIcaoJSON?username=larsi&ICAO=<icao>`, kept as permanent
 * regression anchors for the XML -> JSON endpoint switch.
 */
class GeoNames2Test
{
	private fun gmtFormatter(): DateFormat {
		val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		formatter.timeZone = TimeZone.getTimeZone("GMT")
		return formatter
	}

	private fun parse(json: String) =
		GeoNames2.parseObservation(JsonParser.parseString(json).asJsonObject, gmtFormatter())

	@Test
	fun `transformValue converts wind speed from knots to meters per second`() {
		assertEquals("4.6", GeoNames2.transformValue(5, "09"))
		assertEquals("0.0", GeoNames2.transformValue(5, "00"))
	}

	@Test
	fun `transformValue maps each known cloud description to its coverage number`() {
		assertEquals("0", GeoNames2.transformValue(6, "n/a"))
		assertEquals("2", GeoNames2.transformValue(6, "few clouds"))
		assertEquals("4", GeoNames2.transformValue(6, "scattered clouds"))
		assertEquals("7", GeoNames2.transformValue(6, "broken clouds"))
		assertEquals("10", GeoNames2.transformValue(6, "overcast"))
	}

	@Test
	fun `transformValue lowercases and passes through an unrecognized cloud description`() {
		assertEquals("unknown value", GeoNames2.transformValue(6, "Unknown Value"))
	}

	@Test
	fun `transformValue passes every other sensor through unchanged`() {
		assertEquals("17.8", GeoNames2.transformValue(1, "17.8")) // dew point
		assertEquals("1015.7", GeoNames2.transformValue(3, "1015.7")) // sea level pressure
	}

	@Test
	fun `parseObservation captures the status code and message for an unknown-ICAO response`() {
		val json = """{"status":{"message":"no observation found","value":15}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.NoObservation

		assertEquals(15, result.statusCode)
		assertEquals("no observation found", result.statusMessage)
		assertFalse(result.isRateLimited)
	}

	@Test
	fun `parseObservation flags GeoNames status 18 as a daily rate limit`() {
		val json = """{"status":{"message":"the daily limit of 10000 credits for larsi has been exceeded. Please throttle your requests or use the commercial service.","value":18}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.NoObservation

		assertEquals(18, result.statusCode)
		assertTrue(result.isRateLimited)
	}

	@Test
	fun `parseObservation flags GeoNames status 19 as an hourly rate limit`() {
		val json = """{"status":{"message":"the hourly limit of 1000 credits for larsi has been exceeded.","value":19}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.NoObservation

		assertEquals(19, result.statusCode)
		assertTrue(result.isRateLimited)
	}

	@Test
	fun `parseObservation tolerates a response with neither weatherObservation nor status`() {
		val result = parse("{}") as GeoNames2.GeoNamesResult.NoObservation

		assertNull(result.statusCode)
		assertNull(result.statusMessage)
		assertFalse(result.isRateLimited)
	}

	@Test
	fun `parseObservation returns Down when weatherObservation has no datetime`() {
		val json = """{"weatherObservation":{"ICAO":"KROC","temperature":"25"}}"""
		assertEquals(GeoNames2.GeoNamesResult.Down, parse(json))
	}

	@Test
	fun `parseObservation parses a real KROC response into epoch and per-sensor values`() {
		// Captured 2026-09-12: http://api.geonames.org/weatherIcaoJSON?username=larsi&ICAO=KROC
		val json = """{"weatherObservation":{"elevation":178,"lng":-77.672389,"observation":"KROC 121654Z 19009KT 10SM FEW060 SCT200 BKN250 25/18 A2999 RMK AO2 SLP157 T02500178","ICAO":"KROC","countryCode":"US","clouds":"few clouds","dewPoint":"17.8","cloudsCode":"FEW","datetime":"2026-09-12 16:54:00","seaLevelPressure":1015.7,"temperature":"25","humidity":64,"stationName":"Rochester, Greater Rochester International Airport","weatherCondition":"n/a","windDirection":190,"windSpeed":"09","lat":43.118866}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.Success

		assertEquals(1789232040, result.epoch) // 2026-09-12 16:54:00 UTC
		assertEquals("25", result.values[0]) // temperature
		assertEquals("17.8", result.values[1]) // dew point
		assertEquals("64", result.values[2]) // humidity
		assertEquals("1015.7", result.values[3]) // sea level pressure
		assertEquals("190", result.values[4]) // wind direction
		assertEquals("4.6", result.values[5]) // wind speed, knots -> m/s
		assertEquals("2", result.values[6]) // clouds, "few clouds" -> 2
	}

	@Test
	fun `parseObservation omits a sensor whose key is missing (windDirection during variable wind)`() {
		// Captured 2026-09-12, KDEN under VRB03KT (variable wind): GeoNames omits windDirection
		// entirely rather than reporting a placeholder value.
		val json = """{"weatherObservation":{"elevation":1640,"lng":-104.6732648,"observation":"KDEN 121653Z VRB03KT 10SM FEW070 FEW120 BKN220 18/07 A3021 RMK AO2 SLP154 T01830067","ICAO":"KDEN","countryCode":"US","clouds":"few clouds","dewPoint":"6.7","cloudsCode":"FEW","datetime":"2026-09-12 16:53:00","seaLevelPressure":1015.4,"temperature":"18.3","humidity":46,"stationName":"Denver, Denver International Airport","weatherCondition":"n/a","windSpeed":"03","lat":39.8589112}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.Success

		assertNull(result.values[4]) // wind direction
		assertEquals(6, result.values.size) // every other sensor still present
	}

	@Test
	fun `parseObservation maps a clear-sky observation's n_a clouds to 0`() {
		// Captured 2026-09-12, KTPH reporting CLR: no cloudsCode key at all, "clouds":"n/a".
		val json = """{"weatherObservation":{"elevation":1652,"lng":-117.0864655,"observation":"KTPH 121656Z AUTO 15005KT 10SM CLR 22/M04 A3013 RMK AO2 SLP123 T02221044","ICAO":"KTPH","countryCode":"US","clouds":"n/a","dewPoint":"-4.4","datetime":"2026-09-12 16:56:00","seaLevelPressure":1012.3,"temperature":"22.2","humidity":16,"stationName":"Tonopah, Tonopah Airport","weatherCondition":"n/a","windDirection":150,"windSpeed":"05","lat":38.0599341}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.Success

		assertEquals("0", result.values[6])
	}
}
