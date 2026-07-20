package org.larsi.dev

import java.io.File
import java.io.FileOutputStream
import java.net.URL

import org.larsi.util.MeteredDataConnector
import org.larsi.util.WeatherToolsConfig

object GetIcons
{
	/*
	 * Use ZOOM = 6 for icons
	 * - convert to RGB
	 * - crop 192x96
	 * - reduce to 96x48
	 * - export to png
	 */
	const val ZOOM = 6
	//final static int  ZOOM      = 9;
	val DIRECTORY = File(WeatherToolsConfig.WORK_DIRECTORY, "icons")

	@JvmStatic
	fun main(args: Array<String>)
	{
		try {
			MeteredDataConnector("larsi-epw").use { md ->
				val locations = md.queryList("SELECT Prefix, Y(Location), X(Location) FROM location WHERE 1 ORDER BY Prefix") {
					Location(it.getString(1).lowercase(), it.getFloat(2), it.getFloat(3))
				}

				for ((prefix, lat, lng) in locations) {
					println(prefix)

					val api = if (lat == 0.0f && lng == 0.0f) "center=0,0&zoom=0&size=360x180" else
						"center=$lat,$lng&zoom=$ZOOM&size=360x180&markers=color:red%7Clabel:.%7C$lat,$lng"

					val url = "https://maps.googleapis.com/maps/api/staticmap?$api&key=${WeatherToolsConfig.googleMaps!!["apiKey"]}"

					try {
						URL(url).openStream().use { input ->
							FileOutputStream(File(DIRECTORY, "$prefix.png")).use { output ->
								input.copyTo(output, bufferSize = 16 * 1024)
							}
						}
					}
					catch (e: java.io.IOException) {
						e.printStackTrace()
					}

					Thread.sleep(2000L) // wait 5 seconds
				}
			}
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}

	data class Location(val prefix: String, val lat: Float, val lng: Float)
}
