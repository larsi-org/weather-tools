package org.larsi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Covers Metar.parse, the METAR text into store-ready values. The METARs are real (GeoNames,
 *  NOAA's GHCNh and AWC). No network or database involved. */
class MetarTest
{
	@Test
	fun `parse reads a full METAR, the remark's T group and SLP included`() {
		// KROC, GeoNames 2026-11-24 (its own fields: humidity 53, clouds "overcast")
		val v = Metar.parse("KROC 241754Z 33010KT 10SM OVC035 03/M06 A3050 RMK AO2 SLP338 T00281056 10033 21006 50007")
		assertEquals(mapOf(0 to "2.8", 1 to "-5.6", 2 to "54", 3 to "1033.8", 4 to "330", 5 to "5.1", 6 to "10"), v)
	}

	@Test
	fun `parse takes the most covered cloud layer, not the lowest`() {
		// GeoNames called these "broken clouds", "few clouds" and "broken clouds" (2026-11-24)
		assertEquals("10", Metar.parse("KSEA 241753Z 10014G25KT 10SM BKN075 BKN095 OVC120 12/00 A2982 RMK AO2 SLP105 T01220000")[6])
		assertEquals("10", Metar.parse("KMIA 241753Z 36005KT 10SM FEW027 BKN060 OVC140 24/21 A2997 RMK AO2 SLP148 T02390211")[6])
		assertEquals("6", Metar.parse("KORD 241651Z 35007KT 10SM BKN031 BKN250 03/M04 A3056 RMK AO2 SLP356 T00331044")[6])
		assertEquals("2", Metar.parse("KDEN 241753Z 21010KT 10SM FEW090 FEW200 08/M09 A3028 RMK AO2 SLP252 T00831089")[6])
		assertEquals("4", Metar.parse("KSAN 241751Z 18003KT 10SM SCT200 19/07 A3019 RMK AO2 SLP224 T01940067")[6])
	}

	@Test
	fun `parse reads clear sky as 0 and leaves clouds out when the METAR has no cloud group`() {
		assertEquals("0", Metar.parse("KTUS 241753Z 12017G26KT 10SM CLR 18/M02 A3020 RMK AO2 SLP208 T01781022")[6])
		assertEquals("0", Metar.parse("EDDF 241750Z 24008KT CAVOK 09/02 Q1021")[6])
		assertNull(Metar.parse("KXYZ 241750Z 24008KT 10SM 09/02 A3001 RMK AO2")[6])
	}

	@Test
	fun `parse reads vertical visibility as overcast and M temperatures as negative`() {
		// From NOAA's GHCNh REM column, KROC 2026-01-01 00:54 (snow and freezing fog)
		val v = Metar.parse("KROC 010054Z 25010KT 1/2SM R04/5000VP6000FT SN FZFG VV009 M04/M04 A2949 RMK AO2 SLP998 SNINCR 1/6 P0006 T10441044")
		assertEquals("-4.4", v[0])
		assertEquals("-4.4", v[1])
		assertEquals("100", v[2])
		assertEquals("999.8", v[3])  // SLP998: below 500 is 10xx.x, from 500 9xx.x
		assertEquals("10", v[6])
	}

	@Test
	fun `parse falls back to whole degrees without a T group, and leaves out what is missing`() {
		val v = Metar.parse("KXYZ 241750Z VRB03KT 10SM SCT040 03/M06 A3001 RMK AO2 SLPNO")
		assertEquals("3.0", v[0])
		assertEquals("-6.0", v[1])
		assertEquals("52", v[2])
		assertNull(v[3])  // SLPNO
		assertNull(v[4])  // variable wind: no direction
		assertEquals("1.5", v[5])
	}

	@Test
	fun `parse gives a calm wind a speed of 0 and no direction, and reads MPS`() {
		val calm = Metar.parse("KPHX 241751Z 00000KT 10SM FEW250 19/02 A3019 RMK AO2")
		assertNull(calm[4])
		assertEquals("0.0", calm[5])
		assertEquals("6.0", Metar.parse("UUEE 241750Z 27006MPS 9999 BKN020 M02/M05 Q1015")[5])
	}

	@Test
	fun `parse rounds wind speed as NOAA does, to hundredths and then tenths`() {
		assertEquals("8.8", Metar.parse("KROC 092054Z 21017G39KT 10SM SCT035 14/09 A2966")[5])  // 8.745 m/s
		assertEquals("16.0", Metar.parse("KROC 092154Z 25031G48KT 10SM BKN040 11/04 A2977")[5])  // 15.947 m/s
	}

	@Test
	fun `parse ignores cloud words in the remarks`() {
		assertNull(Metar.parse("KXYZ 241750Z 24008KT 10SM 20/10 A3001 RMK AO2 FEW020 CB DSNT W")[6])
	}

	@Test
	fun `parse reads past a leading report type, as AWC sends it`() {
		// AWC rawOb, KROC 2026-11-11 05:54
		val v = Metar.parse("METAR KROC 110554Z 24005KT 10SM FEW070 04/03 A2996 RMK AO2 SLP149 T00390028 10122 20039 50001 $")
		assertEquals(mapOf(0 to "3.9", 1 to "2.8", 2 to "93", 3 to "1014.9", 4 to "240", 5 to "2.6", 6 to "2"), v)
	}
}
