package org.larsi.util

enum class DataBaseType(
	val className: String,
	val useCredentials: Boolean,
	val buildUrl: (Map<String, String>) -> String)
{
	SQLite(
        className = "org.sqlite.JDBC",
        useCredentials = false,
        buildUrl = { properties -> "jdbc:sqlite:${properties["filename"]}" }),
	MySQL(
        className = "com.mysql.cj.jdbc.Driver",
        useCredentials = true,
        buildUrl = { properties -> "jdbc:mysql://${properties["host"]}/${properties["db"]}?allowMultiQueries=true" }),
	Postgres(
        className = "org.postgresql.Driver",
        useCredentials = true,
        buildUrl = { properties -> "jdbc:postgresql://${properties["host"]}/${properties["db"]}" })
}
