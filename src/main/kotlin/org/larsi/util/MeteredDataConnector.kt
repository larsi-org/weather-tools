package org.larsi.util

import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.logging.Logger

class MeteredDataConnector : Closeable
{
	enum class DataBaseType(
		val className: String,
		val buildUrl: (host: String, db: String) -> String)
	{
		MySQL(
	        className = "com.mysql.cj.jdbc.Driver",
	        buildUrl = { host, db -> "jdbc:mysql://$host/$db?allowMultiQueries=true" }),
		Postgres(
	        className = "org.postgresql.Driver",
	        buildUrl = { host, db -> "jdbc:postgresql://$host/$db" })
	}

	companion object {
		private val LOG = Logger.getLogger(MeteredDataConnector::class.java.name)

		val DATA_BASE_TYPE = DataBaseType.MySQL
	}

	private var sqlConnection: Connection? = null

	private var sqlStatement: Statement? = null

	constructor(db: String) {
		connect(db)
	}

	/**
	 * Reads the credentials and attempts to make a connection to the correct database.<br></br>
	 */
	private fun connect(db: String) {
		val credentials = WeatherToolsConfig.database!!
		connect(
			DATA_BASE_TYPE.className,
			DATA_BASE_TYPE.buildUrl(credentials["host"]!!, db),
			credentials["username"],
			credentials["password"]
		)
	}

	/**
	 * Reads the credentials and attempts to make a connection to the correct database.<br></br>
	 */
	private fun connect(className: String, url: String, username: String?, password: String?) {
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
	fun executeQuery(s: String): ResultSet =
		sqlStatement!!.executeQuery(s)

	/**
	 * Attempts to submit the statement given<br></br>
	 * Usually an 'INSERT' 'DELETE' or 'UPDATE' statement
	 */
	@Throws(SQLException::class)
	fun executeUpdate(s: String): Int =
		sqlStatement!!.executeUpdate(s)

	@Throws(SQLException::class)
	fun clearBatch() =
		sqlStatement!!.clearBatch()

	@Throws(SQLException::class)
	fun addBatch(sqlString: String) =
		sqlStatement!!.addBatch(sqlString)

	@Throws(SQLException::class)
	fun executeBatch(): IntArray =
		sqlStatement!!.executeBatch()

	/**
	 * Close the statement and connection
	 */
	@Throws(SQLException::class)
	override fun close()
	{
		sqlStatement!!.close()
		sqlConnection!!.close()
	}

	fun insertLogSQL2(stationId: String, dateTime: Int, sensorID: Int, value: Any?): String =
		"INSERT INTO log VALUES ($dateTime, '$stationId', $sensorID, $value)"

	fun emptyLogSQL2(prefix: String, sensorID: Int): String =
		"DELETE FROM log WHERE station='$prefix' AND sensor_id=$sensorID"

	fun cleanLogSQL2(prefix: String, sensorIDs: String, timeMin: Int, timeMax: Int): String =
		"DELETE FROM log WHERE station='$prefix' AND sensor_id IN ($sensorIDs) AND epoch >= $timeMin AND epoch <= $timeMax"

	fun updateLastEpochSQL2(prefix: String, epoch: Int): String =
		"UPDATE location SET last_epoch=$epoch WHERE Prefix='$prefix'"

	fun optimizeTable(name: String): String =
		"OPTIMIZE TABLE $name"

	@Throws(SQLException::class)
	fun getMaxDateTimeLog2(prefix: String, sensorID: Int): Int =
		queryList("SELECT MAX(epoch) FROM log WHERE station='$prefix' AND sensor_id=$sensorID") { it.getInt(1) }.first()

	@Throws(SQLException::class)
	fun getMaxDateTimeLog2(prefix: String, sensorIDs: String): Int =
		queryList("SELECT MAX(epoch) FROM log WHERE station='$prefix' AND sensor_id IN ($sensorIDs)") { it.getInt(1) }.first()

	@Throws(SQLException::class)
	fun <T> queryList(statement: String, mapper: (ResultSet) -> T): List<T> {
		val result = executeQuery(statement)
		val list = mutableListOf<T>()
		while (result.next())
			list.add(mapper(result))
		result.close()
		return list
	}
}
