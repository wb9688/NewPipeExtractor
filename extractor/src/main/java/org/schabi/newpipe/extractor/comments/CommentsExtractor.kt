package org.schabi.newpipe.extractor.comments

import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler

abstract class CommentsExtractor(service: StreamingService, uiHandler: ListLinkHandler?) : ListExtractor<CommentsInfoItem?>(service, uiHandler) {
    @get:Throws(ExtractionException::class)
    open val isCommentsDisabled: Boolean
        /**
         * @apiNote Warning: This method is experimental and may get removed in a future release.
         * @return `true` if the comments are disabled otherwise `false` (default)
         */
        get() {
            return false
        }

    @get:Throws(ExtractionException::class)
    open val commentsCount: Int
        /**
         * @return the total number of comments
         */
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return "Comments"
        }
}
