package org.larsi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataBaseTypeTest
{
	@Test
	fun `MySQL buildUrl includes host, db, and allowMultiQueries`()
	{
		val properties = mapOf("host" to "larsi.org", "db" to "larsi-weather", "user" to "u", "password" to "p")
		assertEquals("jdbc:mysql://larsi.org/larsi-weather?allowMultiQueries=true", DataBaseType.MySQL.buildUrl(properties))
	}

	@Test
	fun `Postgres buildUrl includes host and db, no allowMultiQueries`()
	{
		val properties = mapOf("host" to "larsi.org", "db" to "larsi-weather")
		assertEquals("jdbc:postgresql://larsi.org/larsi-weather", DataBaseType.Postgres.buildUrl(properties))
	}

	@Test
	fun `SQLite buildUrl uses the filename property`()
	{
		val properties = mapOf("filename" to "sbir.sqlite")
		assertEquals("jdbc:sqlite:sbir.sqlite", DataBaseType.SQLite.buildUrl(properties))
	}

	@Test
	fun `only MySQL and Postgres require credentials`()
	{
		assertEquals(false, DataBaseType.SQLite.useCredentials)
		assertEquals(true, DataBaseType.MySQL.useCredentials)
		assertEquals(true, DataBaseType.Postgres.useCredentials)
	}

	@Test
	fun `each database type has its JDBC driver class name`()
	{
		assertEquals("org.sqlite.JDBC", DataBaseType.SQLite.className)
		assertEquals("com.mysql.cj.jdbc.Driver", DataBaseType.MySQL.className)
		assertEquals("org.postgresql.Driver", DataBaseType.Postgres.className)
	}
}
