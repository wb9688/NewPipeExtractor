/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeTrendingExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream

class YoutubeTrendingExtractor(service: StreamingService,
                               linkHandler: ListLinkHandler?,
                               kioskId: String) : KioskExtractor<StreamInfoItem?>(service, linkHandler, kioskId) {
    private var initialData: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        // @formatter:off
     val body: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(getExtractorLocalization(), 
        getExtractorContentCountry())
        .value("browseId", "FEtrending")
        .value("params", VIDEOS_TAB_PARAMS)
        .done())
        .toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        initialData = YoutubeParsingHelper.getJsonPostResponse("browse", body, getExtractorLocalization())
    }

    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<StreamInfoItem?>()
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            val header: JsonObject = initialData!!.getObject("header")
            var name: String? = null
            if (header.has("feedTabbedHeaderRenderer")) {
                name = YoutubeParsingHelper.getTextAtKey(header.getObject("feedTabbedHeaderRenderer"), "title")
            } else if (header.has("c4TabbedHeaderRenderer")) {
                name = YoutubeParsingHelper.getTextAtKey(header.getObject("c4TabbedHeaderRenderer"), "title")
            } else if (header.has("pageHeaderRenderer")) {
                name = YoutubeParsingHelper.getTextAtKey(header.getObject("pageHeaderRenderer"), "pageTitle")
            }
            if (Utils.isNullOrEmpty(name)) {
                throw ParsingException("Could not get Trending name")
            }
            return name
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            val timeAgoParser: TimeAgoParser? = getTimeAgoParser()
            val tab: JsonObject = trendingTab
            val tabContent: JsonObject = tab.getObject("content")
            val isVideoTab: Boolean = (tab.getObject("endpoint").getObject("browseEndpoint")
                    .getString("params", "") == VIDEOS_TAB_PARAMS)
            if (tabContent.has("richGridRenderer")) {
                tabContent.getObject("richGridRenderer")
                        .getArray("contents")
                        .stream()
                        .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) })) // Filter Trending shorts and Recently trending sections
                        .filter(Predicate({ content: JsonObject -> content.has("richItemRenderer") }))
                        .map(Function({ content: JsonObject ->
                            content.getObject("richItemRenderer")
                                    .getObject("content")
                                    .getObject("videoRenderer")
                        }))
                        .forEachOrdered(Consumer({ videoRenderer: JsonObject ->
                            collector.commit(
                                    YoutubeStreamInfoItemExtractor(videoRenderer, timeAgoParser))
                        }))
            } else if (tabContent.has("sectionListRenderer")) {
                val shelves: Stream<JsonObject> = tabContent.getObject("sectionListRenderer")
                        .getArray("contents")
                        .stream()
                        .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                        .flatMap(Function<JsonObject, Stream<*>>({ content: JsonObject ->
                            content.getObject("itemSectionRenderer")
                                    .getArray("contents")
                                    .stream()
                        }))
                        .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                        .map(Function({ content: JsonObject -> content.getObject("shelfRenderer") }))
                val items: Stream<JsonObject>
                if (isVideoTab) {
                    // The first shelf of the Videos tab contains the normal trends
                    items = shelves.findFirst().stream()
                } else {
                    // Filter Trending shorts and Recently trending sections which have a title,
                    // contrary to normal trends
                    items = shelves.filter(Predicate({ shelfRenderer: JsonObject -> !shelfRenderer.has("title") }))
                }
                items.flatMap(Function<JsonObject, Stream<*>>({ shelfRenderer: JsonObject ->
                    shelfRenderer.getObject("content")
                            .getObject("expandedShelfContentsRenderer")
                            .getArray("items")
                            .stream()
                }))
                        .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                        .map(Function({ item: JsonObject -> item.getObject("videoRenderer") }))
                        .forEachOrdered(Consumer({ videoRenderer: JsonObject ->
                            collector.commit(
                                    YoutubeStreamInfoItemExtractor(videoRenderer, timeAgoParser))
                        }))
            }
            return InfoItemsPage(collector, null)
        }

    @get:Throws(ParsingException::class)
    private val trendingTab: JsonObject
        private get() {
            return initialData!!.getObject("contents")
                    .getObject("twoColumnBrowseResultsRenderer")
                    .getArray("tabs")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ tab: JsonObject -> tab.getObject("tabRenderer") }))
                    .filter(Predicate({ tabRenderer: JsonObject -> tabRenderer.getBoolean("selected") }))
                    .filter(Predicate({ tabRenderer: JsonObject -> tabRenderer.has("content") })) // There should be at most one tab selected
                    .findFirst()
                    .orElseThrow(Supplier({ ParsingException("Could not get \"Now\" or \"Videos\" trending tab") }))
        }

    companion object {
        val KIOSK_ID: String = "Trending"
        private val VIDEOS_TAB_PARAMS: String = "4gIOGgxtb3N0X3BvcHVsYXI%3D"
    }
}
