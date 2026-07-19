package org.larsi

import java.util.Date

import org.larsi.util.MeteredDataConnector

object Zeus
{
	@JvmStatic
	fun check(): MutableMap<String, String>
	{
		val msg = mutableMapOf<String, String>()

		try {
			val md = MeteredDataConnector("larsi-sensors")

			var zeusEntries = md.queryList("SELECT `Prefix`, `ID` FROM `sensor`") {
				ZeusEntry(it.getString(1), it.getInt(2))
			}

			println("Checking ${zeusEntries.size} sensor entries...")
			for (entry in zeusEntries) {
				try {
					println("${entry.prefix}[${entry.id}]")
					val statsSQL = """
						SELECT COUNT(*) AS `Count`, MIN(`DateTime`) AS `DateTimeMin`, MAX(`DateTime`) AS `DateTimeMax`, MIN(`Value`) AS `ValueMin`, MAX(`Value`) AS `ValueMax`
						FROM ${entry.prefix}_log
						WHERE `SensorID`=${entry.id}
					""".trimIndent()
					val stats = md.queryList(statsSQL) {
						ZeusStats(it.getInt(1), it.getInt(2), it.getInt(3), it.getFloat(4), it.getFloat(5))
					}.first()
					val count = stats.count
					val dateTimeMin = stats.dateTimeMin
					val dateTimeMax = stats.dateTimeMax
					val valueMin = stats.valueMin
					val valueMax = stats.valueMax
					val updateSQL = """
						UPDATE sensor SET `Count`=$count, `DateTimeMin`=$dateTimeMin, `DateTimeMax`=$dateTimeMax, `ValueMin`=$valueMin, `ValueMax`=$valueMax
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
				SELECT S.`Prefix`, S.`ID`, S.`DateTimeMax`, S.`ZeusMinutes`, S.`ZeusSuccessful`, U.`Email`
				FROM sensor AS S, user AS U, location AS L
				WHERE S.`ZeusMinutes` > 0 && S.`Prefix` = L.`Prefix` && L.`OwnerID` = U.`ID`
			""".trimIndent()
			zeusEntries = md.queryList(zeusEntriesSQL) {
				ZeusEntry(
                    prefix = it.getString(1),
                    id = it.getInt(2),
                    dateTimeMax = it.getInt(3),
                    minutes = it.getInt(4),
                    successful = it.getInt(5) != 0,
                    email = it.getString(6)
				)
			}

			println("Checking ${zeusEntries.size} zeus entries...")
			for (entry in zeusEntries) {
				try {
					println("${entry.prefix}[${entry.id}]")
					var delta = (Date().time / 1000).toInt() - entry.dateTimeMax
					delta /= 60 // in minutes
					val successful = delta <= entry.minutes
					if (entry.successful != successful) {
						// email status change
						val line = "${if (successful) "RESUMED" else "FAILED"}: ${entry.prefix}[${entry.id}] ($delta min)\n"
						msg[entry.email] = (msg[entry.email] ?: "") + line
						val successfulSQL = """
							UPDATE sensor SET `ZeusSuccessful`=${if (successful) "1" else "0"} WHERE `Prefix`="${entry.prefix}" && `ID`=${entry.id};
						""".trimIndent()
						md.executeUpdate(successfulSQL)
					}
				}
				catch (e: Exception) {
					e.printStackTrace()
				}
			}

			md.close()
		}
		catch (e: Exception) {
			e.printStackTrace()
		}

		println()

		return msg
	}

	@JvmStatic
	fun main(args: Array<String>)
	{
		// Check and send emails
		//SendMail m = new SendMail(WeatherToolsConfig.get("email", "email"));
		for (email in check().entries) {
			println("${email.key}:")
			println(email.value)
			//m.send(email.getKey(), "Zeus Update", email.getValue());
		}

		println("Done")
	}

	/** Keeps one zeus entry */
	data class ZeusEntry(
		val prefix: String,
		val id: Int,
		val dateTimeMax: Int = 0,
		val minutes: Int = 0,
		val successful: Boolean = false,
		val email: String = ""
	)

	/** Aggregate stats for one sensor's log rows */
	data class ZeusStats(
		val count: Int,
		val dateTimeMin: Int,
		val dateTimeMax: Int,
		val valueMin: Float,
		val valueMax: Float
	)

}
