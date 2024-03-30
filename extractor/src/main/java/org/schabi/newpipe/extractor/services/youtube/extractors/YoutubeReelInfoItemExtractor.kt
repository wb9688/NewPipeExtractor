package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.utils.Utils
import java.util.Locale

/**
 * A [StreamInfoItemExtractor] for YouTube's `reelItemRenderers`.
 *
 *
 *
 * `reelItemRenderers` are returned on YouTube for their short-form contents on almost every
 * place and every major client. They provide a limited amount of information and do not provide
 * the exact view count, any uploader info (name, URL, avatar, verified status) and the upload date.
 *
 */
open class YoutubeReelInfoItemExtractor(@field:Nonnull @param:Nonnull private val reelInfo: JsonObject) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return YoutubeParsingHelper.getTextFromObject(reelInfo.getObject("headline"))
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            try {
                val videoId: String = reelInfo.getString("videoId")
                return YoutubeStreamLinkHandlerFactory.Companion.getInstance().getUrl(videoId)
            } catch (e: Exception) {
                throw ParsingException("Could not get URL", e)
            }
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return YoutubeParsingHelper.getThumbnailsFromInfoItem(reelInfo)
        }

    @get:Throws(ParsingException::class)
    override val streamType: StreamType
        get() {
            return StreamType.VIDEO_STREAM
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            val viewCountText: String? = YoutubeParsingHelper.getTextFromObject(reelInfo.getObject("viewCountText"))
            if (!Utils.isNullOrEmpty(viewCountText)) {
                // This approach is language dependent
                if (viewCountText!!.lowercase(Locale.getDefault()).contains("no views")) {
                    return 0
                }
                return Utils.mixedNumberWordToLong(viewCountText)
            }
            throw ParsingException("Could not get short view count")
        }
    override val isShortFormContent: Boolean
        get() {
            return true
        }

    @get:Throws(ParsingException::class)
    override val isAd: Boolean
        // All the following properties cannot be obtained from reelItemRenderers
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val duration: Long
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return null
        }
}
