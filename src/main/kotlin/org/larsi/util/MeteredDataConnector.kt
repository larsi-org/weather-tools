package org.larsi.util

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.logging.Logger

class MeteredDataConnector
{
	companion object {
		private val LOG = Logger.getLogger(MeteredDataConnector::class.java.name)
	}

	private var sqlConnection: Connection? = null

	private var sqlStatement: Statement? = null

	constructor(credentials: Map<String, String>, dataBaseType: DataBaseType = DataBaseType.MySQL)
	{
		connect(dataBaseType, credentials)
	}

	constructor(db: String, dataBaseType: DataBaseType = DataBaseType.MySQL) : this(WeatherToolsConfig.database!!.plus("db" to db), dataBaseType)

	/**
	 * Reads the credentials and attempts to make a connection to the correct database.<br></br>
	 */
	private fun connect(dataBaseType: DataBaseType, properties: Map<String, String>) =
		connect(
			dataBaseType.className,
			dataBaseType.buildUrl(properties),
			if (dataBaseType.useCredentials) properties["username"] else null,
			if (dataBaseType.useCredentials) properties["password"] else null
		)

	/**
	 * Reads the credentials and attempts to make a connection to the correct database.<br></br>
	 */
	private fun connect(className: String, url: String, username: String?, password: String?)
	{
		LOG.info("Connecting to \"$url\"")

		Class.forName(className)
		sqlConnection = if (username == null || password == null) DriverManager.getConnection(url) else DriverManager.getConnection(url, username, password)
		sqlStatement = sqlConnection!!.createStatement()
	}

	/**
	 * Attempts to submit the statement given<br></br>
	 * Usually a 'SELECT' statement
	 */
	@Throws(SQLException::class)
	fun executeQuery(statement: String): ResultSet =
		sqlStatement!!.executeQuery(statement)

	/**
	 * Attempts to submit the statement given<br></br>
	 * Usually an 'INSERT' 'DELETE' or 'UPDATE' statement
	 */
	@Throws(SQLException::class)
	fun executeUpdate(statement: String): Int =
		sqlStatement!!.executeUpdate(statement)

	@Throws(SQLException::class)
	fun clearBatch() =
		sqlStatement!!.clearBatch()

	@Throws(SQLException::class)
	fun addBatch(sql: String) =
		sqlStatement!!.addBatch(sql)

	@Throws(SQLException::class)
	fun executeBatch(): IntArray =
		sqlStatement!!.executeBatch()

	/**
	 * Close the statement and connection
	 */
	@Throws(SQLException::class)
	fun close()
	{
		sqlStatement!!.close()
		sqlConnection!!.close()
	}

	fun insertLogSQL2(stationId: String, dateTime: Int, sensorID: Int, value: Any?): String =
		"INSERT INTO log VALUES ($dateTime, '$stationId', $sensorID, $value)"

	fun insertLogSQL(prefix: String, dateTime: Int, sensorID: Int, value: Any?): String =
		"INSERT INTO ${prefix}_log VALUES ('$dateTime', '$sensorID', '$value')"

	@Throws(SQLException::class)
	fun insertLog(prefix: String, dateTime: Int, sensorID: Int, value: Any?) =
		executeUpdate(insertLogSQL(prefix, dateTime, sensorID, value))

	fun emptyLogSQL2(prefix: String, sensorID: Int): String =
		"DELETE FROM log WHERE station='$prefix' AND sensor_id=$sensorID"

	fun emptyLogSQL(prefix: String, sensorID: Int): String =
		"DELETE FROM ${prefix}_log WHERE SensorID=$sensorID"

	fun cleanLogSQL2(prefix: String, sensorIDs: String, timeMin: Int, timeMax: Int): String =
		"DELETE FROM log WHERE station='$prefix' AND sensor_id IN ($sensorIDs) AND epoch >= $timeMin AND epoch <= $timeMax"

	fun cleanLogSQL(prefix: String, sensorIDs: String, timeMin: Int, timeMax: Int): String =
		"DELETE FROM ${prefix}_log WHERE SensorID IN ($sensorIDs) AND DateTime >= $timeMin AND DateTime <= $timeMax"

	fun optimizeTable(name: String): String =
		"OPTIMIZE TABLE $name"

	@Throws(SQLException::class)
	fun getMaxDateTimeLog2(prefix: String, sensorID: Int): Int =
		queryInt("SELECT MAX(epoch) FROM log WHERE station='$prefix' AND sensor_id=$sensorID")

	@Throws(SQLException::class)
	fun getMaxDateTimeLog(prefix: String, sensorID: Int): Int =
		queryInt("SELECT MAX(DateTime) FROM ${prefix}_log WHERE SensorID=$sensorID")

	@Throws(SQLException::class)
	fun getMaxDateTimeLog2(prefix: String, sensorIDs: String): Int =
		queryInt("SELECT MAX(epoch) FROM log WHERE station='$prefix' AND sensor_id IN ($sensorIDs)")

	@Throws(SQLException::class)
	fun getMaxDateTimeLog(prefix: String, sensorIDs: String): Int =
		queryInt("SELECT MAX(DateTime) FROM ${prefix}_log WHERE SensorID IN ($sensorIDs)")

	@Throws(SQLException::class)
	fun <T> queryList(statement: String, mapper: (ResultSet) -> T): List<T> {
		val result = executeQuery(statement)
		val list = mutableListOf<T>()
		while (result.next())
			list.add(mapper(result))
		result.close()
		return list
	}

	@Throws(SQLException::class)
	private fun queryInt(sql: String): Int =
		queryList(sql) { it.getInt(1) }.first()
}
