// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampSearchStreamInfoItemExtractor
import java.io.IOException
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

class BandcampSearchExtractor(service: StreamingService,
                              linkHandler: SearchQueryHandler?) : SearchExtractor(service, linkHandler) {
    override val searchSuggestion: String?
        get() {
            return ""
        }
    override val isCorrectedSearch: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val metaInfo: List<MetaInfo?>?
        get() {
            return emptyList<MetaInfo>()
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
        val d: Document = Jsoup.parse(getDownloader().get(page.getUrl()).responseBody())
        for (searchResult: Element in d.getElementsByClass("searchresult")) {
            val type: String = searchResult.getElementsByClass("result-info").stream()
                    .flatMap(Function<Element, Stream<out Element>>({ element: Element -> element.getElementsByClass("itemtype").stream() }))
                    .map(Function({ obj: Element -> obj.text() }))
                    .findFirst()
                    .orElse("")
            when (type) {
                "ARTIST" -> collector.commit(BandcampChannelInfoItemExtractor(searchResult))
                "ALBUM" -> collector.commit(BandcampPlaylistInfoItemExtractor(searchResult))
                "TRACK" -> collector.commit(BandcampSearchStreamInfoItemExtractor(searchResult, null))
                else -> {}
            }
        }

        // Count pages
        val pageLists: Elements = d.getElementsByClass("pagelist")
        if (pageLists.isEmpty()) {
            return InfoItemsPage(collector, null)
        }
        val pages: Elements = pageLists.stream()
                .map<Elements>(Function<Element, Elements>({ element: Element -> element.getElementsByTag("li") }))
                .findFirst()
                .orElseGet(Supplier<Elements>({ Elements() }))

        // Find current page
        var currentPage: Int = -1
        for (i in pages.indices) {
            val pageElement: Element = pages.get(i)
            if (!pageElement.getElementsByTag("span").isEmpty()) {
                currentPage = i + 1
                break
            }
        }
        assert(pages.size < 10)
        var nextUrl: String? = null
        if (currentPage < pages.size) {
            nextUrl = page.getUrl().substring(0, page.getUrl().length - 1) + (currentPage + 1)
        }
        return InfoItemsPage(collector, Page(nextUrl))
    }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            return getPage(Page(getUrl()))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
    }
}
