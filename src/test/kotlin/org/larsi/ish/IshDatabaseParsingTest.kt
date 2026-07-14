package org.larsi.ish

import java.util.TimeZone

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Covers IshDatabase's pure fixed-width NOAA ISH parsing logic (getCDS/getMDS/getGF1) — no file
 * I/O, network, or database involved. IshDatabase is a Kotlin object with shared mutable state,
 * so [setGmt] re-pins the timezone main() normally sets before any real run, and the GF1 tests
 * set iREM_IndexOf explicitly rather than relying on process()'s side effect, so tests don't
 * depend on execution order.
 */
class IshDatabaseParsingTest
{
	@BeforeEach
	fun setGmt() {
		IshDatabase.formatter.timeZone = TimeZone.getTimeZone("GMT")
	}

	// Builds a Mandatory Data Section test record: 104 filler chars with the fields getMDS
	// actually reads (dir/spd/tempSign/temp/dewpSign/dewp/slp) overwritten at their real
	// fixed-width offsets. Defaults are all-sentinel ("no data").
	private fun mdsLine(
			dir: String = "999",
			spd: String = "9999",
			tempSign: String = "+",
			temp: String = "9999",
			dewpSign: String = "+",
			dewp: String = "9999",
			slp: String = "99999"
	): String {
		require(dir.length == 3 && spd.length == 4 && tempSign.length == 1 && temp.length == 4
				&& dewpSign.length == 1 && dewp.length == 4 && slp.length == 5)
		val sb = StringBuilder("0".repeat(104))
		sb.replace(60, 63, dir)
		sb.replace(65, 69, spd)
		sb.replace(87, 88, tempSign)
		sb.replace(88, 92, temp)
		sb.replace(93, 94, dewpSign)
		sb.replace(94, 98, dewp)
		sb.replace(99, 104, slp)
		return sb.toString()
	}

	// Builds a GF1 element ("GF1" + 2-digit sky-cover code + 21 filler chars = 26-char element)
	// optionally preceded by a REM marker, to test getGF1's REM-boundary check.
	private fun gf1Line(skc: String, withRemBefore: Boolean = false): String {
		require(skc.length == 2)
		val prefix = if (withRemBefore) "REMxxxxx" else "xxxxxxxx"
		return prefix + "GF1" + skc + "x".repeat(21)
	}

	private fun setRemIndexFor(line: String) {
		val idx = line.indexOf("REM")
		IshDatabase.iREM_IndexOf = if (idx == -1) 9999 else idx
	}

	@Test
	fun `getCDS parses a GMT timestamp to epoch seconds`() {
		IshDatabase.getCDS("0".repeat(15) + "202001010000" + "0".repeat(78))
		assertEquals(1577836800, IshDatabase.utc) // 2020-01-01 00:00:00 UTC

		IshDatabase.getCDS("0".repeat(15) + "202001010053" + "0".repeat(78))
		assertEquals(1577839980, IshDatabase.utc) // 2020-01-01 00:53:00 UTC
	}

	@Test
	fun `getMDS treats all sentinel values as missing`() {
		IshDatabase.getMDS(mdsLine())
		assertEquals("***", IshDatabase.sMDS_Dir)
		assertEquals("****", IshDatabase.sMDS_Spd)
		assertEquals("****", IshDatabase.sMDS_Temp)
		assertEquals("****", IshDatabase.sMDS_Dewp)
		assertEquals("******", IshDatabase.sMDS_Slp)
	}

	@Test
	fun `getMDS passes through a real wind direction unchanged`() {
		IshDatabase.getMDS(mdsLine(dir = "340"))
		assertEquals("340", IshDatabase.sMDS_Dir)
	}

	@Test
	fun `getMDS converts wind speed from tenths of meters per second`() {
		IshDatabase.getMDS(mdsLine(spd = "0021"))
		assertEquals("2.1", IshDatabase.sMDS_Spd)
	}

	@Test
	fun `getMDS converts a positive temperature from tenths of a degree`() {
		IshDatabase.getMDS(mdsLine(tempSign = "+", temp = "0233"))
		assertEquals("23.3", IshDatabase.sMDS_Temp)
	}

	@Test
	fun `getMDS applies the sign for a negative temperature`() {
		IshDatabase.getMDS(mdsLine(tempSign = "-", temp = "0050"))
		assertEquals("-5.0", IshDatabase.sMDS_Temp)
	}

	@Test
	fun `getMDS applies the sign for a negative dew point`() {
		IshDatabase.getMDS(mdsLine(dewpSign = "-", dewp = "0011"))
		assertEquals("-1.1", IshDatabase.sMDS_Dewp)
	}

	@Test
	fun `getMDS converts sea level pressure from tenths of a hectopascal`() {
		IshDatabase.getMDS(mdsLine(slp = "10171"))
		assertEquals("1017.1", IshDatabase.sMDS_Slp)
	}

	@Test
	fun `getGF1 defaults to missing when no GF1 element is present`() {
		val line = "xxxxxxxxxxxxxxxxxxxx"
		setRemIndexFor(line)
		IshDatabase.getGF1(line)
		assertEquals("**", IshDatabase.sGF1_Skc)
	}

	@Test
	fun `getGF1 treats sky-cover sentinel 99 as missing`() {
		val line = gf1Line(skc = "99")
		setRemIndexFor(line)
		IshDatabase.getGF1(line)
		assertEquals("**", IshDatabase.sGF1_Skc)
	}

	@Test
	fun `getGF1 passes through sky-cover codes below 8`() {
		val line = gf1Line(skc = "02")
		setRemIndexFor(line)
		IshDatabase.getGF1(line)
		assertEquals("2", IshDatabase.sGF1_Skc)
	}

	@Test
	fun `getGF1 maps sky-cover code 8 to 10 (obscured)`() {
		val line = gf1Line(skc = "08")
		setRemIndexFor(line)
		IshDatabase.getGF1(line)
		assertEquals("10", IshDatabase.sGF1_Skc)
	}

	@Test
	fun `getGF1 maps sky-cover codes above 8 to missing`() {
		val line = gf1Line(skc = "09")
		setRemIndexFor(line)
		IshDatabase.getGF1(line)
		assertEquals("**", IshDatabase.sGF1_Skc)
	}

	@Test
	fun `getGF1 ignores a GF1 element that appears after the REM marker`() {
		val line = gf1Line(skc = "02", withRemBefore = true)
		setRemIndexFor(line)
		IshDatabase.getGF1(line)
		assertEquals("**", IshDatabase.sGF1_Skc)
	}

	// Real records from KMIA (station 722020-12839, 2020-01-01), used earlier to verify the
	// IshDatabase/Ish2 simplification was behavior-preserving — kept here as permanent
	// regression anchors for process()'s end-to-end wiring of getCDS/getMDS/getGF1.
	@Test
	fun `process parses a real synoptic (SYN) record`() {
		val line = "0211722020128392020010100004+25750-080383FM-12+000599999V0203401N002112200019N016000199+02331+01671101711ADDGA1999+008001999GE19MSL   +99999+99999GF102991999999008001999999KA1120M+02721KA2120N+02111MA1999999101631MD1310121+9999REMSYN08072202 32566 23404 10233 20167 30163 40171 53012 92353 333 10272 20211 555 90100="

		IshDatabase.process(line)

		assertEquals(1577836800, IshDatabase.utc) // 2020-01-01 00:00:00 UTC
		assertEquals("23.3", IshDatabase.sMDS_Temp)
		assertEquals("16.7", IshDatabase.sMDS_Dewp)
		assertEquals("2", IshDatabase.sGF1_Skc)
		assertEquals("1017.1", IshDatabase.sMDS_Slp)
		assertEquals("340", IshDatabase.sMDS_Dir)
		assertEquals("2.1", IshDatabase.sMDS_Spd)
	}

	@Test
	fun `process parses a real calm-wind METAR record`() {
		val line = "0235722020128392020010100537+25788-080317FM-15+0009KMIA V0309999C000052200059N0160935N5+02285+01505101785ADDAA101000095GA1025+007625999GD11991+0076259GE19AGL   +99999+99999GF102995999999007621999999MA1101795101695REMMET09912/31/19 19:53:03 METAR KMIA 010053Z 00000KT 10SM FEW025 23/15 A3006 RMK AO2 SLP178 T02280150 (RDR)EQDD01      0ADE726"

		IshDatabase.process(line)

		assertEquals(1577839980, IshDatabase.utc) // 2020-01-01 00:53:00 UTC
		assertEquals("22.8", IshDatabase.sMDS_Temp)
		assertEquals("15.0", IshDatabase.sMDS_Dewp)
		assertEquals("2", IshDatabase.sGF1_Skc)
		assertEquals("1017.8", IshDatabase.sMDS_Slp)
		assertEquals("***", IshDatabase.sMDS_Dir) // calm wind (00000KT) reports as the 999 sentinel
		assertEquals("0.0", IshDatabase.sMDS_Spd)
	}
}
