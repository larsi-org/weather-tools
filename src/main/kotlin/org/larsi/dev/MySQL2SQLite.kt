package org.larsi.dev

import org.larsi.util.DataBaseType
import org.larsi.util.MeteredDataConnector
import org.larsi.util.WeatherToolsConfig

object MySQL2SQLite {
	@JvmStatic
	fun createLog(filename: String, prefix: String) {
		println("Creating $prefix...")

		val dst = MeteredDataConnector(mapOf("filename" to filename), DataBaseType.SQLite)
		dst.executeUpdate("DROP TABLE IF EXISTS `${prefix}_log`")
		dst.executeUpdate("CREATE TABLE `${prefix}_log` (`DateTime` int, `SensorID` smallint, `Value` float)")
		dst.close()
	}

	@JvmStatic
	fun insertLog(filename: String, prefix: String, entries: MutableList<LogEntry>) {
		println("Inserting ${entries.size} entries into $prefix...")

		val dst = MeteredDataConnector(mapOf("filename" to filename), DataBaseType.SQLite)
		dst.executeUpdate("BEGIN")
		for (entry in entries)
			dst.insertLog(prefix, entry.dateTime, entry.sensorID, entry.value)
		dst.executeUpdate("COMMIT")
		dst.close()
	}

	@JvmStatic
	fun copyWeather(prefix: String) {
		println("Copying $prefix...")

		createLog(WeatherToolsConfig.SQLITE_FILE, prefix)

		val entries = mutableListOf<LogEntry>()
		val src = MeteredDataConnector("larsi-weather",
				DataBaseType.MySQL)
		val result = src.executeQuery("SELECT * FROM `${prefix}_log` WHERE `SensorID` IN (1,4,7,10,16,19,22) ORDER BY `DateTime`,`SensorID`")
		while (result.next())
			entries.add(LogEntry(result.getInt(1), result.getInt(2), result.getFloat(3)))
		result.close()
		src.close()

		insertLog(WeatherToolsConfig.SQLITE_FILE, prefix, entries)
	}

	@JvmStatic
	fun copyMetered(prefix: String) {
		println("Copying $prefix...")

		createLog(WeatherToolsConfig.SQLITE_FILE, prefix)

		val entries = mutableListOf<LogEntry>()
		var limit = 0
		while (true) {
			println(limit)

			val src = MeteredDataConnector("larsi-sensors",
					DataBaseType.MySQL)
			val result = src.executeQuery("SELECT * FROM `${prefix}_log` WHERE 1 ORDER BY `DateTime`,`SensorID` LIMIT $limit,10000")
			while (result.next())
				entries.add(LogEntry(result.getInt(1), result.getInt(2), result.getFloat(3)))
			result.close()
			src.close()

			if (entries.isEmpty())
				break
			insertLog(WeatherToolsConfig.SQLITE_FILE, prefix, entries)

			entries.clear()
			limit += 10000
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		try {
			copyWeather("kith")
			copyWeather("ksyr")
			copyMetered("thecoop")
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	class LogEntry(val dateTime: Int, val sensorID: Int, val value: Float)
}
