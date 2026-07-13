package org.larsi.dev

import java.io.File

import org.larsi.util.MeteredDataConnector

object RenameEPW
{
	val DIRECTORY = File(File(System.getProperty("user.home"), "Desktop"), "png")

	@JvmStatic
	fun main(args: Array<String>)
	{
		try {
			val md = MeteredDataConnector("larsi-epw")

			val result = md.executeQuery("SELECT Prefix, FileName FROM location WHERE 1")

			while (result.next()) {
				val name_from = "${result.getString(1)}.png"
				val name_to = "${result.getString(2)}.png"

				println("$name_from => $name_to")
				val file_from = File(DIRECTORY, name_from)
				val file_to = File(DIRECTORY, name_to)
				file_from.renameTo(file_to)
			}
			result.close()
			md.close()
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
