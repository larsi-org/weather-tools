package org.larsi

import java.net.URL
import java.sql.SQLException
import java.text.ParsePosition
import java.text.SimpleDateFormat

import javax.xml.namespace.QName
import javax.xml.rpc.ParameterMode

import org.apache.axis.Constants
import org.apache.axis.client.Call
import org.apache.axis.client.Service

import org.larsi.util.MeteredDataConnector

object NDFD2
{
	val entries = mutableListOf<NDFDEntry>()

	const val SENSOR_CNT = 6

	val sensorIDs = intArrayOf( // SENSOR_CNT
		16, // ICAO Temperature (predicted)
		17, // ICAO Dew Point Temperature (predicted)
		21, // ICAO Wind Speed (predicted)
		20, // ICAO Wind Direction (predicted)
		22, // ICAO Clouds (predicted)
		18, // ICAO Humidity (predicted)
	)

	// local variables used in search
	val searchString = arrayOf( // SENSOR_CNT
		"<temperature type=\"hourly\"",        // dry bulb temperature (C)
		"<temperature type=\"dew point\"",     // dew point temperature (C)
		"<wind-speed type=\"sustained\"",      // wind speed (m/s)
		"<direction type=\"wind\"",            // wind direction (degrees true)
		"<cloud-amount type=\"total\"",        // cloud cover (percent)
		"<humidity type=\"relative\""          // humidity (percent)
	)

	/** keeps the date formatter for parsing the time stamp */
	val SDF = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sszzzzzzzzz")

	@JvmStatic
	fun check() {
		try {
			MeteredDataConnector("larsi-weather2").use { md ->
				/** Get ICAO entries */
				entries.addAll(md.queryList("SELECT Prefix, Y(Location) AS Latitude, X(Location) AS Longitude FROM location") {
					NDFDEntry(it.getString(1).uppercase(), it.getString(2), it.getString(3))
				}.filter { it.prefix.startsWith("K") })

				// check all entries
				println("Checking ${entries.size} Entries...")
				for (entry in entries) {
					try {
						print("Checking: ${entry.prefix}")

						// collects data from NDFD site, saves it as xml-formatted response string requestResponse
						val response = soapRequest(entry.latitude, entry.longitude)

						// parses xml response, saves data to class variables
						parseXMLresponse(response)

						// update database
						update(md, entry.prefix)

					} catch (e: Exception) {
						System.err.println("Soap call failed!")
					}

				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	@JvmStatic
	fun main(args: Array<String>) {
		check()
		println("Done")
	}

	/** dry bulb temperature */
	var values = mutableListOf<MutableList<Float>>()

	/** dry bulb temperature timestamps (UnixTimestamp: seconds since January 1, 1970 00:00 UTC) */
	var times = mutableListOf<MutableList<Int>>()

	/** collects data from NDFD site, saves xml-formatted response string to <code>requestResponse<\code>
	 * @throws RemoteException
	 * @throws ServiceException
	 * @throws MalformedURLException */
	fun soapRequest(latitude: String, longitude: String): String
	{
		val server = "http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php"
		val soapAction = "http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl#NDFDgen"

		print(" SOAP call ")
		val service = Service()
		val call = service.createCall() as Call
		call.setTargetEndpointAddress(URL(server))
		call.setOperationName(QName("uri:DWMLgen", "NDFDgen"))
		call.setUseSOAPAction(true)
		call.setSOAPActionURI(soapAction)

		call.addParameter("latitude", Constants.XSD_STRING, ParameterMode.IN)
		call.addParameter("longitude", Constants.XSD_STRING, ParameterMode.IN)
		call.addParameter("product", Constants.XSD_STRING, ParameterMode.IN)
		call.addParameter("startTime", Constants.XSD_DATETIME, ParameterMode.IN)
		call.addParameter("endTime", Constants.XSD_DATETIME, ParameterMode.IN)
		call.addParameter("Unit", Constants.XSD_STRING, ParameterMode.IN)
		call.addParameter("weatherParameters", QName("weatherParameters"), ParameterMode.IN)
		call.setReturnType(Constants.XSD_STRING)

		val weatherParams = arrayOfNulls<String>(1)
		weatherParams[0] = "temp = TRUE"

		val ret = call.invoke(arrayOf<Any>(
			latitude, // <latitude>
			longitude, //<longitude>
			"time-series", //<product>
			"2004-01-01T00:00:00", //<startTime>
			"2030-01-12T00:00:00", //<endTime>
			"m", //<Unit>
			weatherParams
		))

		return ret as String
	}

	/** collects data from NDFD site, saves xml-formatted response string to <code>requestResponse<\code> */
	fun parseXMLresponse(response: String)
	{
		// splits response string by lines
		val responseByLineNum = response.split("\n").toTypedArray()

		val lineNumber = IntArray(SENSOR_CNT)
		val timeLayout = arrayOfNulls<String>(SENSOR_CNT)
		val tlLineNumber = IntArray(SENSOR_CNT)

		values.clear()
		times.clear()
        (0 until SENSOR_CNT).forEach { _ ->
            values.add(mutableListOf())
            times.add(mutableListOf())
        }

		// finds line number and "time-layout" string associated with each data type of interest
		for (i in responseByLineNum.indices) {
			for (j in 0 until SENSOR_CNT) {
				if (responseByLineNum[i].trim().startsWith(searchString[j])) {
					lineNumber[j] = i
					timeLayout[j] = responseByLineNum[i].split("\"").toTypedArray()[5]
				}
			}
		}

		// collect data values
		for (j in 0 until SENSOR_CNT) {
			var i = lineNumber[j] + 2
			while (!responseByLineNum[i].trim().startsWith("</")) {
				var value: Float
				if (responseByLineNum[i].trim().endsWith("xsi:nil=\"true\"/>")) {
					value = -999.0f
				} else {
					value = responseByLineNum[i].substring(responseByLineNum[i].indexOf(">") + 1, responseByLineNum[i].lastIndexOf("<")).toFloat()
					if (j == 4) value = (value / 10f + 0.5f).toInt().toFloat() // adjust cloud coverage from % to 10th
				}
				values[j].add(value)
				i++
			}
		}

		// the "time-layout" string is used to identify the timestamps associated with the given data type
		for (i in responseByLineNum.indices) {
			for (j in 0 until SENSOR_CNT) {
				if (responseByLineNum[i].trim().startsWith("<layout-key>${timeLayout[j]}")) {
					tlLineNumber[j] = i
				}
			}
		}

		// collect timestamps
		for (j in 0 until SENSOR_CNT) {
			var i = tlLineNumber[j] + 1
			while (!responseByLineNum[i].trim().startsWith("</")) {
				val value = responseByLineNum[i].substring(responseByLineNum[i].indexOf(">") + 1, responseByLineNum[i].lastIndexOf("<"))
				val p = ParsePosition(0)
				val d = SDF.parse("${value.substring(0, 19)}GMT${value.substring(19)}", p)
				val unixTime = (d.time / 1000).toInt()
				times[j].add(unixTime)
				i++
			}
		}
	}

	fun update(md: MeteredDataConnector, prefix: String)
	{
		try {
			md.clearBatch()
			for (j in 0 until SENSOR_CNT) {
				print(" $j")

				try {
					val sensorID = sensorIDs[j]
					md.addBatch(md.emptyLogSQL2(prefix, sensorID))
					val v = values[j]
					val t = times[j]
					val lastCurrent = md.getMaxDateTimeLog2(prefix, sensorID - 16)
					if (prefix.startsWith("K")) {
						val cnt = minOf(v.size, t.size)
						for (i in 0 until cnt) {
							val tPredicted = t[i]
							if (tPredicted > lastCurrent)
								md.addBatch(md.insertLogSQL2(prefix, tPredicted, sensorID, v[i]))
						}
					}
				} catch (e: SQLException) {
					e.printStackTrace()
				}
			}
			md.executeBatch()
		} catch (e1: SQLException) {
			e1.printStackTrace()
		}
		println()
	}

	/** Keeps one enabled check entry */
	data class NDFDEntry(val prefix: String, val latitude: String, val longitude: String)
}
