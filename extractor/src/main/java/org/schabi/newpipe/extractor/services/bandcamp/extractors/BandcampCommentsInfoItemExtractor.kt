package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.stream.Description

class BandcampCommentsInfoItemExtractor(private val review: JsonObject, override val url: String?) : CommentsInfoItemExtractor {

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return commentText.getContent()
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return uploaderAvatars
        }

    @get:Throws(ParsingException::class)
    override val commentText: Description
        get() {
            return Description(review.getString("why"), Description.Companion.PLAIN_TEXT)
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return review.getString("name")
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageId(review.getLong("image_id"), false)
        }
}
