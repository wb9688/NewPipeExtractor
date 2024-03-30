package org.schabi.newpipe.extractor.services.soundcloud

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelInfoItemExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudStreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.ImageSuffix
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Collections
import java.util.Locale
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

object SoundcloudParsingHelper {
    // CHECKSTYLE:OFF
    // From https://web.archive.org/web/20210214185000/https://developers.soundcloud.com/docs/api/reference#tracks
    // and researches on images used by the websites
    // CHECKSTYLE:ON
    /*
    SoundCloud avatars and artworks are almost always squares.

    When we get non-square pictures, all these images variants are still squares, except the
    original and the crop versions provides images which are respecting aspect ratios.
    The websites only use the square variants.

    t2400x2400 and t3000x3000 variants also exists, but are not returned as several images are
    uploaded with a lower size than these variants: in this case, these variants return an upscaled
    version of the original image.
    */
    private val ALBUMS_AND_ARTWORKS_IMAGE_SUFFIXES: List<ImageSuffix> = java.util.List.of(ImageSuffix("mini", 16, 16, ResolutionLevel.LOW),
            ImageSuffix("t20x20", 20, 20, ResolutionLevel.LOW),
            ImageSuffix("small", 32, 32, ResolutionLevel.LOW),
            ImageSuffix("badge", 47, 47, ResolutionLevel.LOW),
            ImageSuffix("t50x50", 50, 50, ResolutionLevel.LOW),
            ImageSuffix("t60x60", 60, 60, ResolutionLevel.LOW),  // Seems to work also on avatars, even if it is written to be not the case in
            // the old API docs
            ImageSuffix("t67x67", 67, 67, ResolutionLevel.LOW),
            ImageSuffix("t80x80", 80, 80, ResolutionLevel.LOW),
            ImageSuffix("large", 100, 100, ResolutionLevel.LOW),
            ImageSuffix("t120x120", 120, 120, ResolutionLevel.LOW),
            ImageSuffix("t200x200", 200, 200, ResolutionLevel.MEDIUM),
            ImageSuffix("t240x240", 240, 240, ResolutionLevel.MEDIUM),
            ImageSuffix("t250x250", 250, 250, ResolutionLevel.MEDIUM),
            ImageSuffix("t300x300", 300, 300, ResolutionLevel.MEDIUM),
            ImageSuffix("t500x500", 500, 500, ResolutionLevel.MEDIUM))
    private val VISUALS_IMAGE_SUFFIXES: List<ImageSuffix> = java.util.List.of(ImageSuffix("t1240x260", 1240, 260, ResolutionLevel.MEDIUM),
            ImageSuffix("t2480x520", 2480, 520, ResolutionLevel.MEDIUM))
    private var clientId: String? = null
    val SOUNDCLOUD_API_V2_URL: String = "https://api-v2.soundcloud.com/"
    @Synchronized
    @Throws(ExtractionException::class, IOException::class)
    fun clientId(): String? {
        if (!Utils.isNullOrEmpty(clientId)) {
            return clientId
        }
        val dl: Downloader? = NewPipe.getDownloader()
        val download: Response? = dl!!.get("https://soundcloud.com")
        val responseBody: String? = download!!.responseBody()
        val clientIdPattern: String = ",client_id:\"(.*?)\""
        val doc: Document = Jsoup.parse((responseBody)!!)
        val possibleScripts: Elements = doc.select(
                "script[src*=\"sndcdn.com/assets/\"][src$=\".js\"]")
        // The one containing the client id will likely be the last one
        Collections.reverse(possibleScripts)
        val headers: Map<String?, List<String?>?> = java.util.Map.of<String?, List<String?>?>("Range", listOf("bytes=0-50000"))
        for (element: Element in possibleScripts) {
            val srcUrl: String = element.attr("src")
            if (!Utils.isNullOrEmpty(srcUrl)) {
                try {
                    clientId = Parser.matchGroup1(clientIdPattern, dl.get(srcUrl, headers)
                            .responseBody())
                    return clientId
                } catch (ignored: RegexException) {
                    // Ignore it and proceed to try searching other script
                }
            }
        }

        // Officially give up
        throw ExtractionException("Couldn't extract client id")
    }

    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String?): OffsetDateTime {
        try {
            return OffsetDateTime.parse(textualUploadDate)
        } catch (e1: DateTimeParseException) {
            try {
                return OffsetDateTime.parse(textualUploadDate, DateTimeFormatter
                        .ofPattern("yyyy/MM/dd HH:mm:ss +0000"))
            } catch (e2: DateTimeParseException) {
                throw ParsingException(("Could not parse date: \"" + textualUploadDate + "\""
                        + ", " + e1.message), e2)
            }
        }
    }

    /**
     * Call the endpoint "/resolve" of the API.
     *
     *
     *
     *
     * See https://developers.soundcloud.com/docs/api/reference#resolve
     */
    @Throws(IOException::class, ExtractionException::class)
    fun resolveFor(@Nonnull downloader: Downloader?, url: String?): JsonObject {
        val apiUrl: String = (SOUNDCLOUD_API_V2_URL + "resolve"
                + "?url=" + Utils.encodeUrlUtf8(url)
                + "&client_id=" + clientId())
        try {
            val response: String? = downloader.get(apiUrl, ServiceList.SoundCloud.getLocalization())
                    .responseBody()
            return JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }

    /**
     * Fetch the embed player with the apiUrl and return the canonical url (like the permalink_url
     * from the json API).
     *
     * @return the url resolved
     */
    @JvmStatic
    @Throws(IOException::class, ReCaptchaException::class)
    fun resolveUrlWithEmbedPlayer(apiUrl: String?): String {
        val response: String? = NewPipe.getDownloader().get(("https://w.soundcloud.com/player/?url="
                + Utils.encodeUrlUtf8(apiUrl)), ServiceList.SoundCloud.getLocalization()).responseBody()
        return Jsoup.parse((response)!!).select("link[rel=\"canonical\"]").first()
                .attr("abs:href")
    }

    /**
     * Fetch the widget API with the url and return the id (like the id from the json API).
     *
     * @return the resolved id
     */
    @JvmStatic
    @Throws(IOException::class, ParsingException::class)
    fun resolveIdWithWidgetApi(urlString: String?): String {
        // Remove the tailing slash from URLs due to issues with the SoundCloud API
        var fixedUrl: String? = urlString
        if (fixedUrl!!.get(fixedUrl.length - 1) == '/') {
            fixedUrl = fixedUrl.substring(0, fixedUrl.length - 1)
        }
        // Make URL lower case and remove m. and www. if it exists.
        // Without doing this, the widget API does not recognize the URL.
        fixedUrl = Utils.removeMAndWWWFromUrl(fixedUrl.lowercase(Locale.getDefault()))
        val url: URL?
        try {
            url = Utils.stringToURL(fixedUrl)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("The given URL is not valid")
        }
        try {
            val widgetUrl: String = ("https://api-widget.soundcloud.com/resolve?url="
                    + Utils.encodeUrlUtf8(url.toString())
                    + "&format=json&client_id=" + clientId())
            val response: String? = NewPipe.getDownloader().get(widgetUrl,
                    ServiceList.SoundCloud.getLocalization()).responseBody()
            val o: JsonObject = JsonParser.`object`().from(response)
            return JsonUtils.getValue(o, "id").toString()
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON response", e)
        } catch (e: ExtractionException) {
            throw ParsingException(
                    "Could not resolve id with embedded player. ClientId not extracted", e)
        }
    }

    /**
     * Fetch the users from the given API and commit each of them to the collector.
     *
     *
     * This differ from [.getUsersFromApi] in the sense
     * that they will always get MIN_ITEMS or more.
     *
     * @param minItems the method will return only when it have extracted that many items
     * (equal or more)
     */
    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getUsersFromApiMinItems(minItems: Int,
                                collector: ChannelInfoItemsCollector,
                                apiUrl: String?): String {
        var nextPageUrl: String = getUsersFromApi(collector, apiUrl)
        while (!nextPageUrl.isEmpty() && collector.getItems().size < minItems) {
            nextPageUrl = getUsersFromApi(collector, nextPageUrl)
        }
        return nextPageUrl
    }

    /**
     * Fetch the user items from the given API and commit each of them to the collector.
     *
     * @return the next streams url, empty if don't have
     */
    @Nonnull
    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getUsersFromApi(collector: ChannelInfoItemsCollector,
                        apiUrl: String?): String {
        val response: String? = NewPipe.getDownloader().get(apiUrl, ServiceList.SoundCloud.getLocalization())
                .responseBody()
        val responseObject: JsonObject
        try {
            responseObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        val responseCollection: JsonArray = responseObject.getArray("collection")
        for (o: Any? in responseCollection) {
            if (o is JsonObject) {
                collector.commit(SoundcloudChannelInfoItemExtractor(o))
            }
        }
        return getNextPageUrl(responseObject)
    }

    /**
     * Fetch the streams from the given API and commit each of them to the collector.
     *
     *
     * This differ from [.getStreamsFromApi] in the sense
     * that they will always get MIN_ITEMS or more items.
     *
     * @param minItems the method will return only when it have extracted that many items
     * (equal or more)
     */
    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getStreamsFromApiMinItems(minItems: Int,
                                  collector: StreamInfoItemsCollector,
                                  apiUrl: String?): String {
        var nextPageUrl: String = getStreamsFromApi(collector, apiUrl)
        while (!nextPageUrl.isEmpty() && collector.getItems().size < minItems) {
            nextPageUrl = getStreamsFromApi(collector, nextPageUrl)
        }
        return nextPageUrl
    }

    /**
     * Fetch the streams from the given API and commit each of them to the collector.
     *
     * @return the next streams url, empty if don't have
     */
    @Nonnull
    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    fun getStreamsFromApi(collector: StreamInfoItemsCollector,
                          apiUrl: String?,
                          charts: Boolean): String {
        val response: Response? = NewPipe.getDownloader().get(apiUrl, ServiceList.SoundCloud
                .getLocalization())
        if (response!!.responseCode() >= 400) {
            throw IOException("Could not get streams from API, HTTP " + response
                    .responseCode())
        }
        val responseObject: JsonObject
        try {
            responseObject = JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        val responseCollection: JsonArray = responseObject.getArray("collection")
        for (o: Any? in responseCollection) {
            if (o is JsonObject) {
                val `object`: JsonObject = o
                collector.commit(SoundcloudStreamInfoItemExtractor(if (charts) `object`.getObject("track") else `object`))
            }
        }
        return getNextPageUrl(responseObject)
    }

    @Nonnull
    private fun getNextPageUrl(@Nonnull response: JsonObject): String {
        try {
            var nextPageUrl: String = response.getString("next_href")
            if (!nextPageUrl.contains("client_id=")) {
                nextPageUrl += "&client_id=" + clientId()
            }
            return nextPageUrl
        } catch (ignored: Exception) {
            return ""
        }
    }

    @Throws(ReCaptchaException::class, ParsingException::class, IOException::class)
    fun getStreamsFromApi(collector: StreamInfoItemsCollector,
                          apiUrl: String?): String {
        return getStreamsFromApi(collector, apiUrl, false)
    }

    @Throws(ReCaptchaException::class, ParsingException::class, IOException::class)
    fun getInfoItemsFromApi(collector: MultiInfoItemsCollector,
                            apiUrl: String?): String {
        val response: Response? = NewPipe.getDownloader().get(apiUrl, ServiceList.SoundCloud.getLocalization())
        if (response!!.responseCode() >= 400) {
            throw IOException(("Could not get streams from API, HTTP "
                    + response.responseCode()))
        }
        val responseObject: JsonObject
        try {
            responseObject = JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        responseObject.getArray("collection")
                .stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .forEach(Consumer({ searchResult: JsonObject ->
                    val kind: String = searchResult.getString("kind", "")
                    when (kind) {
                        "user" -> collector.commit(SoundcloudChannelInfoItemExtractor(searchResult))
                        "track" -> collector.commit(SoundcloudStreamInfoItemExtractor(searchResult))
                        "playlist" -> collector.commit(SoundcloudPlaylistInfoItemExtractor(searchResult))
                    }
                }))
        var nextPageUrl: String
        try {
            nextPageUrl = responseObject.getString("next_href")
            if (!nextPageUrl.contains("client_id=")) {
                nextPageUrl += "&client_id=" + clientId()
            }
        } catch (ignored: Exception) {
            nextPageUrl = ""
        }
        return nextPageUrl
    }

    @Nonnull
    fun getUploaderUrl(`object`: JsonObject?): String? {
        val url: String = `object`!!.getObject("user").getString("permalink_url", "")
        return Utils.replaceHttpWithHttps(url)
    }

    @Nonnull
    fun getAvatarUrl(`object`: JsonObject?): String? {
        val url: String = `object`!!.getObject("user").getString("avatar_url", "")
        return Utils.replaceHttpWithHttps(url)
    }

    @Nonnull
    fun getUploaderName(`object`: JsonObject?): String {
        return `object`!!.getObject("user").getString("username", "")
    }

    @Nonnull
    @Throws(ParsingException::class)
    fun getAllImagesFromTrackObject(@Nonnull trackObject: JsonObject?): List<Image> {
        val artworkUrl: String? = trackObject!!.getString("artwork_url")
        if (artworkUrl != null) {
            return getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
        }
        val avatarUrl: String? = trackObject.getObject("user").getString("avatar_url")
        if (avatarUrl != null) {
            return getAllImagesFromArtworkOrAvatarUrl(avatarUrl)
        }
        throw ParsingException("Could not get track or track user's thumbnails")
    }

    @Nonnull
    fun getAllImagesFromArtworkOrAvatarUrl(
            originalArtworkOrAvatarUrl: String?): List<Image> {
        if (Utils.isNullOrEmpty(originalArtworkOrAvatarUrl)) {
            return listOf()
        }
        return getAllImagesFromImageUrlReturned( // Artwork and avatars are originally returned with the "large" resolution, which
                // is 100px wide
                originalArtworkOrAvatarUrl!!.replace("-large.", "-%s."),
                ALBUMS_AND_ARTWORKS_IMAGE_SUFFIXES)
    }

    @Nonnull
    fun getAllImagesFromVisualUrl(
            originalVisualUrl: String?): List<Image> {
        if (Utils.isNullOrEmpty(originalVisualUrl)) {
            return listOf()
        }
        return getAllImagesFromImageUrlReturned( // Images are originally returned with the "original" resolution, which may be
                // huge so don't include it for size purposes
                originalVisualUrl!!.replace("-original.", "-%s."),
                VISUALS_IMAGE_SUFFIXES)
    }

    private fun getAllImagesFromImageUrlReturned(
            @Nonnull baseImageUrlFormat: String,
            @Nonnull imageSuffixes: List<ImageSuffix>): List<Image> {
        return imageSuffixes.stream()
                .map(Function({ imageSuffix: ImageSuffix ->
                    Image(String.format(baseImageUrlFormat, imageSuffix.getSuffix()),
                            imageSuffix.getHeight(), imageSuffix.getWidth(),
                            imageSuffix.getResolutionLevel())
                }))
                .collect(Collectors.toUnmodifiableList())
    }
}
