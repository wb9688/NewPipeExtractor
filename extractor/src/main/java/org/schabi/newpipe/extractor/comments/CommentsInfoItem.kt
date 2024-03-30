package org.schabi.newpipe.extractor.comments

import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.Description

class CommentsInfoItem(serviceId: Int, url: String?, name: String?) : InfoItem(InfoType.COMMENT, serviceId, url, name) {
    @JvmField
    var commentId: String? = null

    @JvmField
    @get:Nonnull
    @Nonnull
    var commentText: Description? = Description.Companion.EMPTY_DESCRIPTION
    @JvmField
    var uploaderName: String? = null

    @JvmField
    @get:Nonnull
    @Nonnull
    var uploaderAvatars: List<Image?>? = listOf<Image>()
    @JvmField
    var uploaderUrl: String? = null
    var isUploaderVerified: Boolean = false
    @JvmField
    var textualUploadDate: String? = null
    @JvmField
    var uploadDate: DateWrapper? = null

    /**
     * @return the comment's like count or [CommentsInfoItem.NO_LIKE_COUNT] if it is
     * unavailable
     */
    @JvmField
    var likeCount: Int = 0
    @JvmField
    var textualLikeCount: String? = null
    var isHeartedByUploader: Boolean = false
    var isPinned: Boolean = false

    /**
     * Get the playback position of the stream to which this comment belongs.
     * This is not supported by all services.
     *
     * @return the playback position in seconds or [.NO_STREAM_POSITION] if not available
     */
    var streamPosition: Int = 0
    @JvmField
    var replyCount: Int = 0
    @JvmField
    var replies: Page? = null
    @JvmField
    var isChannelOwner: Boolean = false
    private var creatorReply: Boolean = false
    fun setCreatorReply(creatorReply: Boolean) {
        this.creatorReply = creatorReply
    }

    fun hasCreatorReply(): Boolean {
        return creatorReply
    }

    companion object {
        val NO_LIKE_COUNT: Int = -1
        val NO_STREAM_POSITION: Int = -1
        @JvmField
        val UNKNOWN_REPLY_COUNT: Int = -1
    }
}
