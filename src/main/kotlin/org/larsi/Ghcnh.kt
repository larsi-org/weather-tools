package org.larsi

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

import org.larsi.util.Icao
import org.larsi.util.MeteredDataConnector
import org.larsi.util.WeatherToolsConfig

/**
 * Archived hourly observations from NOAA's GHCNh (Global Historical Climatology Network hourly),
 * the successor of ISD, which ended 2025-08-28.
 *
 *   java -jar ghcnh.jar download <year | from-to> [prefix ...]
 *   java -jar ghcnh.jar load [--dry-run] [<year | from-to>] [prefix ...]
 *
 * download: for each year and station (all of Icao, or the prefixes given) fetches
 * https://www.ncei.noaa.gov/oa/global-historical-climatology-network/hourly/access/by-year/<year>/psv/GHCNh_<id>_<year>.psv
 * into WORK_DIRECTORY/ghcnh/<year>/, gzipped. A past year is fetched once (an existing file is
 * kept), the current year every time, since it grows. A failed download is retried 3 times, then
 * skipped; the failures are listed at the end, and running the same command again fetches only
 * what is missing. No database access, so no credentials.json needed.
 *
 * load: replaces each station's rows in `log` with its file's METARs, from the files on disk (run
 * download first), only where the file has them: the METARs, sorted, split into runs wherever two
 * neighbors are more than MAX_STEP apart (a missing hour makes a gap), and for each run channels
 * 0-6 from its first METAR to its last are deleted, then the file's METARs inserted. A gap in the
 * file (NOAA's 2026-11-09 to 11-12 outage, a late start of the year, anything after its last
 * METAR) keeps whatever GeoNames2 stored there; GeoNames2 cannot refetch history, so deleting it
 * would leave the hours empty. A file with no METAR deletes nothing. Years default to
 * LOAD_FROM (2025, the year ISD stopped) through the current year; 2024 and before stay as the
 * earlier ISD import loaded them. --dry-run prints what would change, with the file's gaps of
 * GAP_LISTED or longer, and writes nothing.
 *
 * Station id: USW000 + the WBAN part of Icao.usafWban (all 304 are in GHCNh's station list).
 *
 * GHCNh merges every source into one row per timestamp; each value carries its own report type,
 * source and quality code. Only METAR values (report type FM15) are kept, one an hour (`hourly`), the series
 * AviationWeather stores live (SPECI FM16 and SYNOP FM12 are left out; the earlier ISD import loaded them too,
 * as records of their own, which is where 2024-2025's duplicate rows came from). A value is dropped when
 * GHCNh's checks failed it: a letter code (a-z) or a legacy 2, 3, 6 or 7 (suspect or erroneous).
 *
 *   0 temperature (C), 1 dew_point_temperature (C), 2 relative_humidity (%, derived by GHCNh),
 *   3 sea_level_pressure (hPa), 4 wind_direction (degrees; 999 = calm or variable, dropped), 5 wind_speed (m/s), 6 clouds: the highest sky_cover_summation layer as GeoNames2 codes
 *   it (CLR/SKC 0, FEW 2, SCT 4, BKN 6, OVC and VV 10).
 */
object Ghcnh
{
	private const val BASE = "https://www.ncei.noaa.gov/oa/global-historical-climatology-network/hourly/access/by-year"
	private const val CHANNELS = "0,1,2,3,4,5,6"
	private const val INSERT_CHUNK = 2000 // observations per INSERT statement
	const val LOAD_FROM = 2025
	/** Two METARs further apart than this (seconds) are in different runs: hourly ones are 3600
	 *  apart, one missing hour makes 7200. */
	const val MAX_STEP = 5400
	private const val GAP_LISTED = 3 * 3600 // gaps this long or longer (2 hours without a METAR) are listed in the log

	/** One METAR's values by channel, at its epoch (UTC seconds). */
	data class Observation(val epoch: Int, val values: Map<Int, String>)

	private val COLUMNS = linkedMapOf(
		0 to "temperature",
		1 to "dew_point_temperature",
		2 to "relative_humidity",
		3 to "sea_level_pressure",
		4 to "wind_direction",
		5 to "wind_speed")

	private val CLOUD_CODES = mapOf("CLR" to 0, "SKC" to 0, "FEW" to 2, "SCT" to 4, "BKN" to 6, "OVC" to 10, "VV" to 10)

	private val NUMBER = Regex("""-?\d+(\.\d+)?""")

	fun stationId(entry: Icao): String = "USW000" + entry.usafWban.substringAfter('-')

	/** A quality code GHCNh's checks passed (or did not set). */
	fun goodQuality(code: String): Boolean =
		code.isEmpty() || code !in setOf("2", "3", "6", "7") && !(code.length == 1 && code[0] in 'a'..'z')

	/** The highest cover among sky_cover_summation values like "SCT:04;" as GeoNames2 codes it,
	 *  or null when none is given. */
	fun cloudCode(layers: List<String>): Int? =
		layers.mapNotNull { CLOUD_CODES[it.substringBefore(':').trim()] }.maxOrNull()

	fun parseEpoch(date: String): Int {
		val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
		f.timeZone = TimeZone.getTimeZone("UTC")
		return (f.parse(date).time / 1000).toInt()
	}

	/** One PSV row to an observation, or null when it is not a METAR. `col` gives a column's index
	 *  by name (from the header line). */
	fun parseRow(fields: List<String>, col: Map<String, Int>): Observation? {
		fun get(name: String) = col[name]?.let { fields.getOrNull(it) } ?: ""
		if (get("temperature_Report_Type") != "FM15") return null

		val values = sortedMapOf<Int, String>()
		for ((channel, name) in COLUMNS) {
			val v = get(name)
			if (!NUMBER.matches(v) || get("${name}_Report_Type") != "FM15" || !goodQuality(get("${name}_Quality_Code"))) continue
			if (channel == 4 && v == "999") continue
			values[channel] = v
		}
		val layers = (1..4).map { "sky_cover_summation_$it" }
			.filter { get("${it}_Report_Type") == "FM15" }
			.map { get(it) }.filter { it.isNotEmpty() }
		cloudCode(layers)?.let { values[6] = it.toString() }

		return if (values.isEmpty()) null else Observation(parseEpoch(get("DATE")), values)
	}

	/** Every METAR in a (gzipped) PSV file, in time order, one per epoch. */
	fun readFile(file: File): List<Observation> {
		val byEpoch = sortedMapOf<Int, Observation>()
		GZIPInputStream(file.inputStream()).bufferedReader(Charset.defaultCharset()).use { reader ->
			val header = reader.readLine()?.split('|') ?: return emptyList()
			val col = header.withIndex().associate { (i, name) -> name to i }
			reader.forEachLine { line ->
				parseRow(line.split('|'), col)?.let { byEpoch[it.epoch] = it }
			}
		}
		return byEpoch.values.toList()
	}

	/** Stations whose file has a second instrument's METARs in most hours, at a fixed minute next to
	 *  the station's own: the own METAR's minute, kept when the hour has it. KCYS: :53 and :55 in
	 *  every hour with clearly different values (2026-06-10 10:53 10.6 C, wind 280; 10:55 7.9 C,
	 *  calm); AWC has only the :53 one. */
	val OWN_MINUTE = mapOf("kcys" to 53)

	/** One METAR per clock hour (UTC), the rule AviationWeather stores by: the hour's latest, or the
	 *  one at `ownMinute` when the hour has it. NOAA's files keep every routine METAR: the 20-minute
	 *  stations' :15, :35 and :55 (KLAM, KPNA, KOUN and others, some only at times), and extra
	 *  reports before the routine one (:00, :45-:51 before a :53); in 2026 87,661 METARs more than
	 *  station-hours, in 2025 128,607. Input sorted, output too. */
	fun hourly(observations: List<Observation>, ownMinute: Int? = null): List<Observation> =
		observations.groupBy { it.epoch / 3600 }.values.map { h ->
			ownMinute?.let { m -> h.lastOrNull { it.epoch / 60 % 60 == m } } ?: h.last()
		}.sortedBy { it.epoch }

	/** Sorted epochs as runs of neighbors at most `maxStep` apart, each the range from its first
	 *  epoch to its last (a lone epoch is a run of its own). */
	fun runs(epochs: List<Int>, maxStep: Int = MAX_STEP): List<IntRange> {
		val runs = mutableListOf<IntRange>()
		var start = 0
		for (i in 1..epochs.size) {
			if (i == epochs.size || epochs[i] - epochs[i - 1] > maxStep) {
				if (epochs.isNotEmpty()) runs += epochs[start]..epochs[i - 1]
				start = i
			}
		}
		return runs
	}

	/** `epoch` as "2026-11-09 15:54" UTC, for the log. */
	private fun utc(epoch: Int): String {
		val f = SimpleDateFormat("yyyy-MM-dd HH:mm")
		f.timeZone = TimeZone.getTimeZone("UTC")
		return f.format(epoch * 1000L)
	}

	/** Downloads one station-year into `file`, gzipped. False when GHCNh has no such file; throws
	 *  on any other failure (the partial file is removed). */
	private fun fetch(url: String, file: File): Boolean {
		val part = File(file.path + ".part")
		val connection = URL(url).openConnection() as HttpURLConnection
		connection.connectTimeout = 30_000
		connection.readTimeout = 120_000
		try {
			connection.inputStream.use { input ->
				GZIPOutputStream(FileOutputStream(part)).use { output -> input.copyTo(output, 64 * 1024) }
			}
		} catch (e: FileNotFoundException) {
			part.delete()
			return false
		} catch (e: Exception) {
			part.delete()
			throw e
		} finally {
			connection.disconnect()
		}
		return part.renameTo(file)
	}

	private fun currentYear() = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)

	private fun yearRange(arg: String): IntRange =
		arg.split('-').map { it.toInt() }.let { if (it.size == 2) it[0]..it[1] else it[0]..it[0] }

	private fun stations(prefixes: List<String>): List<Icao> {
		val wanted = prefixes.map { it.lowercase() }.toSet()
		val unknown = wanted - Icao.entries.map { it.name.lowercase() }.toSet()
		require(unknown.isEmpty()) { "unknown station(s): $unknown" }
		return Icao.entries.filter { wanted.isEmpty() || it.name.lowercase() in wanted }
	}

	private fun file(year: Int, entry: Icao) =
		File(WeatherToolsConfig.WORK_DIRECTORY, "ghcnh/$year/GHCNh_${stationId(entry)}_$year.psv.gz")

	fun download(years: IntRange, stations: List<Icao>) {
		val failed = mutableListOf<String>()
		var fetched = 0; var kept = 0; var none = 0
		for (year in years) {
			File(WeatherToolsConfig.WORK_DIRECTORY, "ghcnh/$year").mkdirs()
			for ((i, entry) in stations.withIndex()) {
				val file = file(year, entry)
				val tag = "$year ${i + 1}/${stations.size} ${entry.name.lowercase()} (${stationId(entry)})"
				if (file.exists() && year < currentYear()) { kept++; continue }
				val url = "$BASE/$year/psv/GHCNh_${stationId(entry)}_$year.psv"
				var done = false
				for (attempt in 1..4) {
					try {
						if (fetch(url, file)) { fetched++; println("$tag: ${file.length() / 1024} KB") }
						else { none++; println("$tag: no GHCNh file") }
						done = true
						break
					} catch (e: Exception) {
						println("$tag: attempt $attempt failed (${e.message})")
						Thread.sleep(10_000L * attempt)
					}
				}
				if (!done) failed += "$year ${entry.name.lowercase()}"
				Thread.sleep(500)
			}
		}
		println("downloaded $fetched, already on disk $kept, not in GHCNh $none, failed ${failed.size}")
		if (failed.isNotEmpty()) println("failed (run the same command again to retry): ${failed.joinToString(", ")}")
	}

	fun load(years: IntRange, stations: List<Icao>, dryRun: Boolean) {
		for (year in years) {
			println(">>> $year${if (dryRun) " (dry run)" else ""} <<<")
			for ((i, entry) in stations.withIndex()) {
				val prefix = entry.name.lowercase()
				val file = file(year, entry)
				val tag = "${i + 1}/${stations.size} $prefix (${stationId(entry)}) $year"
				if (!file.exists()) {
					println("$tag: not downloaded")
					continue
				}

				val all = readFile(file)
				val observations = hourly(all, OWN_MINUTE[prefix])
				if (observations.isEmpty()) {
					println("$tag: no METAR, nothing replaced")
					continue
				}
				// runs from every METAR in the file, so the hours it covers are replaced in full,
				// the METARs `hourly` leaves out included
				val runs = runs(all.map { it.epoch })
				val ranges = runs.joinToString(" OR ") { "epoch BETWEEN ${it.first} AND ${it.last}" }
				val where = "prefix='$prefix' AND channel IN ($CHANNELS) AND ($ranges)"
				val values = observations.sumOf { it.values.size }
				val gaps = runs.zipWithNext { a, b -> a.last to b.first }
				val listed = gaps.filter { (from, to) -> to - from >= GAP_LISTED }
				val span = "${utc(runs.first().first)} to ${utc(runs.last().last)} UTC, ${runs.size} runs, " +
					"${gaps.size} gaps kept)" + listed.joinToString("") { (from, to) ->
						"\n    no METAR between ${utc(from)} and ${utc(to)} (${(to - from + 1800) / 3600 - 1} hours)" }

				MeteredDataConnector("larsi-weather2").use { md ->
					val existing = md.queryList("SELECT COUNT(*) FROM log WHERE $where") { it.getInt(1) }.first()
					if (dryRun) {
						println("$tag: would replace $existing rows with $values (${observations.size} METARs, $span")
						return@use
					}
					md.clearBatch()
					md.addBatch("DELETE FROM log WHERE $where")
					for (chunk in observations.chunked(INSERT_CHUNK)) {
						md.addBatch("INSERT IGNORE INTO log (epoch, prefix, channel, value) VALUES " + chunk.joinToString(", ") { o ->
							o.values.entries.joinToString(", ") { (channel, v) -> "(${o.epoch}, '$prefix', $channel, $v)" }
						})
					}
					md.executeBatch()
					println("$tag: replaced $existing rows with $values (${observations.size} METARs, $span")
				}
			}
			println()
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		val usage = "usage: ghcnh.jar download <year | from-to> [prefix ...]\n" +
			"       ghcnh.jar load [--dry-run] [<year | from-to>] [prefix ...]"
		val command = args.firstOrNull()
		val rest = args.drop(1).filter { !it.startsWith("--") }
		val hasYears = rest.firstOrNull()?.matches(Regex("""\d{4}(-\d{4})?""")) == true
		try { when (command) {
			"download" -> {
				if (!hasYears) { System.err.println(usage); return }
				download(yearRange(rest[0]), stations(rest.drop(1)))
			}
			"load" -> {
				val years = if (hasYears) yearRange(rest[0]) else LOAD_FROM..currentYear()
				load(years, stations(if (hasYears) rest.drop(1) else rest), "--dry-run" in args)
			}
			else -> System.err.println(usage)
		} } catch (e: IllegalArgumentException) {
			System.err.println(e.message)
		}
	}
}
