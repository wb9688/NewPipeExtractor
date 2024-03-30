package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class PeertubeChannelTabExtractor(service: StreamingService,
                                  linkHandler: ListLinkHandler?) : ChannelTabExtractor(service, linkHandler) {
    private override val baseUrl: String?

    init {
        baseUrl = getBaseUrl()
    }

    public override fun onFetchPage(downloader: Downloader?) {}

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            return getPage(Page((baseUrl + PeertubeChannelLinkHandlerFactory.Companion.API_ENDPOINT
                    + getId() + PeertubeChannelTabLinkHandlerFactory.Companion.getUrlSuffix(getName()) + "?" + PeertubeParsingHelper.START_KEY + "=0&" + PeertubeParsingHelper.COUNT_KEY + "="
                    + PeertubeParsingHelper.ITEMS_PER_PAGE)))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val response: Response? = getDownloader().get(page.getUrl())
        var pageJson: JsonObject? = null
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                pageJson = JsonParser.`object`().from(response.responseBody())
            } catch (e: Exception) {
                throw ParsingException("Could not parse json data for account info", e)
            }
        }
        if (pageJson == null) {
            throw ExtractionException("Unable to get account channel list")
        }
        PeertubeParsingHelper.validate(pageJson)
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
        collectItemsFrom(collector, pageJson, getBaseUrl())
        return InfoItemsPage(collector,
                PeertubeParsingHelper.getNextPage(page.getUrl(), pageJson.getLong("total")))
    }
}
