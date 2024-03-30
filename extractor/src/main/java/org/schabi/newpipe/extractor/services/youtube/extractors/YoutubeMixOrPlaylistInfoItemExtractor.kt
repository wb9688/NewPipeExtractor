package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils

class YoutubeMixOrPlaylistInfoItemExtractor(private val mixInfoItem: JsonObject) : PlaylistInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextFromObject(mixInfoItem.getObject("title"))
            if (Utils.isNullOrEmpty(name)) {
                throw ParsingException("Could not get name")
            }
            return name
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            val url: String = mixInfoItem.getString("shareUrl")
            if (Utils.isNullOrEmpty(url)) {
                throw ParsingException("Could not get url")
            }
            return url
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return YoutubeParsingHelper.getThumbnailsFromInfoItem(mixInfoItem)
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            // this will be a list of uploaders for mixes
            return YoutubeParsingHelper.getTextFromObject(mixInfoItem.getObject("longBylineText"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            // They're auto-generated, so there's no uploader
            return null
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            // They're auto-generated, so there's no uploader
            return false
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            val countString: String? = YoutubeParsingHelper.getTextFromObject(
                    mixInfoItem.getObject("videoCountShortText"))
            if (countString == null) {
                throw ParsingException("Could not extract item count for playlist/mix info item")
            }
            try {
                return countString.toInt().toLong()
            } catch (ignored: NumberFormatException) {
                // un-parsable integer: this is a mix with infinite items and "50+" as count string
                // (though YouTube Music mixes do not necessarily have an infinite count of songs)
                return ListExtractor.Companion.ITEM_COUNT_INFINITE
            }
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val playlistType: PlaylistType?
        get() {
            return YoutubeParsingHelper.extractPlaylistTypeFromPlaylistUrl(url)
        }
}
