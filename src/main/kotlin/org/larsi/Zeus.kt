package org.larsi

import java.util.Date

import org.larsi.util.MeteredDataConnector
import org.larsi.util.Ntfy

object Zeus
{
	const val NTFY_TOPIC = "larsi-zeus"

	@JvmStatic
	fun check(): MutableList<String>
	{
		val alerts = mutableListOf<String>()

		try {
			MeteredDataConnector("larsi-sensors").use { md ->
				var zeusEntries = md.queryList("SELECT `Prefix`, `ID` FROM `sensor`") {
					ZeusEntry(it.getString(1), it.getInt(2))
				}

				println("Checking ${zeusEntries.size} sensor entries...")
				for (entry in zeusEntries) {
					try {
						println("${entry.prefix}[${entry.id}]")
						val statsSQL = """
							SELECT COUNT(*) AS `Count`, MAX(`DateTime`) AS `LastEpoch`
							FROM ${entry.prefix}_log
							WHERE `SensorID`=${entry.id}
						""".trimIndent()
						val stats = md.queryList(statsSQL) {
							ZeusStats(it.getInt(1), it.getInt(2))
						}.first()
						val count = stats.count
						val lastEpoch = stats.lastEpoch
						val updateSQL = """
							UPDATE sensor SET `Count`=$count, `last_epoch`=$lastEpoch
							WHERE `Prefix`='${entry.prefix}' && `ID`=${entry.id}
						""".trimIndent()
						md.executeUpdate(updateSQL)
					}
					catch (e: Exception) {
						e.printStackTrace()
					}
				}

				// Roll each device's sensor stats up: last_epoch = latest of any of its sensors, count = sum of all of them
				val deviceStatsSQL = """
					SELECT `Prefix`, `device_id`, SUM(`Count`) AS `Count`, MAX(`last_epoch`) AS `LastEpoch`
					FROM sensor
					GROUP BY `Prefix`, `device_id`
				""".trimIndent()
				val deviceStats = md.queryList(deviceStatsSQL) {
					ZeusDeviceStats(it.getString(1), it.getInt(2), it.getInt(3), it.getInt(4))
				}

				println("Aggregating stats for ${deviceStats.size} devices...")
				for (entry in deviceStats) {
					try {
						println("${entry.prefix}[${entry.deviceId}]")
						val updateSQL = """
							UPDATE device SET `count`=${entry.count}, `last_epoch`=${entry.lastEpoch}
							WHERE `prefix`='${entry.prefix}' && `device_id`=${entry.deviceId}
						""".trimIndent()
						md.executeUpdate(updateSQL)
					}
					catch (e: Exception) {
						e.printStackTrace()
					}
				}

				// Check if values are outdated, per device (not per sensor -- sensor.ZeusMinutes/ZeusSuccessful are no longer read)
				val zeusEntriesSQL = """
					SELECT D.`prefix`, D.`device_id`, D.`last_epoch`, D.`zeus_minutes`, D.`zeus_successful`
					FROM device AS D, location AS L
					WHERE D.`zeus_minutes` > 0 && D.`prefix` = L.`Prefix`
				""".trimIndent()
				zeusEntries = md.queryList(zeusEntriesSQL) {
					ZeusEntry(
	                    prefix = it.getString(1),
	                    id = it.getInt(2),
	                    lastEpoch = it.getInt(3),
	                    minutes = it.getInt(4),
	                    successful = it.getInt(5) != 0
					)
				}

				println("Checking ${zeusEntries.size} device zeus entries...")
				for (entry in zeusEntries) {
					try {
						println("${entry.prefix}[${entry.id}]")
						var delta = (Date().time / 1000).toInt() - entry.lastEpoch
						delta /= 60 // in minutes
						val successful = delta <= entry.minutes
						if (entry.successful != successful) {
							alerts.add("${if (successful) "✅ RESUMED" else "⚠️ FAILED"}: ${entry.prefix}[${entry.id}] ($delta min)\n")
							val successfulSQL = """
								UPDATE device SET `zeus_successful`=${if (successful) "1" else "0"} WHERE `prefix`='${entry.prefix}' && `device_id`=${entry.id};
							""".trimIndent()
							md.executeUpdate(successfulSQL)
						}
					}
					catch (e: Exception) {
						e.printStackTrace()
					}
				}
			}
		}
		catch (e: Exception) {
			e.printStackTrace()
		}

		try {
			MeteredDataConnector("larsi-weather2").use { md ->
				// No per-station `sensor` table here (weather2's `sensor` is 14 rows of shared
				// display metadata, not per-instance data) and no `user` table (no per-station
				// ownership) -- station stats come straight from `log`, one rollup instead of two.
				val statsSQL = """
					SELECT `station`, COUNT(*) AS `Count`, MAX(`epoch`) AS `LastEpoch`
					FROM log
					GROUP BY `station`
				""".trimIndent()
				val stats = md.queryList(statsSQL) {
					ZeusLocationStats(it.getString(1), it.getInt(2), it.getInt(3))
				}

				println("Aggregating stats for ${stats.size} weather2 stations...")
				for (entry in stats) {
					try {
						println(entry.station)
						val updateSQL = """
							UPDATE location SET `count`=${entry.count}, `last_epoch`=${entry.lastEpoch}
							WHERE `Prefix`='${entry.station.lowercase()}'
						""".trimIndent()
						md.executeUpdate(updateSQL)
					}
					catch (e: Exception) {
						e.printStackTrace()
					}
				}

				// Check if values are outdated, per station
				val zeusEntriesSQL = """
					SELECT `Prefix`, `last_epoch`, `zeus_minutes`, `zeus_successful`
					FROM location
					WHERE `zeus_minutes` > 0
				""".trimIndent()
				val zeusEntries = md.queryList(zeusEntriesSQL) {
					ZeusStationEntry(
						prefix = it.getString(1),
						lastEpoch = it.getInt(2),
						minutes = it.getInt(3),
						successful = it.getInt(4) != 0
					)
				}

				println("Checking ${zeusEntries.size} weather2 zeus entries...")
				for (entry in zeusEntries) {
					try {
						println(entry.prefix)
						var delta = (Date().time / 1000).toInt() - entry.lastEpoch
						delta /= 60 // in minutes
						val successful = delta <= entry.minutes
						if (entry.successful != successful) {
							alerts.add("${if (successful) "✅ RESUMED" else "⚠️ FAILED"}: ${entry.prefix} ($delta min)\n")
							val successfulSQL = """
								UPDATE location SET `zeus_successful`=${if (successful) "1" else "0"} WHERE `Prefix`='${entry.prefix}';
							""".trimIndent()
							md.executeUpdate(successfulSQL)
						}
					}
					catch (e: Exception) {
						e.printStackTrace()
					}
				}
			}
		}
		catch (e: Exception) {
			e.printStackTrace()
		}

		println()

		return alerts
	}

	@JvmStatic
	fun main(args: Array<String>)
	{
		val alerts = check()
		if (alerts.isNotEmpty()) {
			val message = alerts.joinToString("")
			println(message)
			Ntfy.publish(NTFY_TOPIC, "Zeus Update", message)
		}

		println("Done")
	}

	/** Keeps one zeus entry */
	data class ZeusEntry(
		val prefix: String,
		val id: Int,
		val lastEpoch: Int = 0,
		val minutes: Int = 0,
		val successful: Boolean = false
	)

	/** Aggregate stats for one sensor's log rows */
	data class ZeusStats(
		val count: Int,
		val lastEpoch: Int
	)

	/** Sensor stats rolled up to one device */
	data class ZeusDeviceStats(
		val prefix: String,
		val deviceId: Int,
		val count: Int,
		val lastEpoch: Int
	)

	/** Aggregate stats for one weather2 station's log rows */
	data class ZeusLocationStats(
		val station: String,
		val count: Int,
		val lastEpoch: Int
	)

	/** Keeps one weather2 zeus entry (no numeric id -- Prefix is the station's own key) */
	data class ZeusStationEntry(
		val prefix: String,
		val lastEpoch: Int,
		val minutes: Int,
		val successful: Boolean
	)

}
