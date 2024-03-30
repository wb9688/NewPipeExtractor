package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils

open class YoutubePlaylistInfoItemExtractor(private val playlistInfoItem: JsonObject) : PlaylistInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            try {
                var thumbnails: JsonArray = playlistInfoItem.getArray("thumbnails")
                        .getObject(0)
                        .getArray("thumbnails")
                if (thumbnails.isEmpty()) {
                    thumbnails = playlistInfoItem.getObject("thumbnail")
                            .getArray("thumbnails")
                }
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(thumbnails)
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(playlistInfoItem.getObject("title"))
            } catch (e: Exception) {
                throw ParsingException("Could not get name", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            try {
                val id: String = playlistInfoItem.getString("playlistId")
                return YoutubePlaylistLinkHandlerFactory.Companion.getInstance().getUrl(id)
            } catch (e: Exception) {
                throw ParsingException("Could not get url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(playlistInfoItem.getObject("longBylineText"))
            } catch (e: Exception) {
                throw ParsingException("Could not get uploader name", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            try {
                return YoutubeParsingHelper.getUrlFromObject(playlistInfoItem.getObject("longBylineText"))
            } catch (e: Exception) {
                throw ParsingException("Could not get uploader url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            try {
                return YoutubeParsingHelper.isVerified(playlistInfoItem.getArray("ownerBadges"))
            } catch (e: Exception) {
                throw ParsingException("Could not get uploader verification info", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            var videoCountText: String? = playlistInfoItem.getString("videoCount")
            if (videoCountText == null) {
                videoCountText = YoutubeParsingHelper.getTextFromObject(playlistInfoItem.getObject("videoCountText"))
            }
            if (videoCountText == null) {
                videoCountText = YoutubeParsingHelper.getTextFromObject(playlistInfoItem.getObject("videoCountShortText"))
            }
            if (videoCountText == null) {
                throw ParsingException("Could not get stream count")
            }
            try {
                return Utils.removeNonDigitCharacters(videoCountText).toLong()
            } catch (e: Exception) {
                throw ParsingException("Could not get stream count", e)
            }
        }
}
