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
 * Covers GeoNames2's pure parsing (parseMetar, parseObservation) --
 * no network or database involved. The response bodies below are real captures from
 * `http://api.geonames.org/weatherIcaoJSON?username=larsi&ICAO=<icao>`, kept as permanent
 * regression anchors; the METARs in the parseMetar tests are real too (GeoNames and NOAA's GHCNh).
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
	fun `parseMetar reads a full METAR, the remark's T group and SLP included`() {
		// KROC, GeoNames 2026-11-24 (its own fields: humidity 53, clouds "overcast")
		val v = GeoNames2.parseMetar("KROC 241754Z 33010KT 10SM OVC035 03/M06 A3050 RMK AO2 SLP338 T00281056 10033 21006 50007")
		assertEquals(mapOf(0 to "2.8", 1 to "-5.6", 2 to "54", 3 to "1033.8", 4 to "330", 5 to "5.1", 6 to "10"), v)
	}

	@Test
	fun `parseMetar takes the most covered cloud layer, not the lowest`() {
		// GeoNames called these "broken clouds", "few clouds" and "broken clouds" (2026-11-24)
		assertEquals("10", GeoNames2.parseMetar("KSEA 241753Z 10014G25KT 10SM BKN075 BKN095 OVC120 12/00 A2982 RMK AO2 SLP105 T01220000")[6])
		assertEquals("10", GeoNames2.parseMetar("KMIA 241753Z 36005KT 10SM FEW027 BKN060 OVC140 24/21 A2997 RMK AO2 SLP148 T02390211")[6])
		assertEquals("6", GeoNames2.parseMetar("KORD 241651Z 35007KT 10SM BKN031 BKN250 03/M04 A3056 RMK AO2 SLP356 T00331044")[6])
		assertEquals("2", GeoNames2.parseMetar("KDEN 241753Z 21010KT 10SM FEW090 FEW200 08/M09 A3028 RMK AO2 SLP252 T00831089")[6])
		assertEquals("4", GeoNames2.parseMetar("KSAN 241751Z 18003KT 10SM SCT200 19/07 A3019 RMK AO2 SLP224 T01940067")[6])
	}

	@Test
	fun `parseMetar reads clear sky as 0 and leaves clouds out when the METAR has no cloud group`() {
		assertEquals("0", GeoNames2.parseMetar("KTUS 241753Z 12017G26KT 10SM CLR 18/M02 A3020 RMK AO2 SLP208 T01781022")[6])
		assertEquals("0", GeoNames2.parseMetar("EDDF 241750Z 24008KT CAVOK 09/02 Q1021")[6])
		assertNull(GeoNames2.parseMetar("KXYZ 241750Z 24008KT 10SM 09/02 A3001 RMK AO2")[6])
	}

	@Test
	fun `parseMetar reads vertical visibility as overcast and M temperatures as negative`() {
		// From NOAA's GHCNh REM column, KROC 2026-01-01 00:54 (snow and freezing fog)
		val v = GeoNames2.parseMetar("KROC 010054Z 25010KT 1/2SM R04/5000VP6000FT SN FZFG VV009 M04/M04 A2949 RMK AO2 SLP998 SNINCR 1/6 P0006 T10441044")
		assertEquals("-4.4", v[0])
		assertEquals("-4.4", v[1])
		assertEquals("100", v[2])
		assertEquals("999.8", v[3])  // SLP998: below 500 is 10xx.x, from 500 9xx.x
		assertEquals("10", v[6])
	}

	@Test
	fun `parseMetar falls back to whole degrees without a T group, and leaves out what is missing`() {
		val v = GeoNames2.parseMetar("KXYZ 241750Z VRB03KT 10SM SCT040 03/M06 A3001 RMK AO2 SLPNO")
		assertEquals("3.0", v[0])
		assertEquals("-6.0", v[1])
		assertEquals("52", v[2])
		assertNull(v[3])  // SLPNO
		assertNull(v[4])  // variable wind: no direction
		assertEquals("1.5", v[5])
	}

	@Test
	fun `parseMetar gives a calm wind a speed of 0 and no direction, and reads MPS`() {
		val calm = GeoNames2.parseMetar("KPHX 241751Z 00000KT 10SM FEW250 19/02 A3019 RMK AO2")
		assertNull(calm[4])
		assertEquals("0.0", calm[5])
		assertEquals("6.0", GeoNames2.parseMetar("UUEE 241750Z 27006MPS 9999 BKN020 M02/M05 Q1015")[5])
	}

	@Test
	fun `parseMetar rounds wind speed as NOAA does, to hundredths and then tenths`() {
		assertEquals("8.8", GeoNames2.parseMetar("KROC 092054Z 21017G39KT 10SM SCT035 14/09 A2966")[5])  // 8.745 m/s
		assertEquals("16.0", GeoNames2.parseMetar("KROC 092154Z 25031G48KT 10SM BKN040 11/04 A2977")[5])  // 15.947 m/s
	}

	@Test
	fun `parseMetar ignores cloud words in the remarks`() {
		assertNull(GeoNames2.parseMetar("KXYZ 241750Z 24008KT 10SM 20/10 A3001 RMK AO2 FEW020 CB DSNT W")[6])
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
		assertEquals("25.0", result.values[0]) // temperature, T group
		assertEquals("17.8", result.values[1]) // dew point
		assertEquals("64", result.values[2]) // humidity, computed
		assertEquals("1015.7", result.values[3]) // sea level pressure, SLP157
		assertEquals("190", result.values[4]) // wind direction
		assertEquals("4.6", result.values[5]) // wind speed, knots -> m/s
		assertEquals("6", result.values[6]) // clouds: FEW060 SCT200 BKN250 -> 6 (GeoNames said "few clouds")
	}

	@Test
	fun `parseObservation leaves out the wind direction of a variable wind`() {
		// Captured 2026-09-12, KDEN under VRB03KT (variable wind).
		val json = """{"weatherObservation":{"elevation":1640,"lng":-104.6732648,"observation":"KDEN 121653Z VRB03KT 10SM FEW070 FEW120 BKN220 18/07 A3021 RMK AO2 SLP154 T01830067","ICAO":"KDEN","countryCode":"US","clouds":"few clouds","dewPoint":"6.7","cloudsCode":"FEW","datetime":"2026-09-12 16:53:00","seaLevelPressure":1015.4,"temperature":"18.3","humidity":46,"stationName":"Denver, Denver International Airport","weatherCondition":"n/a","windSpeed":"03","lat":39.8589112}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.Success

		assertNull(result.values[4]) // wind direction
		assertEquals(6, result.values.size) // every other sensor still present
	}

	@Test
	fun `parseObservation reads a clear-sky observation's CLR as 0`() {
		// Captured 2026-09-12, KTPH reporting CLR (GeoNames: no cloudsCode key at all, "clouds":"n/a").
		val json = """{"weatherObservation":{"elevation":1652,"lng":-117.0864655,"observation":"KTPH 121656Z AUTO 15005KT 10SM CLR 22/M04 A3013 RMK AO2 SLP123 T02221044","ICAO":"KTPH","countryCode":"US","clouds":"n/a","dewPoint":"-4.4","datetime":"2026-09-12 16:56:00","seaLevelPressure":1012.3,"temperature":"22.2","humidity":16,"stationName":"Tonopah, Tonopah Airport","weatherCondition":"n/a","windDirection":150,"windSpeed":"05","lat":38.0599341}}"""

		val result = parse(json) as GeoNames2.GeoNamesResult.Success

		assertEquals("0", result.values[6])
	}
}
