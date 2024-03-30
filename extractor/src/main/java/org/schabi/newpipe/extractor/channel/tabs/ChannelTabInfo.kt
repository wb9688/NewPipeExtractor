package org.schabi.newpipe.extractor.channel.tabs

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.utils.ExtractorHelper
import java.io.IOException

class ChannelTabInfo(serviceId: Int,
                     linkHandler: ListLinkHandler) : ListInfo<InfoItem?>(serviceId, linkHandler, linkHandler.contentFilters[0]) {
    companion object {
        /**
         * Get a [ChannelTabInfo] instance from the given service and tab handler.
         *
         * @param service streaming service
         * @param linkHandler Channel tab handler (from [ChannelInfo])
         * @return the extracted [ChannelTabInfo]
         */
        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(service: StreamingService,
                    linkHandler: ListLinkHandler?): ChannelTabInfo {
            val extractor: ChannelTabExtractor? = service.getChannelTabExtractor(linkHandler)
            extractor!!.fetchPage()
            return getInfo(extractor)
        }

        /**
         * Get a [ChannelTabInfo] instance from a [ChannelTabExtractor].
         *
         * @param extractor an extractor where `fetchPage()` was already got called on
         * @return the extracted [ChannelTabInfo]
         */
        fun getInfo(extractor: ChannelTabExtractor): ChannelTabInfo {
            val info = ChannelTabInfo(extractor.serviceId, extractor.linkHandler as ListLinkHandler)
            try {
                info.originalUrl = extractor.originalUrl
            } catch (e: Exception) {
                info.addError(e)
            }
            val page: InfoItemsPage<InfoItem> = ExtractorHelper.getItemsPageOrLogError(info, (extractor))
            info.relatedItems = page.items
            info.nextPage = page.nextPage
            return info
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getMoreItems(
                service: StreamingService,
                linkHandler: ListLinkHandler?,
                page: Page?): InfoItemsPage<InfoItem?>? {
            return service.getChannelTabExtractor(linkHandler)!!.getPage(page)
        }
    }
}
