package org.larsi

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

import org.larsi.AviationWeather.Report

/** Covers AviationWeather's pure parts: AWC's JSON, which METARs are new, and the paging past
 *  AWC's 400-report cap, against a fake AWC (no network or database involved). The JSON is a real
 *  answer of 2026-11-27, cut to the fields read. */
class AviationWeatherTest
{
	/** A fake AWC: one station's reports, answering like the real one (the newest MAX_REPORTS in the
	 *  hours before end, newest first), and keeping each request's arguments. */
	private class FakeAwc(val reports: List<Report>, val now: Int) : AviationWeather.Fetcher
	{
		val requests = mutableListOf<Triple<List<String>, Int, Int?>>()

		override fun fetch(ids: List<String>, hours: Int, end: Int?): List<Report> {
			requests += Triple(ids, hours, end)
			val to = end ?: now
			return reports.filter { it.icao in ids && it.epoch <= to && it.epoch > to - hours * 3600 }
				.sortedByDescending { it.epoch }.take(AviationWeather.MAX_REPORTS)
		}
	}

	private val now = 1_800_000_000

	/** A METAR every hour and a SPECI every second hour, `hours` back from now. */
	private fun station(icao: String, hours: Int): List<Report> = (0 until hours).flatMap { h ->
		val t = now - h * 3600 - 360
		listOf(Report(icao, t, "METAR", "METAR $icao")) + if (h % 2 == 0) listOf(Report(icao, t - 1200, "SPECI", "SPECI $icao")) else emptyList()
	}

	@Test
	fun `parseReports reads AWC's JSON and skips incomplete entries`() {
		val body = """[{"icaoId":"KROC","receiptTime":"2026-11-27T20:04:16.000Z","obsTime":1795809240,"reportTime":"2026-11-27T20:00:00.000Z","metarType":"METAR","rawOb":"METAR KROC 271954Z 20005KT 10SM FEW019 SCT024 BKN031 08/06 A2933 RMK AO2 SLP939 T00780056"},
			{"icaoId":"KROC","obsTime":1795809660,"metarType":"SPECI","rawOb":"SPECI KROC 272001Z 21006KT 10SM SCT017 BKN022 OVC027 08/06 A2934 RMK AO2 T00780056"},
			{"icaoId":"KXYZ","obsTime":null,"metarType":"METAR","rawOb":"METAR KXYZ"}]"""
		val r = AviationWeather.parseReports(body)
		assertEquals(2, r.size)
		assertEquals(Report("KROC", 1795809240, "METAR", "METAR KROC 271954Z 20005KT 10SM FEW019 SCT024 BKN031 08/06 A2933 RMK AO2 SLP939 T00780056"), r[0])
		assertEquals("SPECI", r[1].type)
		assertEquals(emptyList<Report>(), AviationWeather.parseReports(""))
	}

	@Test
	fun `newMetars keeps the station's METARs after the last reading, oldest first, once each`() {
		val r = listOf(Report("KROC", 300, "METAR", "c"), Report("KROC", 200, "METAR", "b"), Report("KROC", 200, "METAR", "b2"),
			Report("KROC", 250, "SPECI", "s"), Report("KROC", 100, "METAR", "a"), Report("KBUF", 300, "METAR", "x"))
		assertEquals(listOf(200, 300), AviationWeather.newMetars(r, "KROC", 100).map { it.epoch })
	}

	@Test
	fun `since takes one request when the answer is not full`() {
		val awc = FakeAwc(station("KROC", 720), now)
		val budget = AviationWeather.Budget(10)
		val r = AviationWeather.since("KROC", now - 10 * 3600, now, budget, awc)!!
		assertEquals(1, awc.requests.size)
		assertEquals(11, awc.requests[0].second)   // 10 hours, plus one
		assertEquals(null, awc.requests[0].third)
		assertEquals(10, AviationWeather.newMetars(r, "KROC", now - 10 * 3600).size)
		assertEquals(9, budget.left)
	}

	@Test
	fun `since pages back past the cap until it reaches the last reading`() {
		// 30 days of METARs and SPECIs: about 1,080 reports, three answers' worth
		val awc = FakeAwc(station("KROC", 720), now)
		val last = now - 720 * 3600
		val r = AviationWeather.since("KROC", last, now, AviationWeather.Budget(10), awc)!!
		assertEquals(3, awc.requests.size)
		assertEquals(720, AviationWeather.newMetars(r, "KROC", last).size)   // every hour, none lost
		assertEquals(awc.requests[0].third, null)
		assertEquals(r.filter { it.epoch < awc.requests[1].third!! }.isNotEmpty(), true)
	}

	@Test
	fun `since starts at most 30 days back and gives null when the budget runs out`() {
		val awc = FakeAwc(station("KROC", 720), now)
		assertNull(AviationWeather.since("KROC", 0, now, AviationWeather.Budget(2), awc))
		assertEquals(720, awc.requests[0].second)
	}

	@Test
	fun `a station AWC has nothing for costs one request`() {
		val awc = FakeAwc(emptyList(), now)
		val budget = AviationWeather.Budget(10)
		assertEquals(emptyList<Report>(), AviationWeather.since("KPBI", 0, now, budget, awc))
		assertEquals(9, budget.left)
	}

	@Test
	fun `group splits a full answer until each part fits`() {
		val ids = (1..200).map { "K%03d".format(it) }
		val awc = FakeAwc(ids.flatMap { station(it, 4) }, now)   // 6 reports each in 4 hours: 1,200
		val r = AviationWeather.group(ids, 4, AviationWeather.Budget(), awc)
		assertEquals(1200, r.size)
		assertEquals(listOf(200, 100, 50, 50, 100, 50, 50), awc.requests.map { it.first.size })
	}

	@Test
	fun `between fetches a past stretch, paging back from its end`() {
		val awc = FakeAwc(station("KROC", 720), now)
		val from = now - 400 * 3600; val to = now - 100 * 3600
		val r = AviationWeather.between("KROC", from, to, now, AviationWeather.Budget(10), awc)!!
		assertEquals(to, awc.requests[0].third)
		assertEquals(300, AviationWeather.newMetars(r, "KROC", from - 1).size)   // every hour from..to
		assertEquals(true, r.all { it.epoch in from..to })
	}

	@Test
	fun `uncovered leaves out METARs within 30 minutes of a stored reading`() {
		val m = listOf(10_000, 20_000, 30_000, 40_000).map { Report("KROC", it, "METAR", "") }
		val stored = listOf(20_000 + 1800, 30_000 - 1801, 50_000)   // 20,000 covered at the edge, 30,000 not
		assertEquals(listOf(10_000, 30_000, 40_000), AviationWeather.uncovered(m, stored).map { it.epoch })
		assertEquals(4, AviationWeather.uncovered(m, emptyList()).size)
	}

	@Test
	fun `daysBack stays an hour inside AWC's 30 days`() {
		assertEquals(now - 7 * 86400, AviationWeather.daysBack(7, now))
		assertEquals(now - 720 * 3600 + 3600, AviationWeather.daysBack(30, now))
	}
}
