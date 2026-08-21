package org.larsi

import java.util.Date

import org.larsi.util.MeteredDataConnector
import org.larsi.util.Ntfy

object Zeus
{
	const val NTFY_TOPIC = "larsi-zeus"

	/** Both DBs live on the same GoDaddy host/credentials, so a connection failure on one almost
	 *  always means the other would fail too (e.g. an IP whitelist lapse) -- stop at the first
	 *  failure instead of trying both and duplicating the same error. */
	@JvmStatic
	fun check(): ZeusCheckResult
	{
		val alerts = mutableListOf<String>()

		try {
			checkSensors(alerts)
		}
		catch (e: Exception) {
			e.printStackTrace()
			return ZeusCheckResult(alerts, "DB connection was refused (larsi-sensors)")
		}

		try {
			checkWeather(alerts)
		}
		catch (e: Exception) {
			e.printStackTrace()
			return ZeusCheckResult(alerts, "DB connection was refused (larsi-weather2)")
		}

		return ZeusCheckResult(alerts)
	}

	/** larsi-sensors: per-sensor stats -> rolled up per-device -> per-device alerting */
	private fun checkSensors(alerts: MutableList<String>)
	{
		MeteredDataConnector("larsi-sensors").use { md ->
			val zeusEntries = md.queryList("SELECT `prefix`, `channel` FROM `sensor`") {
				ZeusEntry(it.getString(1), it.getInt(2))
			}

			println("Checking ${zeusEntries.size} sensor entries...")
			for (entry in zeusEntries) {
				try {
					println("${entry.prefix}[${entry.id}]")
					val statsSQL = """
						SELECT COUNT(*) AS `count`, MAX(`epoch`) AS `last_epoch`
						FROM ${entry.prefix}_log
						WHERE `channel`=${entry.id}
					""".trimIndent()
					val stats = md.queryList(statsSQL) {
						ZeusStats(it.getInt(1), it.getInt(2))
					}.first()
					val count = stats.count
					val lastEpoch = stats.lastEpoch
					val updateSQL = """
						UPDATE sensor SET `count`=$count, `last_epoch`=$lastEpoch
						WHERE `prefix`='${entry.prefix}' && `channel`=${entry.id}
					""".trimIndent()
					md.executeUpdate(updateSQL)
				}
				catch (e: Exception) {
					e.printStackTrace()
				}
			}

			// Roll each device's sensor stats up: last_epoch = latest of any of its sensors.
			// device.last_epoch itself is ingest-owned by sensors/log.php now, not Zeus -- this
			// rollup only exists to compute the alerting delta below, never written back.
			val deviceStatsSQL = """
				SELECT `prefix`, `device_id`, MAX(`last_epoch`) AS `last_epoch`
				FROM sensor
				GROUP BY `prefix`, `device_id`
			""".trimIndent()
			val deviceStats = md.queryList(deviceStatsSQL) {
				ZeusDeviceStats(it.getString(1), it.getInt(2), it.getInt(3))
			}

			// device_name/zeus_minutes/zeus_successful, keyed by (prefix, device_id) -- only
			// devices with zeus_minutes > 0 are monitored at all; joined against `deviceStats`
			// below rather than checked in its own loop, since a device absent from `sensor` has
			// nothing to check this run anyway.
			val zeusConfigSQL = """
				SELECT `prefix`, `device_id`, `device_name`, `zeus_minutes`, `zeus_successful`
				FROM device
				WHERE `zeus_minutes` > 0
			""".trimIndent()
			val zeusConfig = md.queryList(zeusConfigSQL) {
				ZeusEntry(
                    prefix = it.getString(1),
                    id = it.getInt(2),
                    deviceName = it.getString(3),
                    minutes = it.getInt(4),
                    successful = it.getInt(5) != 0
				)
			}.associateBy { Pair(it.prefix, it.id) }

			println("Checking ${deviceStats.size} devices...")
			for (entry in deviceStats) {
				try {
					println("${entry.prefix}[${entry.deviceId}]")

					val zeusInfo = zeusConfig[Pair(entry.prefix, entry.deviceId)]
					if (zeusInfo != null) {
						var delta = (Date().time / 1000).toInt() - entry.lastEpoch
						delta /= 60 // in minutes
						val successful = delta <= zeusInfo.minutes
						if (zeusInfo.successful != successful) {
							alerts.add("${if (successful) "✅ RESUMED" else "⚠️ FAILED"}: ${entry.prefix}[${zeusInfo.deviceName}] ($delta min)\n")
						}
						// Written every run, even when unchanged -- simpler to read than gating
						// the write on the same condition that gates the alert.
						val updateSQL = """
							UPDATE device SET `zeus_successful`=${if (successful) "1" else "0"}
							WHERE `prefix`='${entry.prefix}' && `device_id`=${entry.deviceId}
						""".trimIndent()
						md.executeUpdate(updateSQL)
					}
				}
				catch (e: Exception) {
					e.printStackTrace()
				}
			}

			println() // blank line separating this section's logging from what follows
		}
	}

	/** larsi-weather2: per-station stats and alerting -- flatter than checkSensors, see CLAUDE.md */
	private fun checkWeather(alerts: MutableList<String>)
	{
		MeteredDataConnector("larsi-weather2").use { md ->
			// No per-station `sensor` table here (weather2's `sensor` is 14 rows of shared
			// display metadata, not per-instance data) and no `user` table (no per-station
			// ownership), so there's no per-sensor stats table to roll up from either -- this
			// queries `log` directly just to compute the alerting delta below. location.last_epoch
			// itself is ingest-owned by GeoNames2 now, not Zeus, so it's never written back here.
			val statsSQL = """
				SELECT `prefix`, MAX(`epoch`) AS `last_epoch`
				FROM log
				GROUP BY `prefix`
			""".trimIndent()
			val stats = md.queryList(statsSQL) {
				ZeusDeviceStats(prefix = it.getString(1), lastEpoch = it.getInt(2))
			}

			// zeus_minutes/zeus_successful, keyed by prefix (log.station and location.prefix are
			// both lowercase, so this matches `stats` below directly, no case conversion) -- only
			// stations with zeus_minutes > 0 are monitored at all; joined against `stats` below
			// rather than checked in its own loop, since a station absent from `log` has nothing
			// to update or check this run anyway.
			val zeusConfigSQL = """
				SELECT `prefix`, `zeus_minutes`, `zeus_successful`
				FROM location
				WHERE `zeus_minutes` > 0
			""".trimIndent()
			val zeusConfig = md.queryList(zeusConfigSQL) {
				ZeusEntry(prefix = it.getString(1), minutes = it.getInt(2), successful = it.getInt(3) != 0)
			}.associateBy { it.prefix }

			println("Checking ${stats.size} weather2 stations...")
			for (entry in stats) {
				try {
					val prefix = entry.prefix
					println(prefix)

					val zeusInfo = zeusConfig[prefix]
					if (zeusInfo != null) {
						var delta = (Date().time / 1000).toInt() - entry.lastEpoch
						delta /= 60 // in minutes
						val successful = delta <= zeusInfo.minutes
						if (zeusInfo.successful != successful) {
							alerts.add("${if (successful) "✅ RESUMED" else "⚠️ FAILED"}: $prefix ($delta min)\n")
						}
						// Written every run, even when unchanged -- simpler to read than gating
						// the write on the same condition that gates the alert.
						val updateSQL = """
							UPDATE location SET `zeus_successful`=${if (successful) "1" else "0"}
							WHERE `prefix`='$prefix'
						""".trimIndent()
						md.executeUpdate(updateSQL)
					}
				}
				catch (e: Exception) {
					e.printStackTrace()
				}
			}

			println() // blank line separating this section's logging from what follows
		}
	}

	@JvmStatic
	fun main(args: Array<String>)
	{
		val result = check()
		if (result.alerts.isNotEmpty()) {
			val message = result.alerts.joinToString("")
			println(message)
			Ntfy.publish(NTFY_TOPIC, "Zeus Update", message)
		}
		if (result.dbError != null) {
			println(result.dbError)
			Ntfy.publish(NTFY_TOPIC, "Zeus DB Error", result.dbError, priority = "urgent", tags = "rotating_light")
		}

		println("Done")
	}

	/** Result of a `check()` run: normal FAILED/RESUMED alerts, plus a DB-unreachable error (e.g.
	 *  GoDaddy IP whitelist lapsed) if one aborted the run -- kept separate so the latter can be
	 *  pushed as its own urgent/red ntfy notification. */
	data class ZeusCheckResult(
		val alerts: List<String>,
		val dbError: String? = null
	)

	/** Keeps one zeus entry. A weather2 station is just a location with a single implicit
	 *  device, so it reuses this with `id`/`deviceName` left at their defaults. */
	data class ZeusEntry(
		val prefix: String,
		val id: Int = 0,
		val deviceName: String = "",
		val lastEpoch: Int = 0,
		val minutes: Int = 0,
		val successful: Boolean = false
	)

	/** Aggregate stats for one sensor's log rows */
	data class ZeusStats(
		val count: Int,
		val lastEpoch: Int
	)

	/** Last-logged epoch rolled up to one device (max across its sensors). A weather2 station is
	 *  just a location with a single implicit device, so it reuses this with `deviceId` left at
	 *  its default. */
	data class ZeusDeviceStats(
		val prefix: String,
		val deviceId: Int = 0,
		val lastEpoch: Int
	)

}
