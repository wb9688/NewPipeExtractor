package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper

class BandcampDiscographStreamInfoItemExtractor(private val discograph: JsonObject,
                                                uploaderUrl: String?) : BandcampStreamInfoItemExtractor(uploaderUrl) {
    override val uploaderName: String?
        get() {
            return discograph.getString("band_name")
        }
    override val name: String?
        get() {
            return discograph.getString("title")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return BandcampExtractorHelper.getStreamUrlFromIds(
                    discograph.getLong("band_id"),
                    discograph.getLong("item_id"),
                    discograph.getString("item_type")
            )
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageId(discograph.getLong("art_id"), true)
        }
    override val duration: Long
        get() {
            return -1
        }
}
