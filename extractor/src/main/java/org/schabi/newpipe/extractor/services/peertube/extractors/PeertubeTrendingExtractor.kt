package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class PeertubeTrendingExtractor(streamingService: StreamingService,
                                linkHandler: ListLinkHandler?,
                                kioskId: String) : KioskExtractor<StreamInfoItem?>(streamingService, linkHandler, kioskId) {
    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return getId()
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            return getPage(Page((getUrl() + "&" + PeertubeParsingHelper.START_KEY + "=0&"
                    + PeertubeParsingHelper.COUNT_KEY + "=" + PeertubeParsingHelper.ITEMS_PER_PAGE)))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val response: Response? = getDownloader().get(page.getUrl())
        var json: JsonObject? = null
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.`object`().from(response.responseBody())
            } catch (e: Exception) {
                throw ParsingException("Could not parse json data for kiosk info", e)
            }
        }
        if (json != null) {
            PeertubeParsingHelper.validate(json)
            val total: Long = json.getLong("total")
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            collectItemsFrom(collector, json, getBaseUrl())
            return InfoItemsPage(collector,
                    PeertubeParsingHelper.getNextPage(page.getUrl(), total))
        } else {
            throw ExtractionException("Unable to get PeerTube kiosk info")
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
    }
}
