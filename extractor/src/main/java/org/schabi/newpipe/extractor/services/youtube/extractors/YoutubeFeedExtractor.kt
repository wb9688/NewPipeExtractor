package org.schabi.newpipe.extractor.services.youtube.extractors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.feed.FeedExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import java.io.IOException

class YoutubeFeedExtractor(service: StreamingService, linkHandler: ListLinkHandler?) : FeedExtractor(service, linkHandler) {
    private var document: Document? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val channelIdOrUser: String? = getLinkHandler().getId()
        val feedUrl: String? = YoutubeParsingHelper.getFeedUrlFrom(channelIdOrUser)
        val response: Response? = downloader!!.get(feedUrl)
        if (response!!.responseCode() == 404) {
            throw ContentNotAvailableException("Could not get feed: 404 - not found")
        }
        document = Jsoup.parse(response.responseBody())
    }

    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val entries: Elements = document!!.select("feed > entry")
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            for (entryElement: Element in entries) {
                collector.commit(YoutubeFeedInfoItemExtractor(entryElement))
            }
            return InfoItemsPage(collector, null)
        }

    @get:Nonnull
    override val id: String?
        get() {
            return url!!.replace(WEBSITE_CHANNEL_BASE_URL, "")
        }

    @get:Nonnull
    override val url: String?
        get() {
            val authorUriElement: Element? = document!!.select("feed > author > uri")
                    .first()
            if (authorUriElement != null) {
                val authorUriElementText: String = authorUriElement.text()
                if (!(authorUriElementText == "")) {
                    return authorUriElementText
                }
            }
            val linkElement: Element? = document!!.select("feed > link[rel*=alternate]")
                    .first()
            if (linkElement != null) {
                return linkElement.attr("href")
            }
            return ""
        }

    @get:Nonnull
    override val name: String?
        get() {
            val nameElement: Element? = document!!.select("feed > author > name")
                    .first()
            if (nameElement == null) {
                return ""
            }
            return nameElement.text()
        }

    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<StreamInfoItem?>()
    }

    companion object {
        private val WEBSITE_CHANNEL_BASE_URL: String = "https://www.youtube.com/channel/"
    }
}
