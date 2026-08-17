package org.larsi.util

import java.net.HttpURLConnection
import java.net.URL

object Ntfy
{
	/** Posts a push notification to an ntfy.sh topic. `priority` (min/low/default/high/urgent)
	 *  and `tags` (comma-separated ntfy emoji shortcodes, e.g. "rotating_light") are optional --
	 *  omitted headers fall back to ntfy's own defaults. */
	@JvmStatic
	fun publish(topic: String, title: String, message: String, priority: String? = null, tags: String? = null)
	{
		try {
			val connection = URL("https://ntfy.sh/$topic").openConnection() as HttpURLConnection
			connection.requestMethod = "POST"
			connection.doOutput = true
			connection.setRequestProperty("Title", title)
			if (priority != null) connection.setRequestProperty("Priority", priority)
			if (tags != null) connection.setRequestProperty("Tags", tags)
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
