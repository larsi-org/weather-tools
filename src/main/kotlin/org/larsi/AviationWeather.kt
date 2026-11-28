package org.larsi

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.TimeZone

import com.google.gson.JsonParser

import org.larsi.util.Metar
import org.larsi.util.MeteredDataConnector
import org.larsi.util.Ntfy

/**
 * Live hourly METARs from NOAA's Aviation Weather Center data API, for every station in
 * `location`, into `log` (channels 0-6, read from the raw METAR by [Metar.parse], as GeoNames2
 * and NOAA's GHCNh archive read it).
 *
 *   java -jar aviationweather.jar [--dry-run]
 *   java -jar aviationweather.jar fill [--dry-run] <from> <to> [prefix ...]
 *
 * Jenkins runs it twice an hour. Each run reads every station's last stored reading and stores
 * the METARs after it, so a run missed (Jenkins or the network down) is made up by the next one,
 * as far back as AWC keeps reports (30 days):
 * - Stations stored within RECENT_HOURS are asked for together, GROUP_SIZE per request, the last
 *   RECENT_HOURS + 1 hours: about 6 requests for all 304.
 * - A station further behind is asked for alone, everything after its last reading. AWC answers
 *   at most MAX_REPORTS, the newest ones, so a full answer is followed by a request for the time
 *   before its oldest report, until the station's last reading is reached (30 days are about 3
 *   requests). A station is written only once its whole missing stretch is fetched: when the run's
 *   MAX_REQUESTS are used up first, it waits for the next run, so a cut-off run never leaves a hole.
 *   Least behind first, so one long outage does not hold up the rest; a station AWC has nothing
 *   for (HTTP 204) costs one request.
 * Only METARs (`metarType` METAR, the routine hourly report, usually :54) are stored, the series
 * GHCNh loads as FM15; SPECIs are left out. A METAR at or before the station's last reading is not
 * written (gaps further back are Ghcnh's to fill), and every insert is INSERT IGNORE, so a rerun
 * never doubles a reading. One INSERT per station and METAR (insertLogRowsSQL2), and
 * `location.last_epoch` set to the station's newest, as GeoNames2 does.
 *
 * AWC's rules (aviationweather.gov/data/api): 100 requests a minute (PAUSE_MS keeps a run at 60),
 * at most 400 reports per answer, a custom User-Agent, only documented query parameters (anything
 * else is HTTP 400), history up to 30 days. No key.
 *
 * fill: puts AWC's METARs from <from> to <to> (UTC, yyyy-MM-dd or yyyy-MM-ddTHH:mm, within the last
 * 30 days) into hours the DB has nothing for, for a hole another source left (NOAA's 2026-11-09 to
 * 11-12 outage): a METAR is written only when the station has no reading (channels 0-6) within
 * FILL_CLEARANCE of it, so what GHCNh or a harvester stored always stays. `location.last_epoch`
 * is not touched. All stations, or the prefixes given.
 *
 * --dry-run fetches and prints what would be stored, and writes nothing.
 */
object AviationWeather
{
	private const val BASE = "https://aviationweather.gov/api/data/metar"
	private const val USER_AGENT = "weather-tools/1.0"
	const val MAX_REPORTS = 400       // AWC's cap per answer: the newest are kept
	const val MAX_HOURS = 720         // AWC keeps 30 days
	const val RECENT_HOURS = 3        // a station stored this recently is asked for in a group
	const val GROUP_SIZE = 60         // stations per group request: 4 hours of METARs and SPECIs stay well under 400
	const val MAX_REQUESTS = 90       // per run
	private const val PAUSE_MS = 1000L
	private const val CHANNELS = "0,1,2,3,4,5,6"
	const val FILL_CLEARANCE = 1800   // fill: no METAR within this many seconds of a stored reading

	/** One report as AWC sends it: station, observation time (UTC seconds), METAR or SPECI, raw text. */
	data class Report(val icao: String, val epoch: Int, val type: String, val raw: String)

	/** Fetches the reports of `ids` for the `hours` before `end` (UTC seconds; null: now). */
	fun interface Fetcher
	{
		fun fetch(ids: List<String>, hours: Int, end: Int?): List<Report>
	}

	/** Counts a run's requests against MAX_REQUESTS. */
	class Budget(var left: Int = MAX_REQUESTS)

	/** AWC's JSON answer as reports; an entry without station, time, type or text is skipped. */
	@JvmStatic
	fun parseReports(body: String): List<Report> {
		if (body.isBlank()) return emptyList()
		return JsonParser.parseString(body).asJsonArray.mapNotNull { e ->
			val o = e.asJsonObject
			val icao = o.get("icaoId")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
			val epoch = o.get("obsTime")?.takeIf { !it.isJsonNull }?.asInt ?: return@mapNotNull null
			val type = o.get("metarType")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
			val raw = o.get("rawOb")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
			Report(icao, epoch, type, raw)
		}
	}

	/** The METARs of `icao` after `after`, oldest first, one per epoch. */
	@JvmStatic
	fun newMetars(reports: List<Report>, icao: String, after: Int): List<Report> =
		reports.filter { it.icao == icao && it.type == "METAR" && it.epoch > after }
			.distinctBy { it.epoch }.sortedBy { it.epoch }

	/** One METAR per clock hour (UTC), the hour's latest, and only for hours over by `until`. Most
	 *  stations send one routine METAR an hour (:51-:59); some automated ones (KLAM, KPNA, KANE at
	 *  night) send one every 20 minutes, :15, :35 and :55, and the :55 one is the hourly report (the
	 *  one with the 3-hour pressure tendency). Waiting for the hour to end keeps a run at :45 from
	 *  storing the :15 or :35 one before the :55 one exists. Input oldest first, output too. */
	@JvmStatic
	fun hourly(metars: List<Report>, until: Int): List<Report> =
		metars.groupBy { it.epoch / 3600 }.filterKeys { (it + 1) * 3600 <= until }.values.map { h -> h.maxBy { it.epoch } }
			.sortedBy { it.epoch }

	private fun hoursBetween(from: Int, to: Int): Int = minOf(MAX_HOURS, (to - from + 3599) / 3600 + 1)

	/** Every report of one station after `last` up to `now`, paging back past AWC's cap: null when
	 *  the budget ran out before the whole stretch was fetched (nothing of it is to be written). */
	@JvmStatic
	fun since(icao: String, last: Int, now: Int, budget: Budget, fetcher: Fetcher): List<Report>? =
		between(icao, maxOf(last, now - MAX_HOURS * 3600), null, now, budget, fetcher)

	/** Every report of one station from `from` to `to` (null: now), paging back past AWC's cap;
	 *  null when the budget ran out first. */
	@JvmStatic
	fun between(icao: String, from: Int, to: Int?, now: Int, budget: Budget, fetcher: Fetcher): List<Report>? {
		val reports = mutableListOf<Report>()
		var end: Int? = to
		while (true) {
			if (budget.left <= 0) return null
			budget.left--
			val page = fetcher.fetch(listOf(icao), hoursBetween(from, end ?: now), end)
			reports += page
			if (page.size < MAX_REPORTS) return reports.filter { it.epoch >= from }
			val oldest = page.minOf { it.epoch }
			if (oldest <= from || (end != null && oldest >= end)) return reports.filter { it.epoch >= from }
			end = oldest
		}
	}

	/** The METARs of `metars` with no epoch of `stored` within FILL_CLEARANCE of them. */
	@JvmStatic
	fun uncovered(metars: List<Report>, stored: List<Int>): List<Report> {
		val sorted = stored.sorted()
		return metars.filter { m ->
			val i = sorted.binarySearch(m.epoch - FILL_CLEARANCE).let { if (it < 0) -it - 1 else it }
			i >= sorted.size || sorted[i] > m.epoch + FILL_CLEARANCE
		}
	}

	/** The reports of a group of stations for the last `hours`, split in halves while an answer is
	 *  full (a full answer may have lost its oldest reports). */
	@JvmStatic
	fun group(ids: List<String>, hours: Int, budget: Budget, fetcher: Fetcher): List<Report> {
		budget.left--
		val page = fetcher.fetch(ids, hours, null)
		if (page.size < MAX_REPORTS || ids.size == 1) return page
		val half = ids.size / 2
		return group(ids.subList(0, half), hours, budget, fetcher) + group(ids.subList(half, ids.size), hours, budget, fetcher)
	}

	private fun iso(epoch: Int): String {
		val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		f.timeZone = TimeZone.getTimeZone("UTC")
		return f.format(epoch * 1000L)
	}

	private fun utc(epoch: Int): String {
		val f = SimpleDateFormat("yyyy-MM-dd HH:mm")
		f.timeZone = TimeZone.getTimeZone("UTC")
		return f.format(epoch * 1000L)
	}

	/** The real AWC request, PAUSE_MS after the previous one. HTTP 204 (nothing found) is an empty
	 *  list; any other answer than 200 throws, with AWC's error text. */
	private object Http : Fetcher
	{
		private var lastRequest = 0L

		override fun fetch(ids: List<String>, hours: Int, end: Int?): List<Report> {
			val wait = lastRequest + PAUSE_MS - System.currentTimeMillis()
			if (wait > 0) Thread.sleep(wait)
			lastRequest = System.currentTimeMillis()
			val url = "$BASE?ids=${ids.joinToString(",")}&format=json&hours=$hours" + (end?.let { "&date=${iso(it)}" } ?: "")
			val connection = URL(url).openConnection() as HttpURLConnection
			connection.setRequestProperty("User-Agent", USER_AGENT)
			connection.connectTimeout = 20_000
			connection.readTimeout = 60_000
			try {
				val code = connection.responseCode
				if (code == 204) return emptyList()
				if (code != 200) {
					val error = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
					throw IOException("HTTP $code ${error.take(200)}")
				}
				return parseReports(connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() })
			} finally {
				connection.disconnect()
			}
		}
	}

	@JvmStatic
	fun check(dryRun: Boolean, fetcher: Fetcher = Http) {
		val now = (System.currentTimeMillis() / 1000).toInt()
		val budget = Budget()
		var requests = 0
		var failed = 0
		var firstError: String? = null

		try {
			MeteredDataConnector("larsi-weather2").use { md ->
				val prefixes = md.queryList("SELECT prefix FROM location;") { it.getString(1) }
				val last = prefixes.associateWith { md.getMaxDateTimeLog2(it, CHANNELS) }
				val number = prefixes.withIndex().associate { (i, p) -> p to "${String.format("%4s", i + 1)} / ${String.format("%4s", prefixes.size)} - ${p.uppercase()}:" }

				fun store(prefix: String, metars: List<Report>, note: String) {
					val header = number.getValue(prefix)
					val rows = metars.map { it.epoch to Metar.parse(it.raw) }.filter { it.second.isNotEmpty() }
					if (rows.isEmpty()) {
						println("$header up to date$note")
						return
					}
					val span = "${utc(rows.first().first)}" + (if (rows.size > 1) " to ${utc(rows.last().first)}" else "") + " UTC"
					if (dryRun) {
						println("$header would add ${rows.size} METAR${if (rows.size > 1) "s" else ""} ($span)$note")
						return
					}
					md.clearBatch()
					for ((epoch, values) in rows) md.addBatch(MeteredDataConnector.insertLogRowsSQL2(prefix, epoch, values))
					md.executeBatch()
					md.executeUpdate(md.updateLastEpochSQL2(prefix, rows.last().first))
					println("$header added ${rows.size} METAR${if (rows.size > 1) "s" else ""} ($span)$note")
				}

				// stations stored recently: asked for together
				val recent = prefixes.filter { last.getValue(it) >= now - RECENT_HOURS * 3600 }
				for (chunk in recent.chunked(GROUP_SIZE)) {
					val before = budget.left
					try {
						val reports = group(chunk.map { it.uppercase() }, RECENT_HOURS + 1, budget, fetcher)
						for (prefix in chunk) store(prefix, hourly(newMetars(reports, prefix.uppercase(), last.getValue(prefix)), now), "")
					} catch (e: Exception) {
						failed++
						firstError = firstError ?: e.message
						println("group of ${chunk.size} from ${chunk.first().uppercase()}: request failed (${e.message}), left for the next run")
					}
					requests += before - budget.left
				}

				// stations further behind: one at a time, least behind first
				val behind = prefixes.filter { last.getValue(it) < now - RECENT_HOURS * 3600 }.sortedByDescending { last.getValue(it) }
				for (prefix in behind) {
					val header = number.getValue(prefix)
					val since = last.getValue(prefix).let { if (it > 0) "since ${utc(it)} UTC" else "nothing stored" }
					val before = budget.left
					try {
						val reports = since(prefix.uppercase(), last.getValue(prefix), now, budget, fetcher)
						val metars = reports?.let { hourly(newMetars(it, prefix.uppercase(), last.getValue(prefix)), now) }
						if (metars == null) println("$header behind, $since: left for the next run (request budget used up)")
						else if (metars.isEmpty()) println("$header behind, $since: AWC has no newer METAR")
						else store(prefix, metars, ", behind $since, ${before - budget.left} request${if (before - budget.left > 1) "s" else ""}")
					} catch (e: Exception) {
						failed++
						firstError = firstError ?: e.message
						println("$header behind, $since: request failed (${e.message}), left for the next run")
					}
					requests += before - budget.left
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			if (!dryRun) Ntfy.publish(Zeus.NTFY_TOPIC, "AviationWeather DB Error", "DB connection was refused (larsi-weather2)", priority = "urgent", tags = "rotating_light")
			return
		}

		println("$requests requests, $failed failed${if (budget.left <= 0) ", budget of $MAX_REQUESTS used up" else ""}")
		if (failed > 0 && !dryRun) {
			Ntfy.publish(Zeus.NTFY_TOPIC, "AviationWeather Request Error",
				"$failed of $requests AWC requests failed this run (first: $firstError)", priority = "high", tags = "warning")
		}
	}

	private fun parseTime(s: String): Int {
		val f = SimpleDateFormat(if (s.length > 10) "yyyy-MM-dd'T'HH:mm" else "yyyy-MM-dd")
		f.timeZone = TimeZone.getTimeZone("UTC")
		f.isLenient = false
		return (f.parse(s).time / 1000).toInt()
	}

	/** The start of `fill --days n`: n days before now, but at least an hour inside AWC's 30 days, so
	 *  the oldest page's `date` is never refused. */
	fun daysBack(days: Int, now: Int): Int = maxOf(now - days * 86400, now - MAX_HOURS * 3600 + 3600)

	@JvmStatic
	fun fill(from: Int, to: Int, only: List<String>, dryRun: Boolean, fetcher: Fetcher = Http) {
		val now = (System.currentTimeMillis() / 1000).toInt()
		require(from < to) { "fill: <from> must be before <to>" }
		require(from > now - MAX_HOURS * 3600) { "fill: AWC keeps 30 days, ${utc(from)} is older" }
		val budget = Budget(Int.MAX_VALUE)
		var added = 0; var skipped = 0
		MeteredDataConnector("larsi-weather2").use { md ->
			val all = md.queryList("SELECT prefix FROM location;") { it.getString(1) }
			val unknown = only.map { it.lowercase() } - all.toSet()
			require(unknown.isEmpty()) { "unknown station(s): $unknown" }
			val prefixes = if (only.isEmpty()) all else all.filter { it in only.map { o -> o.lowercase() } }
			println("fill ${utc(from)} to ${utc(to)} UTC${if (dryRun) " (dry run)" else ""}")
			for ((i, prefix) in prefixes.withIndex()) {
				val header = "${String.format("%4s", i + 1)} / ${String.format("%4s", prefixes.size)} - ${prefix.uppercase()}:"
				try {
					val metars = hourly(newMetars(between(prefix.uppercase(), from, to, now, budget, fetcher)!!, prefix.uppercase(), from - 1)
						.filter { it.epoch <= to }, minOf(to, now))
					val stored = md.queryList("SELECT DISTINCT epoch FROM log WHERE prefix='$prefix' AND channel IN ($CHANNELS) " +
						"AND epoch BETWEEN ${from - FILL_CLEARANCE} AND ${to + FILL_CLEARANCE}") { it.getInt(1) }
					val rows = uncovered(metars, stored).map { it.epoch to Metar.parse(it.raw) }.filter { it.second.isNotEmpty() }
					skipped += metars.size - rows.size
					if (rows.isEmpty()) {
						println("$header nothing to fill (${metars.size} METARs at AWC, all near a stored reading)")
						continue
					}
					val holes = Ghcnh.runs(rows.map { it.first }).joinToString(", ") {
						if (it.first == it.last) utc(it.first) else "${utc(it.first)} to ${utc(it.last)}" }
					val span = "$holes UTC"
					if (!dryRun) {
						md.clearBatch()
						for ((epoch, values) in rows) md.addBatch(MeteredDataConnector.insertLogRowsSQL2(prefix, epoch, values))
						md.executeBatch()
					}
					added += rows.size
					println("$header ${if (dryRun) "would fill" else "filled"} ${rows.size} of ${metars.size} METARs ($span)")
				} catch (e: Exception) {
					println("$header request failed (${e.message})")
				}
			}
		}
		println("${if (dryRun) "would fill" else "filled"} $added METARs, $skipped left out next to a stored reading, ${Int.MAX_VALUE - budget.left} requests")
	}

	@JvmStatic
	fun main(args: Array<String>) {
		val dryRun = "--dry-run" in args
		val rest = args.filter { !it.startsWith("--") }
		try {
			if (rest.firstOrNull() == "fill" && "--days" in args) {
				val days = args.getOrNull(args.indexOf("--days") + 1)?.toIntOrNull()
				require(days != null && days in 1..MAX_HOURS / 24) { "fill --days takes 1 to ${MAX_HOURS / 24}" }
				val now = (System.currentTimeMillis() / 1000).toInt()
				fill(daysBack(days, now), now, rest.drop(1) - days.toString(), dryRun)
			} else if (rest.firstOrNull() == "fill") {
				if (rest.size < 3) { System.err.println("usage: aviationweather.jar fill [--dry-run] <from> <to> [prefix ...]\n" +
					"       aviationweather.jar fill [--dry-run] --days <n> [prefix ...]"); return }
				fill(parseTime(rest[1]), parseTime(rest[2]), rest.drop(3), dryRun)
			} else {
				check(dryRun)
			}
		} catch (e: IllegalArgumentException) {
			System.err.println(e.message)
			return
		}
		println("Done")
	}
}
