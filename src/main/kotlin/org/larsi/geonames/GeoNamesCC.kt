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

object GeoNamesCC
{
	/** XML tags that get reported */
	val TAGS = arrayOf(
			"observationTime", // 0 -> n/a
			"temperature", // 1 -> 0
			"dewPoint", // 2 -> 2
			"humidity", // 3 -> 4
			"seaLevelPressure", // 4 -> 6
			"windDirection", // 5 -> 8
			"windSpeed", // 6 -> 10
			"clouds", // 7 -> 12
			"weatherCondition" // 8 -> 14
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
			val md = MeteredDataConnector("larsi-weathercc")

			/** Get ICAO entries */
			val entries = md.queryList("SELECT id, prefix FROM location;") { it.getString(1).uppercase() }

			// check all entries
			println("Checking ${entries.size} Stations...")
			for (entry in entries) {
				print("$entry - ")
				val prefix = entry.uppercase()
				url = "http://api.geonames.org/weatherIcao?username=larsi&ICAO=$prefix"
				// http://weather.noaa.gov/pub/data/observations/metar/stations/KITH.TXT
				// http://weather.noaa.gov/pub/data/observations/metar/decoded/KROC.TXT
				// http://weather.noaa.gov/pub/data/observations/metar/trend/KROC.TREND

				try {
					md.clearBatch()

					val future = executor.submit(GetDocument())
					val doc = future.get(20, TimeUnit.SECONDS)
					doc.documentElement.normalize()

					val root = doc.documentElement
					val nl = root.getElementsByTagName("observation")

					if (nl != null && nl.length > 0) {
						val obs = nl.item(0) as Element

						// nl = obs.getChildNodes();
						// final StringBuilder sb = new StringBuilder();
						// for (int i = 0; i < nl.getLength(); i++) {
						// Node node = nl.item(i);
						// if (node.getNodeType() == Node.ELEMENT_NODE) {
						// sb.append(node.getFirstChild().getNodeValue()).append(',');
						// //System.out.println(node.getNodeName() + "=" +
						// node.getFirstChild().getNodeValue());
						// }
						// }
						// System.out.println(sb.toString());

						var time = 0
						for (typeID in TAGS.indices) {
							var value = getTextValue(obs, TAGS[typeID])

							if (typeID == 0) {
								if (value == null) {
									println("down")
									break
								}
								time = (formatter.parse(value).time / 1000).toInt()
								if (time <= md.getMaxDateTimeLogCC(prefix, "0,2,4,6,8,10,12,14")) {
									println("up to date")
									break
								}
								println("updating...")
							} else {
								if (value != null) {
									when (typeID) {
										6 -> value = FORMAT_1.format(knotsToMPS * value.toFloat()) // wind speed
										7 -> { // clouds
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
										8 -> continue // weather condition (ignored for now)
									}
									md.addBatch(md.insertLogSQLCC(prefix, time, 2 * (typeID - 1), value))
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
