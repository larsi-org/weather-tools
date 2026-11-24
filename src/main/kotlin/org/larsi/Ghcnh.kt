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
 * the successor of ISD, which ended 2025-08-28 (IshHarvester/Ish2 read ISD).
 *
 *   java -jar ghcnh.jar [--dry-run] [--no-download] <year | from-to> [prefix ...]
 *
 * For each year and station (all of Icao, or the prefixes given) it downloads
 * https://www.ncei.noaa.gov/oa/global-historical-climatology-network/hourly/access/by-year/<year>/psv/GHCNh_<id>_<year>.psv
 * into WORK_DIRECTORY/ghcnh/<year>/, gzipped (a past year once, the current year every run, since
 * it grows), then replaces the station's rows in `log` for everything the file covers: channels
 * 0-6 from the year's start up to its last METAR are deleted and the file's METARs inserted, so a
 * live GeoNames2 reading stays only where GHCNh has nothing yet (after its last METAR). A file
 * with no METAR deletes nothing. --dry-run prints what would change and writes nothing;
 * --no-download uses the files already on disk.
 *
 * Station id: USW000 + the WBAN part of Icao.usafWban (all 304 are in GHCNh's station list).
 *
 * GHCNh merges every source into one row per timestamp; each value carries its own report type,
 * source and quality code. Only METAR values (report type FM15) are kept, one an hour, the series
 * GeoNames2 stores live (SPECI FM16 and SYNOP FM12, which Ish2 also loaded, are left out; ISD gave
 * those their own records, which is where Ish2's duplicate rows came from). A value is dropped when
 * GHCNh's checks failed it: a letter code (a-z) or a legacy 2, 3, 6 or 7 (suspect or erroneous).
 *
 *   0 temperature (C), 1 dew_point_temperature (C), 2 relative_humidity (%, derived by GHCNh),
 *   3 sea_level_pressure (hPa), 4 wind_direction (degrees; 999 = calm or variable, dropped as in
 *   Ish2), 5 wind_speed (m/s), 6 clouds: the highest sky_cover_summation layer as GeoNames2 codes
 *   it (CLR/SKC 0, FEW 2, SCT 4, BKN 6, OVC and VV 10).
 */
object Ghcnh
{
	private const val BASE = "https://www.ncei.noaa.gov/oa/global-historical-climatology-network/hourly/access/by-year"
	private const val CHANNELS = "0,1,2,3,4,5,6"
	private const val INSERT_CHUNK = 2000 // observations per INSERT statement

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

	/** Downloads one station-year into `file`, gzipped. False when GHCNh has no such file. */
	private fun download(url: String, file: File): Boolean {
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

	private fun yearStart(year: Int): Int {
		val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
		c.clear()
		c.set(year, Calendar.JANUARY, 1)
		return (c.timeInMillis / 1000).toInt()
	}

	@JvmStatic
	fun main(args: Array<String>) {
		val dryRun = "--dry-run" in args
		val noDownload = "--no-download" in args
		val rest = args.filter { !it.startsWith("--") }
		if (rest.isEmpty()) {
			System.err.println("usage: ghcnh.jar [--dry-run] [--no-download] <year | from-to> [prefix ...]")
			return
		}
		val years = rest[0].split('-').map { it.toInt() }.let { if (it.size == 2) it[0]..it[1] else it[0]..it[0] }
		val wanted = rest.drop(1).map { it.lowercase() }.toSet()
		val stations = Icao.entries.filter { wanted.isEmpty() || it.name.lowercase() in wanted }
		val currentYear = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.YEAR)

		for (year in years) {
			val directory = File(WeatherToolsConfig.WORK_DIRECTORY, "ghcnh/$year")
			directory.mkdirs()
			println(">>> $year${if (dryRun) " (dry run)" else ""} <<<")

			for ((i, entry) in stations.withIndex()) {
				val prefix = entry.name.lowercase()
				val id = stationId(entry)
				val file = File(directory, "GHCNh_${id}_$year.psv.gz")
				val tag = "${i + 1}/${stations.size} $prefix ($id) $year"

				if (!noDownload && (!file.exists() || year >= currentYear)) {
					if (!download("$BASE/$year/psv/GHCNh_${id}_$year.psv", file)) {
						println("$tag: no GHCNh file")
						continue
					}
				}
				if (!file.exists()) {
					println("$tag: not downloaded")
					continue
				}

				val observations = readFile(file)
				if (observations.isEmpty()) {
					println("$tag: no METAR, nothing replaced")
					continue
				}
				val from = yearStart(year)
				val to = observations.last().epoch
				val where = "prefix='$prefix' AND channel IN ($CHANNELS) AND epoch >= $from AND epoch <= $to"
				val values = observations.sumOf { it.values.size }

				MeteredDataConnector("larsi-weather2").use { md ->
					val existing = md.queryList("SELECT COUNT(*) FROM log WHERE $where") { it.getInt(1) }.first()
					if (dryRun) {
						println("$tag: would replace $existing rows with $values (${observations.size} METARs, up to epoch $to)")
						return@use
					}
					md.clearBatch()
					md.addBatch("DELETE FROM log WHERE $where")
					for (chunk in observations.chunked(INSERT_CHUNK)) {
						md.addBatch("INSERT INTO log (epoch, prefix, channel, value) VALUES " + chunk.joinToString(", ") { o ->
							o.values.entries.joinToString(", ") { (channel, v) -> "(${o.epoch}, '$prefix', $channel, $v)" }
						})
					}
					md.executeBatch()
					println("$tag: replaced $existing rows with $values (${observations.size} METARs, up to epoch $to)")
				}
			}
			println()
		}
	}
}
