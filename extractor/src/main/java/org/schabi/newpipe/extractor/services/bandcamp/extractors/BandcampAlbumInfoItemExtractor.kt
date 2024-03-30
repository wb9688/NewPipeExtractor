package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor

class BandcampAlbumInfoItemExtractor(private val albumInfoItem: JsonObject,
                                     override val uploaderUrl: String?) : PlaylistInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return albumInfoItem.getString("title")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return BandcampExtractorHelper.getStreamUrlFromIds(
                    albumInfoItem.getLong("band_id"),
                    albumInfoItem.getLong("item_id"),
                    albumInfoItem.getString("item_type"))
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageId(albumInfoItem.getLong("art_id"), true)
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return albumInfoItem.getString("band_name")
        }
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val streamCount: Long
        get() {
            return ListExtractor.Companion.ITEM_COUNT_UNKNOWN
        }
}
