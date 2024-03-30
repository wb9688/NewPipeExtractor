/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeChannelExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader.HeaderType
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelResponseData
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor.VideosTabExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.Collections
import java.util.Optional
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream

class YoutubeChannelExtractor(service: StreamingService,
                              linkHandler: ListLinkHandler?) : ChannelExtractor(service, linkHandler) {
    private var jsonResponse: JsonObject? = null
    private var channelHeader: Optional<ChannelHeader?>? = null
    private var channelId: String? = null

    /**
     * If a channel is age-restricted, its pages are only accessible to logged-in and
     * age-verified users, we get an `channelAgeGateRenderer` in this case, containing only
     * the following metadata: channel name and channel avatar.
     *
     *
     *
     * This restriction doesn't seem to apply to all countries.
     *
     */
    private var channelAgeGateRenderer: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val channelPath: String? = super.getId()
        val id: String? = YoutubeChannelHelper.resolveChannelId(channelPath)
        // Fetch Videos tab
        val data: ChannelResponseData? = YoutubeChannelHelper.getChannelResponse(id,
                "EgZ2aWRlb3PyBgQKAjoA", getExtractorLocalization(), getExtractorContentCountry())
        jsonResponse = data!!.jsonResponse
        channelHeader = YoutubeChannelHelper.getChannelHeader(jsonResponse)
        channelId = data.channelId
        channelAgeGateRenderer = getChannelAgeGateRenderer()
    }

    private fun getChannelAgeGateRenderer(): JsonObject? {
        return jsonResponse!!.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .flatMap(Function<JsonObject, Stream<out JsonObject>>({ tab: JsonObject ->
                    tab.getObject("tabRenderer")
                            .getObject("content")
                            .getObject("sectionListRenderer")
                            .getArray("contents")
                            .stream()
                            .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                            .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                }))
                .filter(Predicate({ content: JsonObject -> content.has("channelAgeGateRenderer") }))
                .map(Function({ content: JsonObject -> content.getObject("channelAgeGateRenderer") }))
                .findFirst()
                .orElse(null)
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val url: String?
        get() {
            try {
                return YoutubeChannelLinkHandlerFactory.Companion.getInstance().getUrl("channel/" + id)
            } catch (e: ParsingException) {
                return super.getUrl()
            }
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val id: String?
        get() {
            assertPageFetched()
            return channelHeader!!.map(Function({ header: ChannelHeader? -> header!!.json }))
                    .flatMap(Function<JsonObject?, Optional<out String?>>({ header: JsonObject? ->
                        Optional.ofNullable(header!!.getString("channelId"))
                                .or(Supplier<Optional<out String?>>({
                                    Optional.ofNullable(header.getObject("navigationEndpoint")
                                            .getObject("browseEndpoint")
                                            .getString("browseId"))
                                })
                                )
                    }))
                    .or(Supplier<Optional<out String?>>({ Optional.ofNullable(channelId) }))
                    .orElseThrow(Supplier({ ParsingException("Could not get channel ID") }))
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                val title: String = channelAgeGateRenderer!!.getString("channelTitle")
                if (Utils.isNullOrEmpty(title)) {
                    throw ParsingException("Could not get channel name")
                }
                return title
            }
            val metadataRendererTitle: String = jsonResponse!!.getObject("metadata")
                    .getObject("channelMetadataRenderer")
                    .getString("title")
            if (!Utils.isNullOrEmpty(metadataRendererTitle)) {
                return metadataRendererTitle
            }
            return channelHeader!!.map<String>(Function<ChannelHeader?, String>({ header: ChannelHeader? ->
                val channelJson: JsonObject? = header!!.json
                when (header.headerType) {
                    HeaderType.PAGE -> return@map channelJson!!.getObject("content")
                            .getObject("pageHeaderViewModel")
                            .getObject("title")
                            .getObject("dynamicTextViewModel")
                            .getObject("text")
                            .getString("content", channelJson.getString("pageTitle"))

                    HeaderType.CAROUSEL, HeaderType.INTERACTIVE_TABBED -> return@map YoutubeParsingHelper.getTextFromObject(channelJson!!.getObject("title"))
                    HeaderType.C4_TABBED -> return@map channelJson!!.getString("title")
                    else -> return@map channelJson!!.getString("title")
                }
            })) // The channel name from a microformatDataRenderer may be different from the one displayed,
                    // especially for auto-generated channels, depending on the language requested for the
                    // interface (hl parameter of InnerTube requests' payload)
                    .or(Supplier<Optional<out String>>({
                        Optional.ofNullable<String>(jsonResponse!!.getObject("microformat")
                                .getObject("microformatDataRenderer")
                                .getString("title"))
                    }))
                    .orElseThrow<ParsingException>(Supplier<ParsingException>({ ParsingException("Could not get channel name") }))
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val avatars: List<Image?>?
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                return Optional.ofNullable(channelAgeGateRenderer!!.getObject("avatar")
                        .getArray("thumbnails"))
                        .map(Function({ obj: JsonArray? -> YoutubeParsingHelper.getImagesFromThumbnailsArray() }))
                        .orElseThrow(Supplier({ ParsingException("Could not get avatars") }))
            }
            return channelHeader!!.map<JsonArray>(Function<ChannelHeader?, JsonArray>({ header: ChannelHeader? ->
                when (header!!.headerType) {
                    HeaderType.PAGE -> return@map header.json.getObject("content")
                            .getObject("pageHeaderViewModel")
                            .getObject("image")
                            .getObject("contentPreviewImageViewModel")
                            .getObject("image")
                            .getArray("sources")

                    HeaderType.INTERACTIVE_TABBED -> return@map header.json.getObject("boxArt")
                            .getArray("thumbnails")

                    HeaderType.C4_TABBED, HeaderType.CAROUSEL -> return@map header.json.getObject("avatar")
                            .getArray("thumbnails")

                    else -> return@map header.json.getObject("avatar")
                            .getArray("thumbnails")
                }
            }))
                    .map<List<Image?>?>(Function<JsonArray, List<Image?>?>({ obj: JsonArray? -> YoutubeParsingHelper.getImagesFromThumbnailsArray() }))
                    .orElseThrow<ParsingException>(Supplier<ParsingException>({ ParsingException("Could not get avatars") }))
        }

    @get:Nonnull
    override val banners: List<Image?>?
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                return listOf<Image>()
            }

            // No banner is available on pageHeaderRenderer headers
            return channelHeader!!.filter(Predicate({ header: ChannelHeader? -> header!!.headerType != HeaderType.PAGE }))
                    .map(Function({ header: ChannelHeader? ->
                        header!!.json.getObject("banner")
                                .getArray("thumbnails")
                    }))
                    .map(Function({ obj: JsonArray? -> YoutubeParsingHelper.getImagesFromThumbnailsArray() }))
                    .orElse(listOf<Image>())
        }

    @get:Throws(ParsingException::class)
    override val feedUrl: String?
        get() {
            // RSS feeds are accessible for age-restricted channels, no need to check whether a channel
            // has a channelAgeGateRenderer
            try {
                return YoutubeParsingHelper.getFeedUrlFrom(id)
            } catch (e: Exception) {
                throw ParsingException("Could not get feed URL", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val subscriberCount: Long
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                return ChannelExtractor.Companion.UNKNOWN_SUBSCRIBER_COUNT
            }
            if (channelHeader!!.isPresent()) {
                val header: ChannelHeader = channelHeader!!.get()
                if ((header.headerType == HeaderType.INTERACTIVE_TABBED
                                || header.headerType == HeaderType.PAGE)) {
                    // No subscriber count is available on interactiveTabbedHeaderRenderer and
                    // pageHeaderRenderer headers
                    return ChannelExtractor.Companion.UNKNOWN_SUBSCRIBER_COUNT
                }
                val headerJson: JsonObject? = header.json
                var textObject: JsonObject? = null
                if (headerJson!!.has("subscriberCountText")) {
                    textObject = headerJson.getObject("subscriberCountText")
                } else if (headerJson.has("subtitle")) {
                    textObject = headerJson.getObject("subtitle")
                }
                if (textObject != null) {
                    try {
                        return Utils.mixedNumberWordToLong(YoutubeParsingHelper.getTextFromObject(textObject))
                    } catch (e: NumberFormatException) {
                        throw ParsingException("Could not get subscriber count", e)
                    }
                }
            }
            return ChannelExtractor.Companion.UNKNOWN_SUBSCRIBER_COUNT
        }

    @get:Throws(ParsingException::class)
    override val description: String?
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                return null
            }
            try {
                if (channelHeader!!.isPresent()) {
                    val header: ChannelHeader = channelHeader!!.get()
                    if (header.headerType == HeaderType.PAGE) {
                        // A pageHeaderRenderer doesn't contain a description
                        return null
                    }
                    if (header.headerType == HeaderType.INTERACTIVE_TABBED) {
                        /*
                    In an interactiveTabbedHeaderRenderer, the real description, is only available
                    in its header
                    The other one returned in non-About tabs accessible in the
                    microformatDataRenderer object of the response may be completely different
                    The description extracted is incomplete and the original one can be only
                    accessed from the About tab
                     */
                        return YoutubeParsingHelper.getTextFromObject(header.json.getObject("description"))
                    }
                }

                // The description is cut and the original one can be only accessed from the About tab
                return jsonResponse!!.getObject("metadata")
                        .getObject("channelMetadataRenderer")
                        .getString("description")
            } catch (e: Exception) {
                throw ParsingException("Could not get channel description", e)
            }
        }
    override val parentChannelName: String?
        get() {
            return ""
        }
    override val parentChannelUrl: String?
        get() {
            return ""
        }

    @get:Nonnull
    override val parentChannelAvatars: List<Image?>?
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                return false
            }
            if (channelHeader!!.isPresent()) {
                val header: ChannelHeader = channelHeader!!.get()

                // carouselHeaderRenderer and pageHeaderRenderer does not contain any verification
                // badges
                // Since they are only shown on YouTube internal channels or on channels of large
                // organizations broadcasting live events, we can assume the channel to be verified
                if (header.headerType == HeaderType.CAROUSEL || header.headerType == HeaderType.PAGE) {
                    return true
                }
                if (header.headerType == HeaderType.INTERACTIVE_TABBED) {
                    // If the header has an autoGenerated property, it should mean that the channel has
                    // been auto generated by YouTube: we can assume the channel to be verified in this
                    // case
                    return header.json.has("autoGenerated")
                }
                return YoutubeParsingHelper.isVerified(header.json.getArray("badges"))
            }
            return false
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val tabs: List<ListLinkHandler>
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer == null) {
                return tabsForNonAgeRestrictedChannels
            }
            return tabsForAgeRestrictedChannels
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    private val tabsForNonAgeRestrictedChannels: List<ListLinkHandler>
        private get() {
            val responseTabs: JsonArray = jsonResponse!!.getObject("contents")
                    .getObject("twoColumnBrowseResultsRenderer")
                    .getArray("tabs")
            val tabs: MutableList<ListLinkHandler> = ArrayList()
            val addNonVideosTab: Consumer<String?> = Consumer<String?>({ tabName: String? ->
                try {
                    tabs.add(YoutubeChannelTabLinkHandlerFactory.Companion.getInstance().fromQuery(
                            channelId, java.util.List.of<String?>(tabName), ""))
                } catch (ignored: ParsingException) {
                    // Do not add the tab if we couldn't create the LinkHandler
                }
            })
            val name: String? = name
            val url: String? = url
            val id: String? = id
            responseTabs.stream()
                    .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .filter(Predicate<JsonObject>({ tab: JsonObject -> tab.has("tabRenderer") }))
                    .map<JsonObject>(Function<JsonObject, JsonObject>({ tab: JsonObject -> tab.getObject("tabRenderer") }))
                    .forEach(Consumer<JsonObject>({ tabRenderer: JsonObject ->
                        val tabUrl: String? = tabRenderer.getObject("endpoint")
                                .getObject("commandMetadata")
                                .getObject("webCommandMetadata")
                                .getString("url")
                        if (tabUrl != null) {
                            val urlParts: Array<String> = tabUrl.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                            if (urlParts.size == 0) {
                                return@forEach
                            }
                            val urlSuffix: String = urlParts.get(urlParts.size - 1)
                            when (urlSuffix) {
                                "videos" ->                                 // Since the Videos tab has already its contents fetched, make
                                    // sure it is in the first position
                                    // YoutubeChannelTabExtractor still supports fetching this tab
                                    tabs.add(0, ReadyChannelTabListLinkHandler(
                                            tabUrl,
                                            channelId,
                                            ChannelTabs.VIDEOS,
                                            ChannelTabExtractorBuilder({ service: StreamingService, linkHandler: ListLinkHandler? ->
                                                VideosTabExtractor(
                                                        service, linkHandler, tabRenderer, name, id, url)
                                            })))

                                "shorts" -> addNonVideosTab.accept(ChannelTabs.SHORTS)
                                "streams" -> addNonVideosTab.accept(ChannelTabs.LIVESTREAMS)
                                "releases" -> addNonVideosTab.accept(ChannelTabs.ALBUMS)
                                "playlists" -> addNonVideosTab.accept(ChannelTabs.PLAYLISTS)
                            }
                        }
                    }))
            return Collections.unmodifiableList(tabs)
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    private val tabsForAgeRestrictedChannels: List<ListLinkHandler>
        private get() {
            // As we don't have access to the channel tabs list, consider that the channel has videos,
            // shorts and livestreams, the data only accessible without login on YouTube's desktop
            // client using uploads system playlists
            // The playlists channel tab is still available on YouTube Music, but this is not
            // implemented in the extractor
            val tabs: MutableList<ListLinkHandler> = ArrayList()
            val channelUrl: String? = url
            val addTab: Consumer<String?> = Consumer({ tabName: String? ->
                tabs.add(ReadyChannelTabListLinkHandler(channelUrl + "/" + tabName,
                        channelId, tabName, ChannelTabExtractorBuilder({ service: StreamingService, linkHandler: ListLinkHandler -> YoutubeChannelTabPlaylistExtractor(service, linkHandler) })))
            })
            addTab.accept(ChannelTabs.VIDEOS)
            addTab.accept(ChannelTabs.SHORTS)
            addTab.accept(ChannelTabs.LIVESTREAMS)
            return Collections.unmodifiableList(tabs)
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val tags: List<String>
        get() {
            assertPageFetched()
            if (channelAgeGateRenderer != null) {
                return listOf()
            }
            return jsonResponse!!.getObject("microformat")
                    .getObject("microformatDataRenderer")
                    .getArray("tags")
                    .stream()
                    .filter(Predicate({ o: Any? -> String::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> String::class.java.cast(obj) }))
                    .collect(Collectors.toUnmodifiableList())
        }
}
