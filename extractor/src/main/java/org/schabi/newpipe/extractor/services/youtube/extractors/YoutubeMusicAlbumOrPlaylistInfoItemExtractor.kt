package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils

class YoutubeMusicAlbumOrPlaylistInfoItemExtractor(private val albumOrPlaylistInfoItem: JsonObject,
                                                   private val descriptionElements: JsonArray,
                                                   private val searchType: String) : PlaylistInfoItemExtractor {
    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(
                        albumOrPlaylistInfoItem.getObject("thumbnail")
                                .getObject("musicThumbnailRenderer")
                                .getObject("thumbnail")
                                .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextFromObject(albumOrPlaylistInfoItem.getArray("flexColumns")
                    .getObject(0)
                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                    .getObject("text"))
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            throw ParsingException("Could not get name")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            var playlistId: String = albumOrPlaylistInfoItem.getObject("menu")
                    .getObject("menuRenderer")
                    .getArray("items")
                    .getObject(4)
                    .getObject("toggleMenuServiceItemRenderer")
                    .getObject("toggledServiceEndpoint")
                    .getObject("likeEndpoint")
                    .getObject("target")
                    .getString("playlistId")
            if (Utils.isNullOrEmpty(playlistId)) {
                playlistId = albumOrPlaylistInfoItem.getObject("overlay")
                        .getObject("musicItemThumbnailOverlayRenderer")
                        .getObject("content")
                        .getObject("musicPlayButtonRenderer")
                        .getObject("playNavigationEndpoint")
                        .getObject("watchPlaylistEndpoint")
                        .getString("playlistId")
            }
            if (!Utils.isNullOrEmpty(playlistId)) {
                return "https://music.youtube.com/playlist?list=" + playlistId
            }
            throw ParsingException("Could not get URL")
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            val name: String
            if ((searchType == YoutubeSearchQueryHandlerFactory.Companion.MUSIC_ALBUMS)) {
                name = descriptionElements.getObject(2).getString("text")
            } else {
                name = descriptionElements.getObject(0).getString("text")
            }
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            throw ParsingException("Could not get uploader name")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            if ((searchType == YoutubeSearchQueryHandlerFactory.Companion.MUSIC_PLAYLISTS)) {
                return null
            }
            val items: JsonArray = albumOrPlaylistInfoItem.getObject("menu")
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
            throw ParsingException("Could not get uploader URL")
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            if ((searchType == YoutubeSearchQueryHandlerFactory.Companion.MUSIC_ALBUMS)) {
                return ListExtractor.Companion.ITEM_COUNT_UNKNOWN
            }
            val count: String = descriptionElements.getObject(2)
                    .getString("text")
            if (!Utils.isNullOrEmpty(count)) {
                if (count.contains("100+")) {
                    return ListExtractor.Companion.ITEM_COUNT_MORE_THAN_100
                } else {
                    return Utils.removeNonDigitCharacters(count).toLong()
                }
            }
            throw ParsingException("Could not get stream count")
        }
}
