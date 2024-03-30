package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.utils.Utils

class SoundcloudPlaylistInfoItemExtractor(private val itemObject: JsonObject) : PlaylistInfoItemExtractor {
    override val name: String?
        get() {
            return itemObject.getString("title")
        }
    override val url: String?
        get() {
            return Utils.replaceHttpWithHttps(itemObject.getString("permalink_url"))
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            // Over-engineering at its finest
            if (itemObject.isString(ARTWORK_URL_KEY)) {
                val artworkUrl: String = itemObject.getString(ARTWORK_URL_KEY)
                if (!Utils.isNullOrEmpty(artworkUrl)) {
                    return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
                }
            }
            try {
                // Look for artwork URL inside the track list
                for (track: Any in itemObject.getArray("tracks")) {
                    val trackObject: JsonObject = track as JsonObject

                    // First look for track artwork URL
                    if (trackObject.isString(ARTWORK_URL_KEY)) {
                        val artworkUrl: String = trackObject.getString(ARTWORK_URL_KEY)
                        if (!Utils.isNullOrEmpty(artworkUrl)) {
                            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
                        }
                    }

                    // Then look for track creator avatar URL
                    val creator: JsonObject = trackObject.getObject(USER_KEY)
                    val creatorAvatar: String = creator.getString(AVATAR_URL_KEY)
                    if (!Utils.isNullOrEmpty(creatorAvatar)) {
                        return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(creatorAvatar)
                    }
                }
            } catch (ignored: Exception) {
                // Try other method
            }
            try {
                // Last resort, use user avatar URL. If still not found, then throw an exception.
                return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(
                        itemObject.getObject(USER_KEY).getString(AVATAR_URL_KEY))
            } catch (e: Exception) {
                throw ParsingException("Failed to extract playlist thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            try {
                return itemObject.getObject(USER_KEY).getString("username")
            } catch (e: Exception) {
                throw ParsingException("Failed to extract playlist uploader", e)
            }
        }
    override val uploaderUrl: String?
        get() {
            return itemObject.getObject(USER_KEY).getString("permalink_url")
        }
    override val isUploaderVerified: Boolean
        get() {
            return itemObject.getObject(USER_KEY).getBoolean("verified")
        }
    override val streamCount: Long
        get() {
            return itemObject.getLong("track_count")
        }

    companion object {
        private val USER_KEY: String = "user"
        private val AVATAR_URL_KEY: String = "avatar_url"
        private val ARTWORK_URL_KEY: String = "artwork_url"
    }
}
