package org.larsi.ish

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

import org.larsi.dev.CreateDB

object IshHarvester
{
	val directory: String = "${System.getProperty("user.home")}/Desktop/noaa/"

	@JvmStatic
	fun main(args: Array<String>) {
		try {
			for (year in 2020..2026) {
				println(">>> STARTING DOWNLOAD YEAR: $year <<<")

				File("$directory$year").mkdirs()

				for ((i, entry) in CreateDB.Icao.entries.withIndex()) {
					val prefix = entry.prefix.lowercase()
					val name = "$year/${entry.usafWban}-$year.gz"

					val inName = "https://noaa-isd-pds.s3.amazonaws.com/data/$name"
					val outName = "$directory$name"

					if (File(outName).exists()) {
						println("${i + 1} / ${CreateDB.Icao.entries.size} - $prefix:\tAlready downloaded, skipping: $outName")
						continue
					}

					println("${i + 1} / ${CreateDB.Icao.entries.size} - $prefix:\t$inName => $outName")

					try {
						URL(inName).openStream().use { input ->
							FileOutputStream(outName).use { output ->
								input.copyTo(output, bufferSize = 16 * 1024)
							}
						}
					} catch (e: FileNotFoundException) {
						System.err.println(
								"${String.format("%1\$4s", i + 1)} / ${String.format("%1\$4s", CreateDB.Icao.entries.size)} - $prefix:\tCould not find: $inName")
					} catch (e: Exception) {
						System.err.println(
								"${String.format("%1\$4s", i + 1)} / ${String.format("%1\$4s", CreateDB.Icao.entries.size)} - $prefix:\tDownload failed (${e.message}), deleting partial file and stopping so it can be resumed: $outName")
						File(outName).delete()
						return
					}

					Thread.sleep(2000)
				}

				println(">>> $year DONE!!! <<<")
				println()
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
