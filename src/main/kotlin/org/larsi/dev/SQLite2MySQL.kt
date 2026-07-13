package org.larsi.dev

import org.larsi.util.DataBaseType
import org.larsi.util.MeteredDataConnector
import org.larsi.util.WeatherToolsConfig

object SQLite2MySQL {
	@JvmStatic
	fun insertLog(db: String, prefix: String, entries: MutableList<LogEntry>) {
		println("Inserting ${entries.size} entries into $prefix...")

		val dst = MeteredDataConnector(db, DataBaseType.MySQL)

		dst.clearBatch()
		for (entry in entries)
			dst.addBatch(dst.insertLogSQL(prefix, entry.dateTime, entry.sensorID, entry.value))

		dst.executeBatch()

		dst.close()
	}

	@JvmStatic
	fun copyMetered(prefix: String) {
		println("Copying $prefix...")

		val entries = mutableListOf<LogEntry>()
		var limit = 0
		while (true) {
			println(limit)

			val src = MeteredDataConnector(mapOf("filename" to WeatherToolsConfig.SQLITE_FILE),
					DataBaseType.SQLite)
			val result = src.executeQuery("SELECT * FROM `${prefix}_log` WHERE 1 ORDER BY `DateTime`,`SensorID` LIMIT $limit,10000")
			while (result.next())
				entries.add(LogEntry(result.getInt(1), result.getInt(2), result.getFloat(3)))
			result.close()
			src.close()

			if (entries.isEmpty())
				break
			insertLog("larsi-sensors", prefix, entries)

			entries.clear()
			limit += 10000
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		try {
			copyMetered("thecoop")
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	class LogEntry(val dateTime: Int, val sensorID: Int, val value: Float)
}
