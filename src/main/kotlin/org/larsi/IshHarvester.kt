package org.larsi

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

import org.larsi.util.Icao
import org.larsi.util.WeatherToolsConfig

object IshHarvester
{
	private fun progress(i: Int) = "${String.format("%4s", i + 1)} / ${String.format("%4s", Icao.entries.size)}"

	@JvmStatic
	fun main(args: Array<String>) {
		try {
			for (year in 2020..2026) {
				println(">>> STARTING DOWNLOAD YEAR: $year <<<")

				val directory = File(WeatherToolsConfig.WORK_DIRECTORY, "$year")
				directory.mkdirs()

				for ((i, entry) in Icao.entries.withIndex()) {
					val prefix = entry.name.lowercase()
					val name = "${entry.usafWban}-$year.gz"

					val outName = File(directory, name).path

					if (File(outName).exists()) {
						println("${progress(i)} - $prefix: Already downloaded, skipping: $outName")
						continue
					}

					val inName = "https://noaa-isd-pds.s3.amazonaws.com/data/$year/$name"
					println("${progress(i)} - $prefix: $inName => $outName")

					try {
						URL(inName).openStream().use { input ->
							FileOutputStream(outName).use { output ->
								input.copyTo(output, bufferSize = 16 * 1024)
							}
						}
					} catch (e: FileNotFoundException) {
						System.err.println("${progress(i)} - $prefix: Could not find: $inName")
					} catch (e: Exception) {
						System.err.println(
								"${progress(i)} - $prefix: Download failed (${e.message}), deleting partial file and stopping so it can be resumed: $outName")
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
