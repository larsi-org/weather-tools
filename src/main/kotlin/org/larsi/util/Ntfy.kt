package org.larsi.util

import java.net.HttpURLConnection
import java.net.URL

object Ntfy
{
	/** Posts a push notification to an ntfy.sh topic */
	@JvmStatic
	fun publish(topic: String, title: String, message: String)
	{
		try {
			val connection = URL("https://ntfy.sh/$topic").openConnection() as HttpURLConnection
			connection.requestMethod = "POST"
			connection.doOutput = true
			connection.setRequestProperty("Title", title)
			connection.outputStream.use { it.write(message.toByteArray()) }
			val responseCode = connection.responseCode
			if (responseCode != HttpURLConnection.HTTP_OK) {
				println("ntfy publish failed: HTTP $responseCode")
			}
			connection.disconnect()
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}
}
