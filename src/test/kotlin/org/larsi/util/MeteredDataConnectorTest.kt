package org.larsi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MeteredDataConnectorTest
{
	@Test
	fun `MySQL buildUrl includes host, db, and allowMultiQueries`()
	{
		assertEquals("jdbc:mysql://larsi.org/larsi-weather?allowMultiQueries=true", MeteredDataConnector.DataBaseType.MySQL.buildUrl("larsi.org", "larsi-weather"))
	}

	@Test
	fun `Postgres buildUrl includes host and db, no allowMultiQueries`()
	{
		assertEquals("jdbc:postgresql://larsi.org/larsi-weather", MeteredDataConnector.DataBaseType.Postgres.buildUrl("larsi.org", "larsi-weather"))
	}

	@Test
	fun `each database type has its JDBC driver class name`()
	{
		assertEquals("com.mysql.cj.jdbc.Driver", MeteredDataConnector.DataBaseType.MySQL.className)
		assertEquals("org.postgresql.Driver", MeteredDataConnector.DataBaseType.Postgres.className)
	}

	@Test
	fun `insertLogRowsSQL2 writes a whole observation as one INSERT with named columns`()
	{
		assertEquals(
			"INSERT INTO log (epoch, prefix, channel, value) VALUES (1795110720, 'kroc', 0, -1.7), (1795110720, 'kroc', 2, 81), (1795110720, 'kroc', 5, 4.6)",
			MeteredDataConnector.insertLogRowsSQL2("kroc", 1795110720, linkedMapOf(0 to "-1.7", 2 to "81", 5 to "4.6")))
	}
}
