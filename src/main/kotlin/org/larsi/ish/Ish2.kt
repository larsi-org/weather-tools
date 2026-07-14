package org.larsi.ish

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.zip.GZIPInputStream

import org.larsi.util.Icao
import org.larsi.util.MeteredDataConnector
import org.larsi.util.PsychrometricsUtil

object Ish2
{
	val directory: String = "${System.getProperty("user.home")}/Desktop/noaa/"

	var iWork: Int = 0

	var formatter: DateFormat = SimpleDateFormat("yyyyMMddHHmm")

	// REM offset
	var iREM_IndexOf = 0

	var utc = 0

	// Fields making up the Mandatory Data Section.
	var sMDS_Dir = ""
	var sMDS_Spd = ""
	var sMDS_TempSign = ""
	var sMDS_Temp = ""
	var sMDS_DewpSign = ""
	var sMDS_Dewp = ""
	var sMDS_Slp = ""

	// Fields making up the GF1 element
	const val iGF1_Length = 26
	var sGF1_Skc = ""

	@JvmStatic
	fun main(args: Array<String>) {
		formatter.timeZone = TimeZone.getTimeZone("GMT")

		for (year in 2020..2020) {
			println(">>> STARTING UPLOAD YEAR: $year <<<")

			for ((idx, entry) in Icao.entries.withIndex()) {
				val prefix = entry.name.uppercase()
				val inName = "$directory$year/${entry.usafWban}-$year.gz"

				if (!File(inName).exists())
					continue
				println("$prefix: $inName")

				try {
					val lines = GZIPInputStream(FileInputStream(inName)).bufferedReader(Charset.defaultCharset()).use { it.readLines() }

					process(lines[0])
					val utcStart = utc
					process(lines[lines.size - 1])
					val utcEnd = utc

					val md = MeteredDataConnector("larsi-weather2")
					md.clearBatch()

					md.addBatch(md.cleanLogSQL2(prefix, "0,1,2,3,4,5,6,7", utcStart, utcEnd))

					val sb = StringBuilder()
					sb.append("INSERT INTO log (epoch,station,sensor_id,value) VALUES ")
					var rows = 0
					for (line in lines) {
						process(line)

						// 0 Temperature
						if (sMDS_Temp != "****")
							insertLog(sb, ++rows, prefix, utc, 0, sMDS_Temp)

						// 1 Dew Point
						if (sMDS_Dewp != "****")
							insertLog(sb, ++rows, prefix, utc, 1, sMDS_Dewp)

						// 2 Humidity
						if (sMDS_Temp != "****" && sMDS_Dewp != "****")
							insertLog(sb, ++rows, prefix, utc, 2,
									PsychrometricsUtil.getRH(sMDS_Temp.toFloat(), sMDS_Dewp.toFloat()))

						// 3 Pressure
						if (sMDS_Slp != "******")
							insertLog(sb, ++rows, prefix, utc, 3, sMDS_Slp)

						// 4 Wind Direction
						if (sMDS_Dir != "***")
							insertLog(sb, ++rows, prefix, utc, 4, sMDS_Dir.toInt())

						// 5 Wind Speed
						if (sMDS_Spd != "****")
							insertLog(sb, ++rows, prefix, utc, 5, sMDS_Spd)

						// 6 Clouds
						if (sGF1_Skc != "**")
							insertLog(sb, ++rows, prefix, utc, 6, sGF1_Skc)
					} // while read
					md.addBatch(sb.toString())

					md.addBatch(md.optimizeTable("log"))

					println("${1 + idx}/${Icao.entries.size}: $rows records - UTC:  $utcStart - $utcEnd - Go!")
					md.executeBatch()

					md.close()
				} catch (e: FileNotFoundException) {
					println("Could not open: $inName")
				}
				Thread.sleep(20000)
			}

			println(">>> $year DONE!!! <<<")
			println()
		}

	} // End of main()

fun process(line: String) {
		// See where the REM section begins
		iREM_IndexOf = line.indexOf("REM")
		if (iREM_IndexOf == -1)
			iREM_IndexOf = 9999 // If no REM section then set to high value

		getCDS(line) // Fields making up the Control Data Section.
		getMDS(line) // Fields making up the Mandatory Data Section.
		getGF1(line) // Fields making up the GF1 element.
	}

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getCDS - Parse the observation timestamp out of the Control Data Section.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	fun getCDS(p_sRecd: String) {
		try {
			utc = (formatter.parse(p_sRecd.substring(15, 27)).time / 1000).toInt()
		} catch (e: ParseException) {
			e.printStackTrace()
		}
	} // End of getCDS

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getMDS - Get MDS section and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	fun getMDS(p_sRecd: String) {
		// Extract fields making up the Mandatory Data Section.
		sMDS_Dir = p_sRecd.substring(60, 63)
		sMDS_Spd = p_sRecd.substring(65, 69)
		sMDS_TempSign = p_sRecd.substring(87, 88)
		sMDS_Temp = p_sRecd.substring(88, 92)
		sMDS_DewpSign = p_sRecd.substring(93, 94)
		sMDS_Dewp = p_sRecd.substring(94, 98)
		sMDS_Slp = p_sRecd.substring(99, 104)

		if (sMDS_Dir == "999") {
			sMDS_Dir = "***"
		}

		if (sMDS_Spd == "9999") {
			sMDS_Spd = "****"
		} else {
			sMDS_Spd = (sMDS_Spd.toInt() / 10f).toString()
		}

		if (sMDS_Temp == "9999") {
			sMDS_Temp = "****"
		} else {
			iWork = sMDS_Temp.toInt() // Convert to integer
			if (sMDS_TempSign == "-") {
				iWork *= -1
			}
			sMDS_Temp = (iWork / 10f).toString()
		}

		if (sMDS_Dewp == "9999") {
			sMDS_Dewp = "****"
		} else {
			iWork = sMDS_Dewp.toInt() // Convert to integer
			if (sMDS_DewpSign == "-") {
				iWork *= -1
			}
			sMDS_Dewp = (iWork / 10f).toString()
		}

		if (sMDS_Slp == "99999") {
			sMDS_Slp = "******"
		} else {
			sMDS_Slp = (sMDS_Slp.toInt() / 10f).toString()
		}
	} // End of getMDS

	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	// getGF1 - Get GF1 element and format its output.
	// =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
	fun getGF1(p_sRecd: String) {
		sGF1_Skc = "**"
		val iGF1_IndexOf = p_sRecd.indexOf("GF1")
		if ((iGF1_IndexOf >= 0) && (iGF1_IndexOf < iREM_IndexOf)) {
			val sGF1 = p_sRecd.substring(iGF1_IndexOf, iGF1_IndexOf + iGF1_Length)
			sGF1_Skc = sGF1.substring(3, 5)
		}

		if ((iGF1_IndexOf >= 0) && (iGF1_IndexOf < iREM_IndexOf)) {
			if (sGF1_Skc == "99") {
				sGF1_Skc = "**"
			} else {
				try {
					iWork = sGF1_Skc.toInt() // Convert to integer
				} catch (e: Exception) {
					System.out.println("DateTime=[$utc] sGF1_Skc value could not be converted to integer=[${sGF1_Skc}]")
					sGF1_Skc = "**" // Data error. Set to missing.
				}
				if (sGF1_Skc != "**") {
					sGF1_Skc = when {
						iWork < 8 -> iWork.toString()
						iWork == 8 -> "10"
						else -> "**"
					}
				}
			}
		}
	} // End of getGF1

	fun insertLog(sb: StringBuilder, rows: Int, prefix: String, dateTime: Int, sensorID: Int, value: Any) {
		if (rows != 1)
			sb.append(',')
		sb.append('(').append(dateTime).append(",'").append(prefix).append("',").append(sensorID).append(",'").append(value).append("')")
	}

}
