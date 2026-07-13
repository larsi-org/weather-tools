package org.larsi.geonames

import java.net.URL
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import javax.xml.parsers.DocumentBuilderFactory

import org.w3c.dom.Document
import org.w3c.dom.Element

import org.larsi.util.MeteredDataConnector

object GeoNames
{
	/** XML tags that get reported */
	val TAGS = arrayOf(
			"observationTime", // 0 -> n/a
			"temperature", // 1 -> 1
			"dewPoint", // 2 -> 4
			"humidity", // 3 -> 7
			"clouds", // 4 -> 10
			"weatherCondition", // 5 -> 13
			"seaLevelPressure", // 6 -> 16
			"windDirection", // 7 -> 19
			"windSpeed" // 8 -> 22
	)

	var url: String? = null

	/**
	 * @param e   element to be searched
	 * @param tag enclosing tags
	 * @return the value of the text within a certain tag
	 */
	@JvmStatic
	fun getTextValue(e: Element, tag: String): String? {
		val nl = e.getElementsByTagName(tag)
		if (nl == null || nl.length < 1)
			return null
		val n = nl.item(0).firstChild ?: return null
		return n.nodeValue
	}

	@JvmStatic
	fun check() {
		val executor = Executors.newSingleThreadExecutor()

		val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
		formatter.timeZone = TimeZone.getTimeZone("GMT")

		val knotsToMPS = 1852.0 / 3600.0
		val FORMAT_1: NumberFormat = DecimalFormat("0.0")

		try {
			val md = MeteredDataConnector("larsi-weather")

			/** Get ICAO entries */
			val entries = md.queryList("SELECT Prefix FROM location;") { it.getString(1).uppercase() }

			// check all entries
			println("Checking ${entries.size} Stations...")
			for (entry in entries) {
				print("$entry - ")
				val prefix = entry.lowercase()
				url = "http://api.geonames.org/weatherIcao?username=larsi&ICAO=$prefix"
				// http://api.geonames.org/weatherIcao?username=larsi&ICAO=KROC
				// https://tgftp.nws.noaa.gov/data/observations/metar/stations/KROC.TXT
				// https://tgftp.nws.noaa.gov/data/observations/metar/decoded/KROC.TXT
				// https://tgftp.nws.noaa.gov/data/observations/metar/trend/KROC.TREND

				try {
					md.clearBatch()

					val future = executor.submit(GetDocument())
					val doc = future.get(20, TimeUnit.SECONDS)
					doc.documentElement.normalize()

					val root = doc.documentElement
					val nl = root.getElementsByTagName("observation")

					if (nl != null && nl.length > 0) {
						val obs = nl.item(0) as Element

						var time = 0
						for (typeID in TAGS.indices) {
							var value = getTextValue(obs, TAGS[typeID])

							if (typeID == 0) {
								if (value == null) {
									println("down")
									break
								}
								time = (formatter.parse(value).time / 1000).toInt()
								if (time <= md.getMaxDateTimeLog(prefix, "1,4,7,10,13,16,19,22")) {
									println("up to date")
									break
								}
								println("updating...")
							} else {
								if (value != null) {
									when (typeID) {
										4 -> { // clouds
											value = value.lowercase()
											value = when (value) {
												"n/a" -> "0"
												"few clouds" -> "2"
												"scattered clouds" -> "4"
												"broken clouds" -> "7"
												"overcast" -> "10"
												else -> value
											}
										}
										5 -> continue // weather condition (ignored for now)
										8 -> value = FORMAT_1.format(knotsToMPS * value.toFloat()) // wind speed
									}
									md.addBatch(md.insertLogSQL(prefix, time, 3 * typeID - 2, value))
								}
							}
						}
					}

					md.executeBatch()
				} catch (e: Exception) {
					println("Did not receive / could not parse data!")
				}
			}

			md.close()
		} catch (e: Exception) {
			e.printStackTrace()
		}

		executor.shutdownNow()
	}

	@JvmStatic
	fun main(args: Array<String>) {
		check()
		println("Done")
	}

	class GetDocument : Callable<Document>
	{
		override fun call(): Document {
			val dbf = DocumentBuilderFactory.newInstance()
			val db = dbf.newDocumentBuilder()
			return db.parse(URL(url).openStream())
		}
	}

}
