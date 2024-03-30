package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionItem
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Extract subscriptions from a Google takeout export
 */
class YoutubeSubscriptionExtractor(youtubeService: YoutubeService) : SubscriptionExtractor(youtubeService, listOf<ContentSource>(ContentSource.INPUT_STREAM)) {
    override val relatedUrl: String?
        get() {
            return "https://takeout.google.com/takeout/custom/youtube"
        }

    @Throws(ExtractionException::class)
    public override fun fromInputStream(contentInputStream: InputStream?): List<SubscriptionItem> {
        return fromJsonInputStream(contentInputStream)
    }

    @Throws(ExtractionException::class)
    public override fun fromInputStream(contentInputStream: InputStream?,
                                        contentType: String): List<SubscriptionItem> {
        when (contentType) {
            "json", "application/json" -> return fromJsonInputStream(contentInputStream)
            "csv", "text/csv", "text/comma-separated-values" -> return fromCsvInputStream(contentInputStream)
            "zip", "application/zip" -> return fromZipInputStream(contentInputStream)
            else -> throw InvalidSourceException("Unsupported content type: " + contentType)
        }
    }

    @Throws(ExtractionException::class)
    fun fromJsonInputStream(contentInputStream: InputStream?): List<SubscriptionItem> {
        val subscriptions: JsonArray
        try {
            subscriptions = JsonParser.array().from(contentInputStream)
        } catch (e: JsonParserException) {
            throw InvalidSourceException("Invalid json input stream", e)
        }
        var foundInvalidSubscription: Boolean = false
        val subscriptionItems: MutableList<SubscriptionItem> = ArrayList()
        for (subscriptionObject: Any? in subscriptions) {
            if (!(subscriptionObject is JsonObject)) {
                foundInvalidSubscription = true
                continue
            }
            val subscription: JsonObject = subscriptionObject.getObject("snippet")
            val id: String = subscription.getObject("resourceId").getString("channelId", "")
            if (id.length != 24) { // e.g. UCsXVk37bltHxD1rDPwtNM8Q
                foundInvalidSubscription = true
                continue
            }
            subscriptionItems.add(SubscriptionItem(service.getServiceId(),
                    BASE_CHANNEL_URL + id, subscription.getString("title", "")))
        }
        if (foundInvalidSubscription && subscriptionItems.isEmpty()) {
            throw InvalidSourceException("Found only invalid channel ids")
        }
        return subscriptionItems
    }

    @Throws(ExtractionException::class)
    fun fromZipInputStream(contentInputStream: InputStream?): List<SubscriptionItem> {
        try {
            ZipInputStream(contentInputStream).use({ zipInputStream ->
                var zipEntry: ZipEntry
                while ((zipInputStream.getNextEntry().also({ zipEntry = it })) != null) {
                    if (zipEntry.getName().lowercase(Locale.getDefault()).endsWith(".csv")) {
                        try {
                            val csvItems: List<SubscriptionItem> = fromCsvInputStream(zipInputStream)

                            // Return it only if it has items (it exits early if it's the wrong file
                            // format), otherwise try the next file
                            if (csvItems.size > 0) {
                                return csvItems
                            }
                        } catch (e: ExtractionException) {
                            // Ignore error and go to next file
                        }
                    }
                }
            })
        } catch (e: IOException) {
            throw InvalidSourceException("Error reading contents of zip file", e)
        }
        throw InvalidSourceException(("Unable to find a valid subscriptions.csv file"
                + " (try extracting and selecting the csv file)"))
    }

    @Throws(ExtractionException::class)
    fun fromCsvInputStream(contentInputStream: InputStream?): List<SubscriptionItem> {
        // Expected format of CSV file:
        // Channel Id,Channel Url,Channel Title
        //UC1JTQBa5QxZCpXrFSkMxmPw,http://www.youtube.com/channel/UC1JTQBa5QxZCpXrFSkMxmPw,Raycevick
        //UCFl7yKfcRcFmIUbKeCA-SJQ,http://www.youtube.com/channel/UCFl7yKfcRcFmIUbKeCA-SJQ,Joji
        //
        // Notes:
        //      It's always 3 columns
        //      The first line is always a header
        //      Header names are different based on the locale
        //      Fortunately the data is always the same order no matter what locale
        var currentLine: Int = 0
        var line: String? = ""
        try {
            BufferedReader(InputStreamReader(contentInputStream)).use({ br ->
                val subscriptionItems: MutableList<SubscriptionItem> = ArrayList()

                // ignore header and skip first line
                currentLine = 1
                line = br.readLine()
                while ((br.readLine().also({ line = it })) != null) {
                    currentLine++

                    // Exit early if we've read the first few lines and we haven't added any items
                    // It's likely we're in the wrong file
                    if (currentLine > 5 && subscriptionItems.size == 0) {
                        break
                    }

                    // First comma
                    val i1: Int = line.indexOf(",")
                    if (i1 == -1) {
                        continue
                    }

                    // Second comma
                    val i2: Int = line.indexOf(",", i1 + 1)
                    if (i2 == -1) {
                        continue
                    }

                    // Third comma or line length
                    var i3: Int = line.indexOf(",", i2 + 1)
                    if (i3 == -1) {
                        i3 = line.length
                    }

                    // Channel URL from second entry
                    val channelUrl: String = line
                            .substring(i1 + 1, i2)
                            .replace("http://", "https://")
                    if (!channelUrl.startsWith(BASE_CHANNEL_URL)) {
                        continue
                    }

                    // Channel title from third entry
                    val channelTitle: String = line.substring(i2 + 1, i3)
                    val newItem: SubscriptionItem = SubscriptionItem(service.getServiceId(), channelUrl, channelTitle)
                    subscriptionItems.add(newItem)
                }
                return subscriptionItems
            })
        } catch (e: IOException) {
            if (line == null) {
                line = "<null>"
            } else if (line!!.length > 10) {
                line = line!!.substring(0, 10) + "..."
            }
            throw InvalidSourceException(("Error reading CSV file on line = \"" + line
                    + "\", line number = " + currentLine), e)
        }
    }

    companion object {
        private val BASE_CHANNEL_URL: String = "https://www.youtube.com/channel/"
    }
}
