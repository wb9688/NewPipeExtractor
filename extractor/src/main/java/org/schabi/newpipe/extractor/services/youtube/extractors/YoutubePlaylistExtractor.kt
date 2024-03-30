package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
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
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

class YoutubePlaylistExtractor(service: StreamingService,
                               linkHandler: ListLinkHandler?) : PlaylistExtractor(service, linkHandler) {
    private var browseResponse: JsonObject? = null
    private var playlistInfo: JsonObject? = null

    @get:Throws(ParsingException::class)
    private var uploaderInfo: JsonObject? = null
        private get() {
            if (field == null) {
                field = browseResponse!!.getObject(SIDEBAR)
                        .getObject("playlistSidebarRenderer")
                        .getArray("items")
                        .stream()
                        .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                        .filter(Predicate({ item: JsonObject ->
                            item.getObject("playlistSidebarSecondaryInfoRenderer")
                                    .getObject("videoOwner")
                                    .has(VIDEO_OWNER_RENDERER)
                        }))
                        .map(Function({ item: JsonObject ->
                            item.getObject("playlistSidebarSecondaryInfoRenderer")
                                    .getObject("videoOwner")
                                    .getObject(VIDEO_OWNER_RENDERER)
                        }))
                        .findFirst()
                        .orElseThrow(Supplier({ ParsingException("Could not get uploader info") }))
            }
            return field
        }

    private var playlistHeader: JsonObject? = null
        private get() {
            if (field == null) {
                field = browseResponse!!.getObject("header")
                        .getObject("playlistHeaderRenderer")
            }
            return field
        }
    private var isNewPlaylistInterface: Boolean = false
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val localization: Localization? = getExtractorLocalization()
        val body: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("browseId", "VL" + getId())
                .value("params", "wgYCCAA%3D") // Show unavailable videos
                .done())
                .toByteArray(StandardCharsets.UTF_8)
        browseResponse = YoutubeParsingHelper.getJsonPostResponse("browse", body, localization)
        YoutubeParsingHelper.defaultAlertsCheck(browseResponse)
        isNewPlaylistInterface = checkIfResponseIsNewPlaylistInterface()
    }

    /**
     * Whether the playlist response is using only the new playlist design.
     *
     *
     *
     * This new response changes how metadata is returned, and does not provide author thumbnails.
     *
     *
     *
     *
     * The new response can be detected by checking whether a header JSON object is returned in the
     * browse response (the old returns instead a sidebar one).
     *
     *
     * @return Whether the playlist response is using only the new playlist design
     */
    private fun checkIfResponseIsNewPlaylistInterface(): Boolean {
        // The "old" playlist UI can be also returned with the new one
        return browseResponse!!.has("header") && !browseResponse!!.has(SIDEBAR)
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getPlaylistInfo(): JsonObject? {
        if (playlistInfo == null) {
            playlistInfo = browseResponse!!.getObject(SIDEBAR)
                    .getObject("playlistSidebarRenderer")
                    .getArray("items")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .filter(Predicate({ item: JsonObject -> item.has("playlistSidebarPrimaryInfoRenderer") }))
                    .map(Function({ item: JsonObject -> item.getObject("playlistSidebarPrimaryInfoRenderer") }))
                    .findFirst()
                    .orElseThrow(Supplier({ ParsingException("Could not get playlist info") }))
        }
        return playlistInfo
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextFromObject(getPlaylistInfo()!!.getObject("title"))
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            return browseResponse!!.getObject("microformat")
                    .getObject("microformatDataRenderer")
                    .getString("title")
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            val playlistMetadataThumbnailsArray: JsonArray
            if (isNewPlaylistInterface) {
                playlistMetadataThumbnailsArray = playlistHeader!!.getObject("playlistHeaderBanner")
                        .getObject("heroPlaylistThumbnailRenderer")
                        .getObject("thumbnail")
                        .getArray("thumbnails")
            } else {
                playlistMetadataThumbnailsArray = playlistInfo!!.getObject("thumbnailRenderer")
                        .getObject("playlistVideoThumbnailRenderer")
                        .getObject("thumbnail")
                        .getArray("thumbnails")
            }
            if (!Utils.isNullOrEmpty(playlistMetadataThumbnailsArray)) {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(playlistMetadataThumbnailsArray)
            }

            // This data structure is returned in both layouts
            val microFormatThumbnailsArray: JsonArray = browseResponse!!.getObject("microformat")
                    .getObject("microformatDataRenderer")
                    .getObject("thumbnail")
                    .getArray("thumbnails")
            if (!Utils.isNullOrEmpty(microFormatThumbnailsArray)) {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(microFormatThumbnailsArray)
            }
            throw ParsingException("Could not get playlist thumbnails")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            try {
                return YoutubeParsingHelper.getUrlFromNavigationEndpoint(if (isNewPlaylistInterface) playlistHeader!!.getObject("ownerText")
                        .getArray("runs")
                        .getObject(0)
                        .getObject("navigationEndpoint") else uploaderInfo!!.getObject("navigationEndpoint"))
            } catch (e: Exception) {
                throw ParsingException("Could not get playlist uploader url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(if (isNewPlaylistInterface) playlistHeader!!.getObject("ownerText") else uploaderInfo!!.getObject("title"))
            } catch (e: Exception) {
                throw ParsingException("Could not get playlist uploader name", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderAvatars: List<Image?>?
        get() {
            if (isNewPlaylistInterface) {
                // The new playlist interface doesn't provide an uploader avatar
                return listOf<Image>()
            }
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(uploaderInfo!!.getObject("thumbnail")
                        .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get playlist uploader avatars", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            // YouTube doesn't provide this information
            return false
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            if (isNewPlaylistInterface) {
                val numVideosText: String? = YoutubeParsingHelper.getTextFromObject(playlistHeader!!.getObject("numVideosText"))
                if (numVideosText != null) {
                    try {
                        return Utils.removeNonDigitCharacters(numVideosText).toLong()
                    } catch (ignored: NumberFormatException) {
                    }
                }
                val firstByLineRendererText: String? = YoutubeParsingHelper.getTextFromObject(
                        playlistHeader!!.getArray("byline")
                                .getObject(0)
                                .getObject("text"))
                if (firstByLineRendererText != null) {
                    try {
                        return Utils.removeNonDigitCharacters(firstByLineRendererText).toLong()
                    } catch (ignored: NumberFormatException) {
                    }
                }
            }

            // These data structures are returned in both layouts
            val briefStats: JsonArray = (if (isNewPlaylistInterface) playlistHeader else getPlaylistInfo())
                    .getArray("briefStats")
            if (!briefStats.isEmpty()) {
                val briefsStatsText: String? = YoutubeParsingHelper.getTextFromObject(briefStats.getObject(0))
                if (briefsStatsText != null) {
                    return Utils.removeNonDigitCharacters(briefsStatsText).toLong()
                }
            }
            val stats: JsonArray = (if (isNewPlaylistInterface) playlistHeader else getPlaylistInfo())
                    .getArray("stats")
            if (!stats.isEmpty()) {
                val statsText: String? = YoutubeParsingHelper.getTextFromObject(stats.getObject(0))
                if (statsText != null) {
                    return Utils.removeNonDigitCharacters(statsText).toLong()
                }
            }
            return ListExtractor.Companion.ITEM_COUNT_UNKNOWN
        }

    @get:Throws(ParsingException::class)
    override val description: Description
        get() {
            val description: String? = YoutubeParsingHelper.getTextFromObject(
                    getPlaylistInfo()!!.getObject("description"),
                    true
            )
            return Description(description, Description.Companion.HTML)
        }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            var nextPage: Page? = null
            val contents: JsonArray = browseResponse!!.getObject("contents")
                    .getObject("twoColumnBrowseResultsRenderer")
                    .getArray("tabs")
                    .getObject(0)
                    .getObject("tabRenderer")
                    .getObject("content")
                    .getObject("sectionListRenderer")
                    .getArray("contents")
            val videoPlaylistObject: JsonObject? = contents.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ content: JsonObject ->
                        content.getObject("itemSectionRenderer")
                                .getArray("contents")
                                .getObject(0)
                    }))
                    .filter(Predicate({ content: JsonObject? ->
                        (content!!.has(PLAYLIST_VIDEO_LIST_RENDERER)
                                || content.has(RICH_GRID_RENDERER))
                    }))
                    .findFirst()
                    .orElse(null)
            if (videoPlaylistObject != null) {
                val renderer: JsonObject
                if (videoPlaylistObject.has(PLAYLIST_VIDEO_LIST_RENDERER)) {
                    renderer = videoPlaylistObject.getObject(PLAYLIST_VIDEO_LIST_RENDERER)
                } else if (videoPlaylistObject.has(RICH_GRID_RENDERER)) {
                    renderer = videoPlaylistObject.getObject(RICH_GRID_RENDERER)
                } else {
                    return InfoItemsPage(collector, null)
                }
                val videosArray: JsonArray = renderer.getArray("contents")
                collectStreamsFrom(collector, videosArray)
                nextPage = getNextPageFrom(videosArray)
            }
            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
        val ajaxJson: JsonObject? = YoutubeParsingHelper.getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization())
        val continuation: JsonArray = ajaxJson!!.getArray("onResponseReceivedActions")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems")
        collectStreamsFrom(collector, continuation)
        return InfoItemsPage(collector, getNextPageFrom(continuation))
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(contents: JsonArray): Page? {
        if (Utils.isNullOrEmpty(contents)) {
            return null
        }
        val lastElement: JsonObject = contents.getObject(contents.size - 1)
        if (lastElement.has("continuationItemRenderer")) {
            val continuation: String = lastElement
                    .getObject("continuationItemRenderer")
                    .getObject("continuationEndpoint")
                    .getObject("continuationCommand")
                    .getString("token")
            val body: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(
                    getExtractorLocalization(), getExtractorContentCountry())
                    .value("continuation", continuation)
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)
            return Page((YoutubeParsingHelper.YOUTUBEI_V1_URL + "browse?key=" + YoutubeParsingHelper.getKey()
                    + YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER), body)
        } else {
            return null
        }
    }

    private fun collectStreamsFrom(collector: StreamInfoItemsCollector,
                                   videos: JsonArray) {
        val timeAgoParser: TimeAgoParser? = getTimeAgoParser()
        videos.stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .forEach(Consumer({ video: JsonObject ->
                    if (video.has(PLAYLIST_VIDEO_RENDERER)) {
                        collector.commit(YoutubeStreamInfoItemExtractor(
                                video.getObject(PLAYLIST_VIDEO_RENDERER), timeAgoParser))
                    } else if (video.has(RICH_ITEM_RENDERER)) {
                        val richItemRenderer: JsonObject = video.getObject(RICH_ITEM_RENDERER)
                        if (richItemRenderer.has("content")) {
                            val richItemRendererContent: JsonObject = richItemRenderer.getObject("content")
                            if (richItemRendererContent.has(REEL_ITEM_RENDERER)) {
                                collector.commit(YoutubeReelInfoItemExtractor(
                                        richItemRendererContent.getObject(REEL_ITEM_RENDERER)))
                            }
                        }
                    }
                }))
    }

    @get:Throws(ParsingException::class)
    override val playlistType: PlaylistType?
        get() {
            return YoutubeParsingHelper.extractPlaylistTypeFromPlaylistUrl(getUrl())
        }

    companion object {
        // Names of some objects in JSON response frequently used in this class
        private val PLAYLIST_VIDEO_RENDERER: String = "playlistVideoRenderer"
        private val PLAYLIST_VIDEO_LIST_RENDERER: String = "playlistVideoListRenderer"
        private val RICH_GRID_RENDERER: String = "richGridRenderer"
        private val RICH_ITEM_RENDERER: String = "richItemRenderer"
        private val REEL_ITEM_RENDERER: String = "reelItemRenderer"
        private val SIDEBAR: String = "sidebar"
        private val VIDEO_OWNER_RENDERER: String = "videoOwnerRenderer"
    }
}
