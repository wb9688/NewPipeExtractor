package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.utils.Utils

class PeertubePlaylistInfoItemExtractor(@param:Nonnull private val item: JsonObject,
                                        @param:Nonnull private val baseUrl: String?) : PlaylistInfoItemExtractor {
    private val uploader: JsonObject

    init {
        uploader = item.getObject("uploader")
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return item.getString("displayName")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return item.getString("url")
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem(baseUrl, item)
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return uploader.getString("displayName")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            return uploader.getString("url")
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            return item.getInt("videosLength").toLong()
        }

    @get:Throws(ParsingException::class)
    override val description: Description
        get() {
            val description: String = item.getString("description")
            if (Utils.isNullOrEmpty(description)) {
                return Description.Companion.EMPTY_DESCRIPTION
            }
            return Description(description, Description.Companion.PLAIN_TEXT)
        }
}
