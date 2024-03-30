package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.utils.Utils

class BandcampPlaylistInfoItemFeaturedExtractor(private val featuredStory: JsonObject) : PlaylistInfoItemExtractor {
    override val uploaderName: String?
        get() {
            return featuredStory.getString("band_name")
        }
    override val uploaderUrl: String?
        get() {
            return null
        }
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val streamCount: Long
        get() {
            return featuredStory.getInt("num_streamable_tracks").toLong()
        }
    override val name: String?
        get() {
            return featuredStory.getString("album_title")
        }
    override val url: String?
        get() {
            return Utils.replaceHttpWithHttps(featuredStory.getString("item_url"))
        }

    override val thumbnails: List<Image?>?
        get() {
            return if (featuredStory.has("art_id")) BandcampExtractorHelper.getImagesFromImageId(featuredStory.getLong("art_id"), true) else BandcampExtractorHelper.getImagesFromImageId(featuredStory.getLong("item_art_id"), true)
        }
}
