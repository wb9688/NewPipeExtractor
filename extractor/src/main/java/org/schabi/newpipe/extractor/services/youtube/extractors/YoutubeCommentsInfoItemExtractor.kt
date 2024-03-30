package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils

class YoutubeCommentsInfoItemExtractor(private val json: JsonObject,
                                       @get:Throws(ParsingException::class) override val url: String?,
                                       private val timeAgoParser: TimeAgoParser?) : CommentsInfoItemExtractor {
    @get:Throws(ParsingException::class)
    private var commentRenderer: JsonObject? = null
        private get() {
            if (field == null) {
                if (json.has("comment")) {
                    field = JsonUtils.getObject(json, "comment.commentRenderer")
                } else {
                    field = json
                }
            }
            return field
        }

    @get:Throws(ParsingException::class)
    private val authorThumbnails: List<Image?>?
        private get() {
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(JsonUtils.getArray(commentRenderer,
                        "authorThumbnail.thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get author thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return authorThumbnails
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(JsonUtils.getObject(commentRenderer, "authorText"))
            } catch (e: Exception) {
                return ""
            }
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(JsonUtils.getObject(commentRenderer,
                        "publishedTimeText"))
            } catch (e: Exception) {
                throw ParsingException("Could not get publishedTimeText", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualPublishedTime: String? = textualUploadDate
            if ((timeAgoParser != null) && (textualPublishedTime != null
                            ) && !textualPublishedTime.isEmpty()) {
                return timeAgoParser.parse(textualPublishedTime)
            } else {
                return null
            }
        }

    @get:Throws(ParsingException::class)
    override val likeCount: Int
        /**
         * @implNote The method tries first to get the exact like count by using the accessibility data
         * returned. But if the parsing of this accessibility data fails, the method parses internally
         * a localized string.
         * <br></br>
         *
         *  * More than 1k likes will result in an inaccurate number
         *  * This will fail for other languages than English. However as long as the Extractor
         * only uses "en-GB" (as seen in [     ][org.schabi.newpipe.extractor.services.youtube.YoutubeService.getSupportedLocalizations])
         * , everything will work fine.
         *
         * <br></br>
         * Consider using [.getTextualLikeCount]
         */
        get() {
            // Try first to get the exact like count by using the accessibility data
            val likeCount: String?
            try {
                likeCount = Utils.removeNonDigitCharacters(JsonUtils.getString(commentRenderer, (
                        "actionButtons.commentActionButtonsRenderer.likeButton.toggleButtonRenderer"
                                + ".accessibilityData.accessibilityData.label")))
            } catch (e: Exception) {
                // Use the approximate like count returned into the voteCount object
                // This may return a language dependent version, e.g. in German: 3,3 Mio
                val textualLikeCount: String? = textualLikeCount
                try {
                    if (Utils.isBlank(textualLikeCount)) {
                        return 0
                    }
                    return Utils.mixedNumberWordToLong(textualLikeCount).toInt()
                } catch (i: Exception) {
                    throw ParsingException(
                            "Unexpected error while converting textual like count to like count", i)
                }
            }
            try {
                if (Utils.isBlank(likeCount)) {
                    return 0
                }
                return likeCount.toInt()
            } catch (e: Exception) {
                throw ParsingException("Unexpected error while parsing like count as Integer", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val textualLikeCount: String?
        get() {
            /*
         * Example results as of 2021-05-20:
         * Language = English
         * 3.3M
         * 48K
         * 1.4K
         * 270K
         * 19
         * 6
         *
         * Language = German
         * 3,3 Mio
         * 48.189
         * 1419
         * 270.984
         * 19
         * 6
         */
            try {
                // If a comment has no likes voteCount is not set
                if (!commentRenderer!!.has("voteCount")) {
                    return ""
                }
                val voteCountObj: JsonObject? = JsonUtils.getObject(commentRenderer, "voteCount")
                if (voteCountObj!!.isEmpty()) {
                    return ""
                }
                return YoutubeParsingHelper.getTextFromObject(voteCountObj)
            } catch (e: Exception) {
                throw ParsingException("Could not get the vote count", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val commentText: Description
        get() {
            try {
                val contentText: JsonObject? = JsonUtils.getObject(commentRenderer, "contentText")
                if (contentText!!.isEmpty()) {
                    // completely empty comments as described in
                    // https://github.com/TeamNewPipe/NewPipeExtractor/issues/380#issuecomment-668808584
                    return Description.Companion.EMPTY_DESCRIPTION
                }
                val commentText: String? = YoutubeParsingHelper.getTextFromObject(contentText, true)
                // YouTube adds U+FEFF in some comments.
                // eg. https://www.youtube.com/watch?v=Nj4F63E59io<feff>
                val commentTextBomRemoved: String? = Utils.removeUTF8BOM(commentText)
                return Description(commentTextBomRemoved, Description.Companion.HTML)
            } catch (e: Exception) {
                throw ParsingException("Could not get comment text", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val commentId: String?
        get() {
            try {
                return JsonUtils.getString(commentRenderer, "commentId")
            } catch (e: Exception) {
                throw ParsingException("Could not get comment id", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderAvatars: List<Image?>?
        get() {
            return authorThumbnails
        }

    @get:Throws(ParsingException::class)
    override val isHeartedByUploader: Boolean
        get() {
            val commentActionButtonsRenderer: JsonObject = commentRenderer
                    .getObject("actionButtons")
                    .getObject("commentActionButtonsRenderer")
            return commentActionButtonsRenderer.has("creatorHeart")
        }

    @get:Throws(ParsingException::class)
    override val isPinned: Boolean
        get() {
            return commentRenderer!!.has("pinnedCommentBadge")
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return commentRenderer!!.has("authorCommentBadge")
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(JsonUtils.getObject(commentRenderer, "authorText"))
            } catch (e: Exception) {
                return ""
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            try {
                return "https://www.youtube.com/channel/" + JsonUtils.getString(commentRenderer,
                        "authorEndpoint.browseEndpoint.browseId")
            } catch (e: Exception) {
                return ""
            }
        }

    @get:Throws(ParsingException::class)
    override val replyCount: Int
        get() {
            val commentRendererJsonObject: JsonObject? = commentRenderer
            if (commentRendererJsonObject!!.has("replyCount")) {
                return commentRendererJsonObject.getInt("replyCount")
            }
            return CommentsInfoItem.Companion.UNKNOWN_REPLY_COUNT
        }
    override val replies: Page?
        get() {
            try {
                val id: String? = JsonUtils.getString(
                        JsonUtils.getArray(json, "replies.commentRepliesRenderer.contents")
                                .getObject(0),
                        "continuationItemRenderer.continuationEndpoint.continuationCommand.token")
                return Page(url, id)
            } catch (e: Exception) {
                return null
            }
        }

    @get:Throws(ParsingException::class)
    override val isChannelOwner: Boolean
        get() {
            return commentRenderer!!.getBoolean("authorIsChannelOwner")
        }

    @Throws(ParsingException::class)
    public override fun hasCreatorReply(): Boolean {
        try {
            val commentRepliesRenderer: JsonObject? = JsonUtils.getObject(json,
                    "replies.commentRepliesRenderer")
            return commentRepliesRenderer!!.has("viewRepliesCreatorThumbnail")
        } catch (e: Exception) {
            return false
        }
    }
}
