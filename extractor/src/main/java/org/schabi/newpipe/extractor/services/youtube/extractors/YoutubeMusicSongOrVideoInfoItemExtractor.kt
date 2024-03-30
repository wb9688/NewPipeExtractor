package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.Utils

class YoutubeMusicSongOrVideoInfoItemExtractor(private val songOrVideoInfoItem: JsonObject,
                                               private val descriptionElements: JsonArray,
                                               private val searchType: String) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            val id: String = songOrVideoInfoItem.getObject("playlistItemData").getString("videoId")
            if (!Utils.isNullOrEmpty(id)) {
                return "https://music.youtube.com/watch?v=" + id
            }
            throw ParsingException("Could not get URL")
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextFromObject(songOrVideoInfoItem.getArray("flexColumns")
                    .getObject(0)
                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                    .getObject("text"))
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            throw ParsingException("Could not get name")
        }
    override val streamType: StreamType
        get() {
            return StreamType.VIDEO_STREAM
        }
    override val isAd: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val duration: Long
        get() {
            val duration: String = descriptionElements.getObject(descriptionElements.size - 1)
                    .getString("text")
            if (!Utils.isNullOrEmpty(duration)) {
                return YoutubeParsingHelper.parseDurationString(duration).toLong()
            }
            throw ParsingException("Could not get duration")
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            val name: String = descriptionElements.getObject(0).getString("text")
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            throw ParsingException("Could not get uploader name")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            if ((searchType == YoutubeSearchQueryHandlerFactory.Companion.MUSIC_VIDEOS)) {
                val items: JsonArray = songOrVideoInfoItem.getObject("menu")
                        .getObject("menuRenderer")
                        .getArray("items")
                for (item: Any in items) {
                    val menuNavigationItemRenderer: JsonObject = (item as JsonObject).getObject("menuNavigationItemRenderer")
                    if ((menuNavigationItemRenderer.getObject("icon")
                                    .getString("iconType", "")
                                    == "ARTIST")) {
                        return YoutubeParsingHelper.getUrlFromNavigationEndpoint(
                                menuNavigationItemRenderer.getObject("navigationEndpoint"))
                    }
                }
                return null
            } else {
                val navigationEndpointHolder: JsonObject = songOrVideoInfoItem.getArray("flexColumns")
                        .getObject(1)
                        .getObject("musicResponsiveListItemFlexColumnRenderer")
                        .getObject("text")
                        .getArray("runs")
                        .getObject(0)
                if (!navigationEndpointHolder.has("navigationEndpoint")) {
                    return null
                }
                val url: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(
                        navigationEndpointHolder.getObject("navigationEndpoint"))
                if (!Utils.isNullOrEmpty(url)) {
                    return url
                }
                throw ParsingException("Could not get uploader URL")
            }
        }
    override val isUploaderVerified: Boolean
        get() {
            // We don't have the ability to know this information on YouTube Music
            return false
        }
    override val textualUploadDate: String?
        get() {
            return null
        }
    override val uploadDate: DateWrapper?
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            if ((searchType == YoutubeSearchQueryHandlerFactory.Companion.MUSIC_SONGS)) {
                return -1
            }
            val viewCount: String = descriptionElements
                    .getObject(descriptionElements.size - 3)
                    .getString("text")
            if (!Utils.isNullOrEmpty(viewCount)) {
                try {
                    return Utils.mixedNumberWordToLong(viewCount)
                } catch (e: RegexException) {
                    // probably viewCount == "No views" or similar
                    return 0
                }
            }
            throw ParsingException("Could not get view count")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(
                        songOrVideoInfoItem.getObject("thumbnail")
                                .getObject("musicThumbnailRenderer")
                                .getObject("thumbnail")
                                .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }
}
