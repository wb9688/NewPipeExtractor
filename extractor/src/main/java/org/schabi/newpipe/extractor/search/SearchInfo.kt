package org.schabi.newpipe.extractor.search

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.utils.ExtractorHelper
import java.io.IOException

class SearchInfo(serviceId: Int,
                 qIHandler: SearchQueryHandler?,
        // Getter
                 val searchString: String?) : ListInfo<InfoItem?>(serviceId, qIHandler, "Search") {
    var searchSuggestion: String? = null
    var isCorrectedSearch: Boolean = false

    @get:Nonnull
    var metaInfo: List<MetaInfo?>? = listOf<MetaInfo>()

    companion object {
        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(service: StreamingService,
                    searchQuery: SearchQueryHandler?): SearchInfo {
            val extractor: SearchExtractor? = service.getSearchExtractor(searchQuery)
            extractor!!.fetchPage()
            return getInfo(extractor)
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(extractor: SearchExtractor?): SearchInfo {
            val info: SearchInfo = SearchInfo(
                    extractor.getServiceId(),
                    extractor.getLinkHandler(),
                    extractor.getSearchString())
            try {
                info.setOriginalUrl(extractor.getOriginalUrl())
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.searchSuggestion = extractor.getSearchSuggestion()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.isCorrectedSearch = extractor!!.isCorrectedSearch()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.metaInfo = extractor.getMetaInfo()
            } catch (e: Exception) {
                info.addError(e)
            }
            val page: InfoItemsPage<InfoItem?>? = ExtractorHelper.getItemsPageOrLogError(info, (extractor)!!)
            info.setRelatedItems(page.getItems())
            info.setNextPage(page.getNextPage())
            return info
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(service: StreamingService,
                         query: SearchQueryHandler?,
                         page: Page?): InfoItemsPage<InfoItem?>? {
            return service.getSearchExtractor(query).getPage(page)
        }
    }
}
