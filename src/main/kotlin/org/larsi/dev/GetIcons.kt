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
	val DIRECTORY = File(File(System.getProperty("user.home"), "Desktop"), "temp")

	@JvmStatic
	fun main(args: Array<String>)
	{
		try {
			//MeteredDataConnector md = new MeteredDataConnector(WeatherToolsConfig.get("database", "metereddata"));
			val md = MeteredDataConnector("larsi-epw")

			val result = md.executeQuery("SELECT Prefix, Y(Location), X(Location) FROM location WHERE 1 ORDER BY Prefix")

			while (result.next()) {
				val prefix = result.getString(1).lowercase()
				val lat = result.getFloat(2)
				val lng = result.getFloat(3)

				println(prefix)

				val api = if (lat == 0.0f && lng == 0.0f) "center=0,0&zoom=0&size=360x180" else
					"center=$lat,$lng&zoom=$ZOOM&size=360x180&markers=color:red%7Clabel:.%7C$lat,$lng"

				// Not sure why, but "sensor" argument must be added (false for no GPS)
				val url = "http://maps.googleapis.com/maps/api/staticmap?$api&sensor=false&key=${WeatherToolsConfig.googleMaps!!["apiKey"]}"

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
			result.close()
			md.close()
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
