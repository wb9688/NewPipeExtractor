package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import java.util.Objects

class SoundcloudCommentsInfoItemExtractor(private val json: JsonObject, override val url: String?) : CommentsInfoItemExtractor {

    override val commentId: String?
        get() {
            return Objects.toString(json.getLong("id"), null)
        }

    override val commentText: Description
        get() {
            return Description(json.getString("body"), Description.Companion.PLAIN_TEXT)
        }
    override val uploaderName: String?
        get() {
            return json.getObject("user").getString("username")
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(json.getObject("user").getString("avatar_url"))
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return json.getObject("user").getBoolean("verified")
        }
    override val streamPosition: Int
        get() {
            return json.getInt("timestamp") / 1000 // convert milliseconds to seconds
        }
    override val uploaderUrl: String?
        get() {
            return json.getObject("user").getString("permalink_url")
        }
    override val textualUploadDate: String?
        get() {
            return json.getString("created_at")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return DateWrapper(SoundcloudParsingHelper.parseDateFrom(textualUploadDate))
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return json.getObject("user").getString("permalink")
        }

    override val thumbnails: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(json.getObject("user").getString("avatar_url"))
        }
}
