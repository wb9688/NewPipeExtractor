package org.schabi.newpipe.extractor.utils

import org.schabi.newpipe.extractor.Info
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfo

object ExtractorHelper {
    fun <T : InfoItem> getItemsPageOrLogError(
            info: Info, extractor: ListExtractor<T>): InfoItemsPage<T> {
        return try {
            val page = extractor.initialPage
            info.addAllErrors(page.errors)
            page
        } catch (e: Exception) {
            info.addError(e)
            InfoItemsPage.Companion.emptyPage<T>()
        }
    }

    fun getRelatedItemsOrLogError(info: StreamInfo,
                                  extractor: StreamExtractor): List<InfoItem?>? {
        return try {
            val collector = extractor.relatedItems ?: return emptyList<InfoItem>()
            info.addAllErrors(collector.errors)
            collector.items
        } catch (e: Exception) {
            info.addError(e)
            emptyList<InfoItem>()
        }
    }

    @Deprecated("Use {@link #getRelatedItemsOrLogError(StreamInfo, StreamExtractor)}")
    fun getRelatedVideosOrLogError(info: StreamInfo,
                                   extractor: StreamExtractor): List<InfoItem?>? {
        return getRelatedItemsOrLogError(info, extractor)
    }
}
