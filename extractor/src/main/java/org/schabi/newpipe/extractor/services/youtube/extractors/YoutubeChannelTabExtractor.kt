package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelResponseData
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * A [ChannelTabExtractor] implementation for the YouTube service.
 *
 *
 *
 * It currently supports `Videos`, `Shorts`, `Live`, `Playlists` and
 * `Channels` tabs.
 *
 */
open class YoutubeChannelTabExtractor(service: StreamingService,
                                      linkHandler: ListLinkHandler?) : ChannelTabExtractor(service, linkHandler) {
    /**
     * Whether the visitor data extracted from the initial channel response is required to be used
     * for continuations.
     *
     *
     *
     * A valid `visitorData` is required to get continuations of shorts in channels.
     *
     *
     *
     *
     * It should be not used when it is not needed, in order to reduce YouTube's tracking.
     *
     */
    private val useVisitorData: Boolean
    private var jsonResponse: JsonObject? = null
    private var channelId: String? = null
    private var visitorData: String? = null

    init {
        useVisitorData = (getName() == ChannelTabs.SHORTS)
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    private val channelTabsParameters: String
        private get() {
            val name: String? = getName()
            when (name) {
                ChannelTabs.VIDEOS -> return "EgZ2aWRlb3PyBgQKAjoA"
                ChannelTabs.SHORTS -> return "EgZzaG9ydHPyBgUKA5oBAA%3D%3D"
                ChannelTabs.LIVESTREAMS -> return "EgdzdHJlYW1z8gYECgJ6AA%3D%3D"
                ChannelTabs.ALBUMS -> return "EghyZWxlYXNlc_IGBQoDsgEA"
                ChannelTabs.PLAYLISTS -> return "EglwbGF5bGlzdHPyBgQKAkIA"
                else -> throw ParsingException("Unsupported channel tab: " + name)
            }
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        channelId = YoutubeChannelHelper.resolveChannelId(super.getId())
        val params: String = channelTabsParameters
        val data: ChannelResponseData? = YoutubeChannelHelper.getChannelResponse(channelId,
                params, getExtractorLocalization(), getExtractorContentCountry())
        jsonResponse = data!!.jsonResponse
        channelId = data.channelId
        if (useVisitorData) {
            visitorData = jsonResponse!!.getObject("responseContext").getString("visitorData")
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val url: String?
        get() {
            try {
                return YoutubeChannelTabLinkHandlerFactory.Companion.getInstance()
                        .getUrl("channel/" + id, java.util.List.of<String?>(getName()), "")
            } catch (e: ParsingException) {
                return super.getUrl()
            }
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val id: String?
        get() {
            val id: String = jsonResponse!!.getObject("header")
                    .getObject("c4TabbedHeaderRenderer")
                    .getString("channelId", "")
            if (!id.isEmpty()) {
                return id
            }
            val carouselHeaderId: Optional<String?> = jsonResponse!!.getObject("header")
                    .getObject("carouselHeaderRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .filter(Predicate({ item: JsonObject -> item.has("topicChannelDetailsRenderer") }))
                    .findFirst()
                    .flatMap(Function<JsonObject, Optional<out String?>>({ item: JsonObject ->
                        Optional.ofNullable(item.getObject("topicChannelDetailsRenderer")
                                .getObject("navigationEndpoint")
                                .getObject("browseEndpoint")
                                .getString("browseId"))
                    }))
            if (carouselHeaderId.isPresent()) {
                return carouselHeaderId.get()
            }
            if (!Utils.isNullOrEmpty(channelId)) {
                return channelId
            } else {
                throw ParsingException("Could not get channel ID")
            }
        }
    protected open val channelName: String?
        protected get() {
            val metadataName: String = jsonResponse!!.getObject("metadata")
                    .getObject("channelMetadataRenderer")
                    .getString("title")
            if (!Utils.isNullOrEmpty(metadataName)) {
                return metadataName
            }
            return YoutubeChannelHelper.getChannelHeader(jsonResponse)
                    .map<String>(Function<ChannelHeader?, String>({ header: ChannelHeader? ->
                        val title: Any? = header!!.json.get("title")
                        if (title is String) {
                            return@map title
                        } else if (title is JsonObject) {
                            val headerName: String? = YoutubeParsingHelper.getTextFromObject(title as JsonObject?)
                            if (!Utils.isNullOrEmpty(headerName)) {
                                return@map headerName
                            }
                        }
                        ""
                    }))
                    .orElse("")
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            var items: JsonArray = JsonArray()
            val tab: Optional<JsonObject> = tabData
            if (tab.isPresent()) {
                val tabContent: JsonObject = tab.get().getObject("content")
                items = tabContent.getObject("sectionListRenderer")
                        .getArray("contents")
                        .getObject(0)
                        .getObject("itemSectionRenderer")
                        .getArray("contents")
                        .getObject(0)
                        .getObject("gridRenderer")
                        .getArray("items")
                if (items.isEmpty()) {
                    items = tabContent.getObject("richGridRenderer")
                            .getArray("contents")
                    if (items.isEmpty()) {
                        items = tabContent.getObject("sectionListRenderer")
                                .getArray("contents")
                    }
                }
            }

            // If a channel tab is fetched, the next page requires channel ID and name, as channel
            // streams don't have their channel specified.
            // We also need to set the visitor data here when it should be enabled, as it is required
            // to get continuations on some channel tabs, and we need a way to pass it between pages
            val channelIds: List<String?> = if (useVisitorData && !Utils.isNullOrEmpty(visitorData)) java.util.List.of(channelName, url, visitorData) else java.util.List.of(channelName, url)
            val continuation: JsonObject? = collectItemsFrom(collector, items, channelIds)
                    .orElse(null)
            val nextPage: Page? = getNextPageFrom(continuation, channelIds)
            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val channelIds: List<String?>? = page.getIds()
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
        val ajaxJson: JsonObject? = YoutubeParsingHelper.getJsonPostResponse("browse", page.getBody(),
                getExtractorLocalization())
        val sectionListContinuation: JsonObject = ajaxJson!!.getArray("onResponseReceivedActions")
                .stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .filter(Predicate({ jsonObject: JsonObject -> jsonObject.has("appendContinuationItemsAction") }))
                .map(Function({ jsonObject: JsonObject -> jsonObject.getObject("appendContinuationItemsAction") }))
                .findFirst()
                .orElse(JsonObject())
        val continuation: JsonObject? = collectItemsFrom(collector,
                sectionListContinuation.getArray("continuationItems"), channelIds)
                .orElse(null)
        return InfoItemsPage(collector, getNextPageFrom(continuation, channelIds))
    }

    open val tabData: Optional<JsonObject>
        get() {
            val urlSuffix: String = YoutubeChannelTabLinkHandlerFactory.Companion.getUrlSuffix(getName())
            return jsonResponse!!.getObject("contents")
                    .getObject("twoColumnBrowseResultsRenderer")
                    .getArray("tabs")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .filter(Predicate({ tab: JsonObject -> tab.has("tabRenderer") }))
                    .map(Function({ tab: JsonObject -> tab.getObject("tabRenderer") }))
                    .filter(Predicate({ tabRenderer: JsonObject ->
                        tabRenderer.getObject("endpoint")
                                .getObject("commandMetadata").getObject("webCommandMetadata")
                                .getString("url", "").endsWith(urlSuffix)
                    }))
                    .findFirst() // Check if tab has no content
                    .filter(Predicate({ tabRenderer: JsonObject ->
                        val tabContents: JsonArray = tabRenderer.getObject("content")
                                .getObject("sectionListRenderer")
                                .getArray("contents")
                                .getObject(0)
                                .getObject("itemSectionRenderer")
                                .getArray("contents")
                        (tabContents.size != 1
                                || !tabContents.getObject(0).has("messageRenderer"))
                    }))
        }

    private fun collectItemsFrom(@Nonnull collector: MultiInfoItemsCollector,
                                 @Nonnull items: JsonArray,
                                 @Nonnull channelIds: List<String?>?): Optional<JsonObject?> {
        return items.stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map(Function({ item: JsonObject -> collectItem(collector, item, channelIds) }))
                .reduce(Optional.empty(), BinaryOperator({ c1: Optional<JsonObject?>, c2: Optional<JsonObject?>? -> c1.or(Supplier<Optional<out JsonObject?>?>({ c2 })) }))
    }

    private fun collectItem(@Nonnull collector: MultiInfoItemsCollector,
                            @Nonnull item: JsonObject,
                            @Nonnull channelIds: List<String?>?): Optional<JsonObject?> {
        val timeAgoParser: TimeAgoParser? = getTimeAgoParser()
        if (item.has("richItemRenderer")) {
            val richItem: JsonObject = item.getObject("richItemRenderer")
                    .getObject("content")
            if (richItem.has("videoRenderer")) {
                getCommitVideoConsumer(collector, timeAgoParser, channelIds,
                        richItem.getObject("videoRenderer"))
            } else if (richItem.has("reelItemRenderer")) {
                getCommitReelItemConsumer(collector, channelIds,
                        richItem.getObject("reelItemRenderer"))
            } else if (richItem.has("playlistRenderer")) {
                getCommitPlaylistConsumer(collector, channelIds,
                        richItem.getObject("playlistRenderer"))
            }
        } else if (item.has("gridVideoRenderer")) {
            getCommitVideoConsumer(collector, timeAgoParser, channelIds,
                    item.getObject("gridVideoRenderer"))
        } else if (item.has("gridPlaylistRenderer")) {
            getCommitPlaylistConsumer(collector, channelIds,
                    item.getObject("gridPlaylistRenderer"))
        } else if (item.has("shelfRenderer")) {
            return collectItem(collector, item.getObject("shelfRenderer")
                    .getObject("content"), channelIds)
        } else if (item.has("itemSectionRenderer")) {
            return collectItemsFrom(collector, item.getObject("itemSectionRenderer")
                    .getArray("contents"), channelIds)
        } else if (item.has("horizontalListRenderer")) {
            return collectItemsFrom(collector, item.getObject("horizontalListRenderer")
                    .getArray("items"), channelIds)
        } else if (item.has("expandedShelfContentsRenderer")) {
            return collectItemsFrom(collector, item.getObject("expandedShelfContentsRenderer")
                    .getArray("items"), channelIds)
        } else if (item.has("continuationItemRenderer")) {
            return Optional.ofNullable(item.getObject("continuationItemRenderer"))
        }
        return Optional.empty()
    }

    private fun getCommitVideoConsumer(@Nonnull collector: MultiInfoItemsCollector,
                                       @Nonnull timeAgoParser: TimeAgoParser?,
                                       @Nonnull channelIds: List<String?>?,
                                       @Nonnull jsonObject: JsonObject) {
        collector.commit(
                object : YoutubeStreamInfoItemExtractor(jsonObject, timeAgoParser) {
                    @get:Throws(ParsingException::class)
                    override val uploaderName: String?
                        get() {
                            if (channelIds!!.size >= 2) {
                                return channelIds.get(0)
                            }
                            return super.getUploaderName()
                        }

                    @get:Throws(ParsingException::class)
                    override val uploaderUrl: String?
                        get() {
                            if (channelIds!!.size >= 2) {
                                return channelIds.get(1)
                            }
                            return super.getUploaderUrl()
                        }
                })
    }

    private fun getCommitReelItemConsumer(@Nonnull collector: MultiInfoItemsCollector,
                                          @Nonnull channelIds: List<String?>?,
                                          @Nonnull jsonObject: JsonObject) {
        collector.commit(
                object : YoutubeReelInfoItemExtractor(jsonObject) {
                    @get:Throws(ParsingException::class)
                    override val uploaderName: String?
                        get() {
                            if (channelIds!!.size >= 2) {
                                return channelIds.get(0)
                            }
                            return super.getUploaderName()
                        }

                    @get:Throws(ParsingException::class)
                    override val uploaderUrl: String?
                        get() {
                            if (channelIds!!.size >= 2) {
                                return channelIds.get(1)
                            }
                            return super.getUploaderUrl()
                        }
                })
    }

    private fun getCommitPlaylistConsumer(@Nonnull collector: MultiInfoItemsCollector,
                                          @Nonnull channelIds: List<String?>?,
                                          @Nonnull jsonObject: JsonObject) {
        collector.commit(
                object : YoutubePlaylistInfoItemExtractor(jsonObject) {
                    @get:Throws(ParsingException::class)
                    override val uploaderName: String?
                        get() {
                            if (channelIds!!.size >= 2) {
                                return channelIds.get(0)
                            }
                            return super.getUploaderName()
                        }

                    @get:Throws(ParsingException::class)
                    override val uploaderUrl: String?
                        get() {
                            if (channelIds!!.size >= 2) {
                                return channelIds.get(1)
                            }
                            return super.getUploaderUrl()
                        }
                })
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(continuations: JsonObject?,
                                channelIds: List<String?>?): Page? {
        if (Utils.isNullOrEmpty(continuations)) {
            return null
        }
        val continuationEndpoint: JsonObject = continuations!!.getObject("continuationEndpoint")
        val continuation: String = continuationEndpoint.getObject("continuationCommand")
                .getString("token")
        val body: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(getExtractorLocalization(),
                getExtractorContentCountry(),
                if (useVisitorData && channelIds!!.size >= 3) channelIds.get(2) else null)
                .value("continuation", continuation)
                .done())
                .toByteArray(StandardCharsets.UTF_8)
        return Page((YoutubeParsingHelper.YOUTUBEI_V1_URL + "browse?key=" + YoutubeParsingHelper.getKey()
                + YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER), null, channelIds, null, body)
    }

    /**
     * A [YoutubeChannelTabExtractor] for the `Videos` tab, if it has been already
     * fetched.
     */
    class VideosTabExtractor internal constructor(service: StreamingService,
                                                  linkHandler: ListLinkHandler?,
                                                  private val tabRenderer: JsonObject,
                                                  override val channelName: String?,
                                                  private val channelId: String?,
                                                  private val channelUrl: String?) : YoutubeChannelTabExtractor(service, linkHandler) {
        public override fun onFetchPage(@Nonnull downloader: Downloader?) {
            // Nothing to do, the initial data was already fetched and is stored in the link handler
        }

        @Nonnull
        @Throws(ParsingException::class)
        public override fun getId(): String? {
            return channelId
        }

        @Nonnull
        @Throws(ParsingException::class)
        public override fun getUrl(): String? {
            return channelUrl
        }

        public override fun getTabData(): Optional<JsonObject> {
            return Optional.of(tabRenderer)
        }
    }
}
