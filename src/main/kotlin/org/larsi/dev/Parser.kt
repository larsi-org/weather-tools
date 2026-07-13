package org.larsi.dev

import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

import org.w3c.dom.Element
import org.xml.sax.SAXException

import org.larsi.util.Icao

object Parser
{
	val ishData = mutableMapOf<String, String>()

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
	fun inStations(call: String): Icao? = Icao.entries.find { it.name == call }

	@JvmStatic
	fun parseIshHistory() {
		val duplicates = mutableMapOf<String, Int>()
		for (entry in Icao.entries)
			duplicates[entry.name] = 0

		// final URL stations =new
		// URL("ftp://ftp.ncdc.noaa.gov/pub/data/noaa/isd-history.txt");
		// BufferedReader in = new BufferedReader(new
		// InputStreamReader(stations.openStream()));
		File("doc/isd-history.txt").useLines(Charset.defaultCharset()) { lines ->
		for (l in lines) {
			if (l.length < 99)
				continue

			val usaf = l.substring(0, 6)
			val wban = l.substring(7, 12)
			val name = l.substring(13, 42).trim()
			val ctry = l.substring(43, 45).trim().uppercase()
			val state = l.substring(48, 50).trim().uppercase()
			val call = l.substring(51, 55).trim().uppercase()
			val lat = l.substring(57, 64).trim()
			val lon = l.substring(65, 73).trim()
			val elev = l.substring(74, 81).trim()
			val start = l.substring(82, 90).trim()
			val end = l.substring(91, 99).trim()

			if (start.equals("NO DATA", ignoreCase = true) || end.equals("NO DATA", ignoreCase = true))
				continue

			if (name.isEmpty())
				continue
			if (call.isEmpty())
				continue

			if (lat == "-99.999")
				continue
			if (lon == "-999.999")
				continue
			if (lat == "-9999.9")
				continue

			if (usaf == "999999")
				continue
			if (wban == "99999")
				continue

			if (!call.startsWith("K"))
				continue
			if (call.matches(Regex(".*[0-9].*")))
				continue
			if (ctry != "US")
				continue

			val endYear = end.substring(0, 4).toInt()
			if (endYear < 2020)
				continue

			val startYear = start.substring(0, 4).toInt()
			if (startYear >= 2015)
				continue

			val stationsEntry = inStations(call) ?: continue

			duplicates[call] = if (duplicates.containsKey(call)) 1 + duplicates[call]!! else 1

			ishData[call] =
					"${stationsEntry.priority}\"$call\", \"$usaf-$wban\", \"$ctry\", \"$state\", \"$lat\", \"$lon\", \"$elev\", \"${stationsEntry.stationName}\", \"${stationsEntry.priority}\""

		}
		}

		println("Duplicates: ")
		for (entry in duplicates.entries) {
			val key = entry.key
			val value = entry.value
			if (value != 1)
				println("$key => $value")
		}
		println()
	}

	@JvmStatic
	fun parseStations() {
		val stationsUrl = URL("http://weather.rap.ucar.edu/surface/stations.txt")
		InputStreamReader(stationsUrl.openStream()).useLines { lines ->
		for (l in lines) {
			if (l.length < 83)
				continue

			// String state = line.substring( 0, 2).toUpperCase();
			val icao = l.substring(20, 24).uppercase()
			val priority = l.substring(79, 80)
			// String country = line.substring(81, 83).toUpperCase();

			if (!(priority == "0" || priority == "1" || priority == "2" || priority == "3" ||
					priority == "4" || priority == "5" || icao == "KELM" || icao == "KITH" ||
					icao == "KROC"))
				continue

			if (icao[0] != 'K')
				continue

			val url = "http://api.geonames.org/weatherIcao?username=larsi&ICAO=$icao"
			try {
				val dbf = DocumentBuilderFactory.newInstance()
				val db = dbf.newDocumentBuilder()
				val doc = db.parse(URL(url).openStream())
				doc.documentElement.normalize()

				val root = doc.documentElement
				val nl = root.getElementsByTagName("observation")

				if (nl != null && nl.length > 0) {
					val obs = nl.item(0) as Element

					val snm = getTextValue(obs, "stationName")
					println("$priority { \"$icao\", \"$snm\", \"$priority\" },")
				}
			} catch (e: java.io.IOException) {
				e.printStackTrace()
			} catch (e: ParserConfigurationException) {
				e.printStackTrace()
			} catch (e: SAXException) {
				e.printStackTrace()
			}
		}
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		try {
			// parseStations();

			parseIshHistory()

			for (entry in ishData.entries)
				println("\t\t{ ${entry.value} },")

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

}