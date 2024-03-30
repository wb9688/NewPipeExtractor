package org.schabi.newpipe.extractor.kiosk

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.utils.ExtractorHelper
import java.io.IOException

/*
* Created by Christian Schabesberger on 12.08.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* KioskInfo.java is part of NewPipe Extractor.
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
class KioskInfo private constructor(serviceId: Int, linkHandler: ListLinkHandler?, name: String?) : ListInfo<StreamInfoItem?>(serviceId, linkHandler, name) {
    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(
                service: StreamingService, url: String, page: Page?): InfoItemsPage<StreamInfoItem?>? {
            return service.getKioskList().getExtractorByUrl(url, page).getPage(page)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): KioskInfo {
            return getInfo(NewPipe.getServiceByUrl(url), url)
        }

        @JvmStatic
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService?, url: String): KioskInfo {
            val extractor = service.getKioskList().getExtractorByUrl(url, null)
            extractor!!.fetchPage()
            return getInfo(extractor)
        }

        /**
         * Get KioskInfo from KioskExtractor
         *
         * @param extractor an extractor where fetchPage() was already got called on.
         */
        @Throws(ExtractionException::class)
        fun getInfo(extractor: KioskExtractor<*>?): KioskInfo {
            val info = KioskInfo(extractor.getServiceId(),
                    extractor.getLinkHandler(),
                    extractor.getName())
            val itemsPage: InfoItemsPage<StreamInfoItem?>? = ExtractorHelper.getItemsPageOrLogError<InfoItem>(info, extractor!!)
            info.relatedItems = itemsPage.getItems()
            info.nextPage = itemsPage.getNextPage()
            return info
        }
    }
}
