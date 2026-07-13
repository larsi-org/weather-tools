package org.larsi.dev

import java.io.File
import java.io.IOException
import java.nio.charset.Charset

object CreateEPW
{
	@Throws(IOException::class)
	@JvmStatic
	fun createLocation()
	{
		print("INSERT INTO `location` VALUES ")
		for (src in File("C:/Users/larsi/Desktop/epw/").list { directory, name -> name.lowercase().endsWith(".epw") }!!) {

			val weatherFile = File("C:/Users/larsi/Desktop/epw/", src)
			try {
				weatherFile.bufferedReader(Charset.defaultCharset()).use { reader ->
					val line = reader.readLine().split(",").toTypedArray()
					if (line.size > 7) {
						val label = line[1]
						val state = line[2]
						val country = line[3]
						val prefix = line[5]
						val latitude = line[6].toFloat()
						val longitude = line[7].toFloat()
						val timezone = line[8].toFloat()
						val elevation = line[9].toFloat()

						val description = "$label${if (state == "-") "" else ", $state"}, $country"
						println(
								"('$prefix', '${description.replace("'", "-")}', GeomFromText('POINT($longitude $latitude)'), $elevation, $timezone, '${src.replace(".epw", "").replace("'", "-")}'),")
					}
				}
			} catch (e: NumberFormatException) {
				println("ERROR: $src")
				// we ignore bad weather files
			} catch (e: IOException) {
				// we ignore bad weather files
			}
		}
	}

	@Throws(IOException::class)
	@JvmStatic
	fun main(args: Array<String>)
	{
		createLocation()
	}

}
