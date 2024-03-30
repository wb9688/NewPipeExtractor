package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonBuilder
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeMetaInfoHelper
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Objects

/*
* Created by Christian Schabesberger on 22.07.2018
*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* YoutubeSearchExtractor.java is part of NewPipe Extractor.
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
* along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
*/
class YoutubeSearchExtractor(service: StreamingService,
                             linkHandler: SearchQueryHandler?) : SearchExtractor(service, linkHandler) {
    private val searchType: String?
    private val extractVideoResults: Boolean
    private val extractChannelResults: Boolean
    private val extractPlaylistResults: Boolean
    private var initialData: JsonObject? = null

    init {
        val contentFilters: List<String?>? = linkHandler.getContentFilters()
        searchType = if (Utils.isNullOrEmpty(contentFilters)) null else contentFilters!!.get(0)
        // Save whether we should extract video, channel and playlist results depending on the
        // requested search type, as YouTube returns sometimes videos inside channel search results
        // If no search type is provided or ALL filter is requested, extract everything
        extractVideoResults = ((searchType == null) || (YoutubeSearchQueryHandlerFactory.Companion.ALL == searchType) || (YoutubeSearchQueryHandlerFactory.Companion.VIDEOS == searchType))
        extractChannelResults = ((searchType == null) || (YoutubeSearchQueryHandlerFactory.Companion.ALL == searchType) || (YoutubeSearchQueryHandlerFactory.Companion.CHANNELS == searchType))
        extractPlaylistResults = ((searchType == null) || (YoutubeSearchQueryHandlerFactory.Companion.ALL == searchType) || (YoutubeSearchQueryHandlerFactory.Companion.PLAYLISTS == searchType))
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val query: String? = super.getSearchString()
        val localization: Localization? = getExtractorLocalization()
        val params: String = YoutubeSearchQueryHandlerFactory.Companion.getSearchParameter(searchType)
        val jsonBody: JsonBuilder<JsonObject?> = YoutubeParsingHelper.prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("query", query)
        if (!Utils.isNullOrEmpty(params)) {
            jsonBody.value("params", params)
        }
        val body: ByteArray = JsonWriter.string(jsonBody.done()).toByteArray(StandardCharsets.UTF_8)
        initialData = YoutubeParsingHelper.getJsonPostResponse("search", body, localization)
    }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return super.getUrl() + "&gl=" + getExtractorContentCountry().getCountryCode()
        }

    @get:Throws(ParsingException::class)
    override val searchSuggestion: String?
        get() {
            val itemSectionRenderer: JsonObject = initialData!!.getObject("contents")
                    .getObject("twoColumnSearchResultsRenderer")
                    .getObject("primaryContents")
                    .getObject("sectionListRenderer")
                    .getArray("contents")
                    .getObject(0)
                    .getObject("itemSectionRenderer")
            val didYouMeanRenderer: JsonObject = itemSectionRenderer.getArray("contents")
                    .getObject(0)
                    .getObject("didYouMeanRenderer")
            if (!didYouMeanRenderer.isEmpty()) {
                return JsonUtils.getString(didYouMeanRenderer,
                        "correctedQueryEndpoint.searchEndpoint.query")
            }
            return Objects.requireNonNullElse(
                    YoutubeParsingHelper.getTextFromObject(itemSectionRenderer.getArray("contents")
                            .getObject(0)
                            .getObject("showingResultsForRenderer")
                            .getObject("correctedQuery")), "")
        }
    override val isCorrectedSearch: Boolean
        get() {
            val showingResultsForRenderer: JsonObject = initialData!!.getObject("contents")
                    .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                    .getObject("sectionListRenderer").getArray("contents").getObject(0)
                    .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                    .getObject("showingResultsForRenderer")
            return !showingResultsForRenderer.isEmpty()
        }

    @get:Throws(ParsingException::class)
    override val metaInfo: List<MetaInfo?>?
        get() {
            return YoutubeMetaInfoHelper.getMetaInfo(
                    initialData!!.getObject("contents")
                            .getObject("twoColumnSearchResultsRenderer")
                            .getObject("primaryContents")
                            .getObject("sectionListRenderer")
                            .getArray("contents"))
        }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            val sections: JsonArray = initialData!!.getObject("contents")
                    .getObject("twoColumnSearchResultsRenderer")
                    .getObject("primaryContents")
                    .getObject("sectionListRenderer")
                    .getArray("contents")
            var nextPage: Page? = null
            for (section: Any in sections) {
                val sectionJsonObject: JsonObject = section as JsonObject
                if (sectionJsonObject.has("itemSectionRenderer")) {
                    val itemSectionRenderer: JsonObject = sectionJsonObject.getObject("itemSectionRenderer")
                    collectStreamsFrom(collector, itemSectionRenderer.getArray("contents"))
                } else if (sectionJsonObject.has("continuationItemRenderer")) {
                    nextPage = getNextPageFrom(
                            sectionJsonObject.getObject("continuationItemRenderer"))
                }
            }
            return InfoItemsPage(collector, nextPage)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val localization: Localization? = getExtractorLocalization()
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())

        // @formatter:off
         val json: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(localization, 
        getExtractorContentCountry())
        .value("continuation", page.getId())
        .done())
        .toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val ajaxJson: JsonObject? = YoutubeParsingHelper.getJsonPostResponse("search", json, localization)
        val continuationItems: JsonArray = ajaxJson!!.getArray("onResponseReceivedCommands")
                .getObject(0)
                .getObject("appendContinuationItemsAction")
                .getArray("continuationItems")
        val contents: JsonArray = continuationItems.getObject(0)
                .getObject("itemSectionRenderer")
                .getArray("contents")
        collectStreamsFrom(collector, contents)
        return InfoItemsPage(collector, getNextPageFrom(continuationItems.getObject(1)
                .getObject("continuationItemRenderer")))
    }

    @Throws(NothingFoundException::class)
    private fun collectStreamsFrom(collector: MultiInfoItemsCollector,
                                   contents: JsonArray) {
        val timeAgoParser: TimeAgoParser? = getTimeAgoParser()
        for (content: Any in contents) {
            val item: JsonObject = content as JsonObject
            if (item.has("backgroundPromoRenderer")) {
                throw NothingFoundException(
                        YoutubeParsingHelper.getTextFromObject(item.getObject("backgroundPromoRenderer")
                                .getObject("bodyText")))
            } else if (extractVideoResults && item.has("videoRenderer")) {
                collector.commit(YoutubeStreamInfoItemExtractor(
                        item.getObject("videoRenderer"), timeAgoParser))
            } else if (extractChannelResults && item.has("channelRenderer")) {
                collector.commit(YoutubeChannelInfoItemExtractor(
                        item.getObject("channelRenderer")))
            } else if (extractPlaylistResults && item.has("playlistRenderer")) {
                collector.commit(YoutubePlaylistInfoItemExtractor(
                        item.getObject("playlistRenderer")))
            }
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getNextPageFrom(continuationItemRenderer: JsonObject): Page? {
        if (Utils.isNullOrEmpty(continuationItemRenderer)) {
            return null
        }
        val token: String = continuationItemRenderer.getObject("continuationEndpoint")
                .getObject("continuationCommand")
                .getString("token")
        val url: String = (YoutubeParsingHelper.YOUTUBEI_V1_URL + "search?key=" + YoutubeParsingHelper.getKey()
                + YoutubeParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER)
        return Page(url, token)
    }
}
