package org.larsi.util

import java.io.File

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object WeatherToolsConfig
{
	private const val CREDENTIALS = "credentials.json"
	private val GSON = Gson()

	val WORK_DIRECTORY = File("${System.getProperty("user.home")}/Desktop/weather-tools/")

	val database: Map<String, String>?
	val googleMaps: Map<String, String>?

	init {
		read().apply {
			database = extractRootProperties("database")
			googleMaps = extractRootProperties("googleMaps")
		}
	}

	private fun JsonObject.extractRootProperties(product: String) =
		getAsJsonObject(product)?.entrySet()?.associate { it.key to it.value.asString }

	private fun read() =
		GSON.fromJson(File(CREDENTIALS).readText(), JsonElement::class.java).asJsonObject
}
