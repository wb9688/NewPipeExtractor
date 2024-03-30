package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class YoutubeMusicSearchExtractor(service: StreamingService,
                                  linkHandler: SearchQueryHandler?) : SearchExtractor(service, linkHandler) {
    private var initialData: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val youtubeMusicKeys: Array<String?>? = YoutubeParsingHelper.getYoutubeMusicKey()
        val url: String = ("https://music.youtube.com/youtubei/v1/search?key="
                + youtubeMusicKeys!!.get(0) + YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER)
        val params: String?
        when (getLinkHandler().getContentFilters().get(0)) {
            YoutubeSearchQueryHandlerFactory.Companion.MUSIC_SONGS -> params = "Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.Companion.MUSIC_VIDEOS -> params = "Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.Companion.MUSIC_ALBUMS -> params = "Eg-KAQwIABAAGAEgACgAMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.Companion.MUSIC_PLAYLISTS -> params = "Eg-KAQwIABAAGAAgACgBMABqChAEEAUQAxAKEAk%3D"
            YoutubeSearchQueryHandlerFactory.Companion.MUSIC_ARTISTS -> params = "Eg-KAQwIABAAGAAgASgAMABqChAEEAUQAxAKEAk%3D"
            else -> params = null
        }

        // @formatter:off
         val json: ByteArray = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("clientName", "WEB_REMIX")
        .value("clientVersion", youtubeMusicKeys.get(2))
        .value("hl", "en-GB")
        .value("gl", getExtractorContentCountry().getCountryCode())
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .value("query", getSearchString())
        .value("params", params)
        .end().done().toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val responseBody: String? = YoutubeParsingHelper.getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(url, YoutubeParsingHelper.getYoutubeMusicHeaders(), json))
        try {
            initialData = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON", e)
        }
    }

    private val itemSectionRendererContents: List<JsonObject>
        private get() {
            return initialData
                    .getObject("contents")
                    .getObject("tabbedSearchResultsRenderer")
                    .getArray("tabs")
                    .getObject(0)
                    .getObject("tabRenderer")
                    .getObject("content")
                    .getObject("sectionListRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ c: JsonObject -> c.getObject("itemSectionRenderer") }))
                    .filter(Predicate({ isr: JsonObject -> !isr.isEmpty() }))
                    .map(Function({ isr: JsonObject ->
                        isr
                                .getArray("contents")
                                .getObject(0)
                    }))
                    .collect(Collectors.toList())
        }

    @get:Throws(ParsingException::class)
    override val searchSuggestion: String?
        get() {
            for (obj: JsonObject in itemSectionRendererContents) {
                val didYouMeanRenderer: JsonObject = obj
                        .getObject("didYouMeanRenderer")
                val showingResultsForRenderer: JsonObject = obj
                        .getObject("showingResultsForRenderer")
                if (!didYouMeanRenderer.isEmpty()) {
                    return YoutubeParsingHelper.getTextFromObject(didYouMeanRenderer.getObject("correctedQuery"))
                } else if (!showingResultsForRenderer.isEmpty()) {
                    return JsonUtils.getString(showingResultsForRenderer,
                            "correctedQueryEndpoint.searchEndpoint.query")
                }
            }
            return ""
        }

    @get:Throws(ParsingException::class)
    override val isCorrectedSearch: Boolean
        get() {
            return itemSectionRendererContents
                    .stream()
                    .anyMatch(Predicate({ obj: JsonObject -> obj.has("showingResultsForRenderer") }))
        }

    override val metaInfo: List<MetaInfo?>?
        get() {
            return emptyList<MetaInfo>()
        }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            val contents: JsonArray? = JsonUtils.getArray(JsonUtils.getArray(initialData,
                    "contents.tabbedSearchResultsRenderer.tabs").getObject(0),
                    "tabRenderer.content.sectionListRenderer.contents")
            var nextPage: Page? = null
            for (content: Any in contents!!) {
                if ((content as JsonObject).has("musicShelfRenderer")) {
                    val musicShelfRenderer: JsonObject = content
                            .getObject("musicShelfRenderer")
                    collectMusicStreamsFrom(collector, musicShelfRenderer.getArray("contents"))
                    nextPage = getNextPageFrom(musicShelfRenderer.getArray("continuations"))
                }
            }
            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
        val youtubeMusicKeys: Array<String?>? = YoutubeParsingHelper.getYoutubeMusicKey()

        // @formatter:off
         val json: ByteArray = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("clientName", "WEB_REMIX")
        .value("clientVersion", youtubeMusicKeys!!.get(2))
        .value("hl", "en-GB")
        .value("gl", getExtractorContentCountry().getCountryCode())
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .end().done().toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val responseBody: String? = YoutubeParsingHelper.getValidJsonResponseBody(
                getDownloader().postWithContentTypeJson(
                        page.getUrl(), YoutubeParsingHelper.getYoutubeMusicHeaders(), json))
        val ajaxJson: JsonObject
        try {
            ajaxJson = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON", e)
        }
        val musicShelfContinuation: JsonObject = ajaxJson.getObject("continuationContents")
                .getObject("musicShelfContinuation")
        collectMusicStreamsFrom(collector, musicShelfContinuation.getArray("contents"))
        val continuations: JsonArray = musicShelfContinuation.getArray("continuations")
        return InfoItemsPage(collector, getNextPageFrom(continuations))
    }

    private fun collectMusicStreamsFrom(collector: MultiInfoItemsCollector,
                                        videos: JsonArray) {
        val searchType: String = getLinkHandler().getContentFilters().get(0)
        videos.stream()
                .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map<JsonObject>(Function<JsonObject, JsonObject>({ item: JsonObject -> item.getObject("musicResponsiveListItemRenderer", null) }))
                .filter(Predicate<JsonObject>({ obj: JsonObject? -> Objects.nonNull(obj) }))
                .forEachOrdered(Consumer<JsonObject>({ infoItem: JsonObject ->
                    val displayPolicy: String = infoItem.getString(
                            "musicItemRendererDisplayPolicy", "")
                    if ((displayPolicy == "MUSIC_ITEM_RENDERER_DISPLAY_POLICY_GREY_OUT")) {
                        // No info about URL available
                        return@forEachOrdered
                    }
                    val descriptionElements: JsonArray = infoItem.getArray("flexColumns")
                            .getObject(1)
                            .getObject("musicResponsiveListItemFlexColumnRenderer")
                            .getObject("text")
                            .getArray("runs")
                    when (searchType) {
                        YoutubeSearchQueryHandlerFactory.Companion.MUSIC_SONGS, YoutubeSearchQueryHandlerFactory.Companion.MUSIC_VIDEOS -> collector.commit(YoutubeMusicSongOrVideoInfoItemExtractor(
                                infoItem, descriptionElements, searchType))

                        YoutubeSearchQueryHandlerFactory.Companion.MUSIC_ARTISTS -> collector.commit(YoutubeMusicArtistInfoItemExtractor(infoItem))
                        YoutubeSearchQueryHandlerFactory.Companion.MUSIC_ALBUMS, YoutubeSearchQueryHandlerFactory.Companion.MUSIC_PLAYLISTS -> collector.commit(YoutubeMusicAlbumOrPlaylistInfoItemExtractor(
                                infoItem, descriptionElements, searchType))
                    }
                }))
    }

    @Throws(IOException::class, ParsingException::class, ReCaptchaException::class)
    private fun getNextPageFrom(continuations: JsonArray): Page? {
        if (Utils.isNullOrEmpty(continuations)) {
            return null
        }
        val nextContinuationData: JsonObject = continuations.getObject(0)
                .getObject("nextContinuationData")
        val continuation: String = nextContinuationData.getString("continuation")
        return Page(("https://music.youtube.com/youtubei/v1/search?ctoken=" + continuation
                + "&continuation=" + continuation + "&key="
                + YoutubeParsingHelper.getYoutubeMusicKey()!!.get(0) + YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER))
    }
}
