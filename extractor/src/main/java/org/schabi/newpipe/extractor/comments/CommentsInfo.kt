package org.schabi.newpipe.extractor.comments

import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListInfo
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.utils.ExtractorHelper
import java.io.IOException

class CommentsInfo private constructor(
        serviceId: Int,
        listUrlIdHandler: ListLinkHandler?,
        name: String?) : ListInfo<CommentsInfoItem?>(serviceId, listUrlIdHandler, name) {
    @Transient
    var commentsExtractor: CommentsExtractor? = null
    /**
     * @return `true` if the comments are disabled otherwise `false` (default)
     * @see CommentsExtractor.isCommentsDisabled
     */
    /**
     * @param commentsDisabled `true` if the comments are disabled otherwise `false`
     */
    var isCommentsDisabled: Boolean = false
    /**
     * Returns the total number of comments.
     *
     * @return the total number of comments
     */
    /**
     * Sets the total number of comments.
     *
     * @param commentsCount the commentsCount to set.
     */
    var commentsCount: Int = 0

    companion object {
        @JvmStatic
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): CommentsInfo? {
            return getInfo(NewPipe.getServiceByUrl(url), url)
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(service: StreamingService?, url: String?): CommentsInfo? {
            return getInfo(service!!.getCommentsExtractor(url))
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(commentsExtractor: CommentsExtractor?): CommentsInfo? {
            // for services which do not have a comments extractor
            if (commentsExtractor == null) {
                return null
            }
            commentsExtractor.fetchPage()
            val name: String? = commentsExtractor.getName()
            val serviceId: Int = commentsExtractor.getServiceId()
            val listUrlIdHandler: ListLinkHandler? = commentsExtractor.getLinkHandler()
            val commentsInfo: CommentsInfo = CommentsInfo(serviceId, listUrlIdHandler, name)
            commentsInfo.commentsExtractor = commentsExtractor
            val initialCommentsPage: InfoItemsPage<CommentsInfoItem?>? = ExtractorHelper.getItemsPageOrLogError(commentsInfo, (commentsExtractor))
            commentsInfo.isCommentsDisabled = commentsExtractor.isCommentsDisabled()
            commentsInfo.setRelatedItems(initialCommentsPage.getItems())
            try {
                commentsInfo.commentsCount = commentsExtractor.getCommentsCount()
            } catch (e: Exception) {
                commentsInfo.addError(e)
            }
            commentsInfo.setNextPage(initialCommentsPage.getNextPage())
            return commentsInfo
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getMoreItems(
                commentsInfo: CommentsInfo,
                page: Page?): InfoItemsPage<CommentsInfoItem?>? {
            return getMoreItems(NewPipe.getService(commentsInfo.getServiceId()), commentsInfo.getUrl(),
                    page)
        }

        @JvmStatic
        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(
                service: StreamingService?,
                commentsInfo: CommentsInfo,
                page: Page?): InfoItemsPage<CommentsInfoItem?>? {
            return getMoreItems(service, commentsInfo.getUrl(), page)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getMoreItems(
                service: StreamingService?,
                url: String?,
                page: Page?): InfoItemsPage<CommentsInfoItem?>? {
            return service!!.getCommentsExtractor(url)!!.getPage(page)
        }
    }
}
