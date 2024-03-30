package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonBuilder
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.ImageSuffix
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * A [YoutubePlaylistExtractor] for a mix (auto-generated playlist).
 * It handles URLs in the format of
 * `youtube.com/watch?v=videoId&list=playlistId`
 */
class YoutubeMixPlaylistExtractor(service: StreamingService,
                                  linkHandler: ListLinkHandler?) : PlaylistExtractor(service, linkHandler) {
    private var initialData: JsonObject? = null
    private var playlistData: JsonObject? = null
    private var cookieValue: String? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val localization: Localization? = getExtractorLocalization()
        val url: URL? = Utils.stringToURL(getUrl())
        val mixPlaylistId: String? = getId()
        val videoId: String? = Utils.getQueryValue(url, "v")
        val playlistIndexString: String? = Utils.getQueryValue(url, "index")
        val jsonBody: JsonBuilder<JsonObject?> = YoutubeParsingHelper.prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry()).value("playlistId", mixPlaylistId)
        if (videoId != null) {
            jsonBody.value("videoId", videoId)
        }
        if (playlistIndexString != null) {
            jsonBody.value("playlistIndex", playlistIndexString.toInt())
        }
        val body: ByteArray = JsonWriter.string(jsonBody.done()).toByteArray(StandardCharsets.UTF_8)

        // Cookie is required due to consent
        val headers: Map<String?, List<String?>?>? = YoutubeParsingHelper.getYouTubeHeaders()
        val response: Response? = getDownloader().postWithContentTypeJson(
                YoutubeParsingHelper.YOUTUBEI_V1_URL + "next?key=" + YoutubeParsingHelper.getKey() + YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER,
                headers, body, localization)
        initialData = JsonUtils.toJsonObject(YoutubeParsingHelper.getValidJsonResponseBody(response))
        playlistData = initialData!!
                .getObject("contents")
                .getObject("twoColumnWatchNextResults")
                .getObject("playlist")
                .getObject("playlist")
        if (Utils.isNullOrEmpty(playlistData)) {
            val ex: ExtractionException = ExtractionException("Could not get playlistData")
            if (!YoutubeParsingHelper.isConsentAccepted()) {
                throw ContentNotAvailableException(
                        "Consent is required in some countries to view Mix playlists",
                        ex)
            }
            throw ex
        }
        cookieValue = YoutubeParsingHelper.extractCookieValue(COOKIE_NAME, response)
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextAtKey(playlistData, "title")
            if (Utils.isNullOrEmpty(name)) {
                throw ParsingException("Could not get playlist name")
            }
            return name
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            try {
                return getThumbnailsFromPlaylistId(playlistData!!.getString("playlistId"))
            } catch (e: Exception) {
                try {
                    // Fallback to thumbnail of current video. Always the case for channel mixes
                    return getThumbnailsFromVideoId(initialData!!.getObject("currentVideoEndpoint")
                            .getObject("watchEndpoint").getString("videoId"))
                } catch (ignored: Exception) {
                }
                throw ParsingException("Could not get playlist thumbnails", e)
            }
        }
    override val uploaderUrl: String?
        get() {
            // YouTube mixes are auto-generated by YouTube
            return ""
        }
    override val uploaderName: String?
        get() {
            // YouTube mixes are auto-generated by YouTube
            return "YouTube"
        }

    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            // YouTube mixes are auto-generated by YouTube
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val streamCount: Long
        get() {
            // Auto-generated playlists always start with 25 videos and are endless
            return ListExtractor.Companion.ITEM_COUNT_INFINITE
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val description: Description
        get() {
            return Description.Companion.EMPTY_DESCRIPTION
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            collectStreamsFrom(collector, playlistData!!.getArray("contents"))
            val cookies: MutableMap<String?, String?> = HashMap()
            cookies.put(COOKIE_NAME, cookieValue)
            return InfoItemsPage(collector, getNextPageFrom(playlistData, cookies))
        }

    @Nonnull
    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(@Nonnull playlistJson: JsonObject?,
                                cookies: Map<String?, String?>?): Page {
        val lastStream: JsonObject? = (playlistJson!!.getArray("contents")
                .get(playlistJson.getArray("contents").size - 1) as JsonObject?)
        if (lastStream == null || lastStream.getObject("playlistPanelVideoRenderer") == null) {
            throw ExtractionException("Could not extract next page url")
        }
        val watchEndpoint: JsonObject = lastStream.getObject("playlistPanelVideoRenderer")
                .getObject("navigationEndpoint").getObject("watchEndpoint")
        val playlistId: String = watchEndpoint.getString("playlistId")
        val videoId: String = watchEndpoint.getString("videoId")
        val index: Int = watchEndpoint.getInt("index")
        val params: String = watchEndpoint.getString("params")
        val body: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(getExtractorLocalization(),
                getExtractorContentCountry())
                .value("videoId", videoId)
                .value("playlistId", playlistId)
                .value("playlistIndex", index)
                .value("params", params)
                .done())
                .toByteArray(StandardCharsets.UTF_8)
        return Page(YoutubeParsingHelper.YOUTUBEI_V1_URL + "next?key=" + YoutubeParsingHelper.getKey(), null, null, cookies, body)
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        if (!page.getCookies().containsKey(COOKIE_NAME)) {
            throw IllegalArgumentException("Cookie '" + COOKIE_NAME + "' is missing")
        }
        val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
        // Cookie is required due to consent
        val headers: Map<String?, List<String?>?>? = YoutubeParsingHelper.getYouTubeHeaders()
        val response: Response? = getDownloader().postWithContentTypeJson(page.getUrl(), headers,
                page.getBody(), getExtractorLocalization())
        val ajaxJson: JsonObject? = JsonUtils.toJsonObject(YoutubeParsingHelper.getValidJsonResponseBody(response))
        val playlistJson: JsonObject = ajaxJson!!.getObject("contents")
                .getObject("twoColumnWatchNextResults").getObject("playlist").getObject("playlist")
        val allStreams: JsonArray = playlistJson.getArray("contents")
        // Sublist because YouTube returns up to 24 previous streams in the mix
        // +1 because the stream of "currentIndex" was already extracted in previous request
        val newStreams: List<Any> = allStreams.subList(playlistJson.getInt("currentIndex") + 1, allStreams.size)
        collectStreamsFrom(collector, newStreams)
        return InfoItemsPage(collector, getNextPageFrom(playlistJson, page.getCookies()))
    }

    private fun collectStreamsFrom(@Nonnull collector: StreamInfoItemsCollector,
                                   streams: List<Any>?) {
        if (streams == null) {
            return
        }
        val timeAgoParser: TimeAgoParser? = getTimeAgoParser()
        streams.stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map(Function({ stream: JsonObject -> stream.getObject("playlistPanelVideoRenderer") }))
                .filter(Predicate({ obj: JsonObject? -> Objects.nonNull(obj) }))
                .map(Function({ streamInfo: JsonObject -> YoutubeStreamInfoItemExtractor(streamInfo, timeAgoParser) }))
                .forEachOrdered(Consumer({ extractor: YoutubeStreamInfoItemExtractor -> collector.commit(extractor) }))
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getThumbnailsFromPlaylistId(@Nonnull playlistId: String): List<Image?> {
        return getThumbnailsFromVideoId(YoutubeParsingHelper.extractVideoIdFromMixId(playlistId))
    }

    @Nonnull
    private fun getThumbnailsFromVideoId(@Nonnull videoId: String?): List<Image?> {
        val baseUrl: String = "https://i.ytimg.com/vi/" + videoId + "/"
        return IMAGE_URL_SUFFIXES_AND_RESOLUTIONS.stream()
                .map(Function({ imageSuffix: ImageSuffix ->
                    Image(baseUrl + imageSuffix.getSuffix(),
                            imageSuffix.getHeight(), imageSuffix.getWidth(),
                            imageSuffix.getResolutionLevel())
                }))
                .collect(Collectors.toUnmodifiableList())
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val playlistType: PlaylistType?
        get() {
            return YoutubeParsingHelper.extractPlaylistTypeFromPlaylistId(playlistData!!.getString("playlistId"))
        }

    companion object {
        private val IMAGE_URL_SUFFIXES_AND_RESOLUTIONS: List<ImageSuffix> = java.util.List.of( // sqdefault and maxresdefault image resolutions are not available on all
                // videos, so don't add them in the list of available resolutions
                ImageSuffix("default.jpg", 90, 120, ResolutionLevel.LOW),
                ImageSuffix("mqdefault.jpg", 180, 320, ResolutionLevel.MEDIUM),
                ImageSuffix("hqdefault.jpg", 360, 480, ResolutionLevel.MEDIUM))

        /**
         * YouTube identifies mixes based on this cookie. With this information it can generate
         * continuations without duplicates.
         */
        @JvmField
        val COOKIE_NAME: String = "VISITOR_INFO1_LIVE"
    }
}
