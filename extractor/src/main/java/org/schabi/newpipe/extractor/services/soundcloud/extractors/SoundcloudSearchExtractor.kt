package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.util.function.IntUnaryOperator

class SoundcloudSearchExtractor(service: StreamingService,
                                linkHandler: SearchQueryHandler?) : SearchExtractor(service, linkHandler) {
    private var initialSearchObject: JsonObject? = null

    @get:Nonnull
    override val searchSuggestion: String?
        get() {
            return ""
        }
    override val isCorrectedSearch: Boolean
        get() {
            return false
        }

    @get:Nonnull
    override val metaInfo: List<MetaInfo?>?
        get() {
            return emptyList<MetaInfo>()
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            if (initialSearchObject!!.getInt(TOTAL_RESULTS) > SoundcloudSearchQueryHandlerFactory.Companion.ITEMS_PER_PAGE) {
                return InfoItemsPage<InfoItem?>(
                        collectItems(initialSearchObject!!.getArray(COLLECTION)),
                        getNextPageFromCurrentUrl(getUrl(), IntUnaryOperator({ currentOffset: Int -> SoundcloudSearchQueryHandlerFactory.Companion.ITEMS_PER_PAGE })))
            } else {
                return InfoItemsPage(
                        collectItems(initialSearchObject!!.getArray(COLLECTION)), null)
            }
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val dl: Downloader? = getDownloader()
        val searchCollection: JsonArray
        val totalResults: Int
        try {
            val response: String? = dl.get(page.getUrl(), getExtractorLocalization())
                    .responseBody()
            val result: JsonObject = JsonParser.`object`().from(response)
            searchCollection = result.getArray(COLLECTION)
            totalResults = result.getInt(TOTAL_RESULTS)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        if (getOffsetFromUrl(page.getUrl()) + SoundcloudSearchQueryHandlerFactory.Companion.ITEMS_PER_PAGE < totalResults) {
            return InfoItemsPage<InfoItem?>(collectItems(searchCollection),
                    getNextPageFromCurrentUrl(page.getUrl(),
                            IntUnaryOperator({ currentOffset: Int -> currentOffset + SoundcloudSearchQueryHandlerFactory.Companion.ITEMS_PER_PAGE })))
        }
        return InfoItemsPage(collectItems(searchCollection), null)
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val dl: Downloader? = getDownloader()
        val url: String? = getUrl()
        try {
            val response: String? = dl.get(url, getExtractorLocalization()).responseBody()
            initialSearchObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        if (initialSearchObject.getArray(COLLECTION).isEmpty()) {
            throw NothingFoundException("Nothing found")
        }
    }

    private fun collectItems(
            searchCollection: JsonArray): InfoItemsCollector<InfoItem?, InfoItemExtractor> {
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
        for (result: Any in searchCollection) {
            if (!(result is JsonObject)) {
                continue
            }
            val searchResult: JsonObject = result
            val kind: String = searchResult.getString("kind", "")
            when (kind) {
                "user" -> collector.commit(SoundcloudChannelInfoItemExtractor(searchResult))
                "track" -> collector.commit(SoundcloudStreamInfoItemExtractor(searchResult))
                "playlist" -> collector.commit(SoundcloudPlaylistInfoItemExtractor(searchResult))
            }
        }
        return collector
    }

    @Throws(ParsingException::class)
    private fun getNextPageFromCurrentUrl(currentUrl: String?,
                                          newPageOffsetCalculator: IntUnaryOperator): Page {
        val currentPageOffset: Int = getOffsetFromUrl(currentUrl)
        return Page(
                currentUrl!!.replace(
                        "&offset=" + currentPageOffset,
                        "&offset=" + newPageOffsetCalculator.applyAsInt(currentPageOffset)))
    }

    @Throws(ParsingException::class)
    private fun getOffsetFromUrl(url: String?): Int {
        try {
            return Parser.compatParseMap(URL(url).getQuery()).get("offset")!!.toInt()
        } catch (e: MalformedURLException) {
            throw ParsingException("Could not get offset from page URL", e)
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not get offset from page URL", e)
        }
    }

    companion object {
        private val COLLECTION: String = "collection"
        private val TOTAL_RESULTS: String = "total_results"
    }
}
