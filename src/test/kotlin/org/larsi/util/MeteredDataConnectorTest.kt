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
}
