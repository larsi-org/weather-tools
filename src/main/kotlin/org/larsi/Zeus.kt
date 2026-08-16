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

				// Check if values are outdated
				val zeusEntriesSQL = """
					SELECT S.`Prefix`, S.`ID`, S.`last_epoch`, S.`ZeusMinutes`, S.`ZeusSuccessful`
					FROM sensor AS S, user AS U, location AS L
					WHERE S.`ZeusMinutes` > 0 && S.`Prefix` = L.`Prefix` && L.`OwnerID` = U.`ID`
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

				println("Checking ${zeusEntries.size} zeus entries...")
				for (entry in zeusEntries) {
					try {
						println("${entry.prefix}[${entry.id}]")
						var delta = (Date().time / 1000).toInt() - entry.lastEpoch
						delta /= 60 // in minutes
						val successful = delta <= entry.minutes
						if (entry.successful != successful) {
							alerts.add("${if (successful) "✅ RESUMED" else "⚠️ FAILED"}: ${entry.prefix}[${entry.id}] ($delta min)\n")
							val successfulSQL = """
								UPDATE sensor SET `ZeusSuccessful`=${if (successful) "1" else "0"} WHERE `Prefix`='${entry.prefix}' && `ID`=${entry.id};
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

}
