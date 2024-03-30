package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class PeertubeSearchExtractor @JvmOverloads constructor(service: StreamingService,
                                                        linkHandler: SearchQueryHandler?,
        // if we should use PeertubeSepiaStreamInfoItemExtractor
                                                        private val sepia: Boolean = false) : SearchExtractor(service, linkHandler) {
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
            return getPage(Page((getUrl() + "&" + PeertubeParsingHelper.START_KEY + "=0&"
                    + PeertubeParsingHelper.COUNT_KEY + "=" + PeertubeParsingHelper.ITEMS_PER_PAGE)))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val response: Response? = getDownloader().get(page.getUrl())
        var json: JsonObject? = null
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.`object`().from(response.responseBody())
            } catch (e: Exception) {
                throw ParsingException("Could not parse json data for search info", e)
            }
        }
        if (json != null) {
            PeertubeParsingHelper.validate(json)
            val total: Long = json.getLong("total")
            val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            PeertubeParsingHelper.collectItemsFrom(collector, json, getBaseUrl(), sepia)
            return InfoItemsPage(collector,
                    PeertubeParsingHelper.getNextPage(page.getUrl(), total))
        } else {
            throw ExtractionException("Unable to get PeerTube search info")
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
    }
}
