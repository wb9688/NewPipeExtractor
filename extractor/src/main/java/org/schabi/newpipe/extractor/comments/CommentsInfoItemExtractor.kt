package org.schabi.newpipe.extractor.comments

import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsInfoItemExtractor
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamExtractor

open interface CommentsInfoItemExtractor : InfoItemExtractor {
    @get:Throws(ParsingException::class)
    val likeCount: Int
        /**
         * Return the like count of the comment,
         * or [CommentsInfoItem.NO_LIKE_COUNT] if it is unavailable.
         *
         * <br></br>
         *
         *
         * NOTE: Currently only implemented for YT [ ][YoutubeCommentsInfoItemExtractor.getLikeCount]
         * with limitations (only approximate like count is returned)
         *
         * @return the comment's like count
         * or [CommentsInfoItem.NO_LIKE_COUNT] if it is unavailable
         * @see StreamExtractor.getLikeCount
         */
        get() {
            return CommentsInfoItem.Companion.NO_LIKE_COUNT
        }

    @get:Throws(ParsingException::class)
    val textualLikeCount: String?
        /**
         * The unmodified like count given by the service
         * <br></br>
         * It may be language dependent
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    val commentText: Description
        /**
         * The text of the comment
         */
        get() {
            return Description.Companion.EMPTY_DESCRIPTION
        }

    @get:Throws(ParsingException::class)
    val textualUploadDate: String?
        /**
         * The upload date given by the service, unmodified
         *
         * @see StreamExtractor.getTextualUploadDate
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    val uploadDate: DateWrapper?
        /**
         * The upload date wrapped with DateWrapper class
         *
         * @see StreamExtractor.getUploadDate
         */
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    val commentId: String?
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    val uploaderUrl: String?
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    val uploaderName: String?
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    val uploaderAvatars: List<Image?>?
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    val isHeartedByUploader: Boolean
        /**
         * Whether the comment has been hearted by the uploader
         */
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    val isPinned: Boolean
        /**
         * Whether the comment is pinned
         */
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    val isUploaderVerified: Boolean
        /**
         * Whether the uploader is verified by the service
         */
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    val streamPosition: Int
        /**
         * The playback position of the stream to which this comment belongs.
         *
         * @see CommentsInfoItem.getStreamPosition
         */
        get() {
            return CommentsInfoItem.Companion.NO_STREAM_POSITION
        }

    @get:Throws(ParsingException::class)
    val replyCount: Int
        /**
         * The count of comment replies.
         *
         * @return the count of the replies
         * or [CommentsInfoItem.UNKNOWN_REPLY_COUNT] if replies are not supported
         */
        get() {
            return CommentsInfoItem.Companion.UNKNOWN_REPLY_COUNT
        }

    @get:Throws(ParsingException::class)
    val replies: Page?
        /**
         * The continuation page which is used to get comment replies from.
         *
         * @return the continuation Page for the replies, or null if replies are not supported
         */
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    val isChannelOwner: Boolean
        /**
         * Whether the comment was made by the channel owner.
         */
        get() {
            return false
        }

    /**
     * Whether the comment was replied to by the creator.
     */
    @Throws(ParsingException::class)
    fun hasCreatorReply(): Boolean {
        return false
    }
}
