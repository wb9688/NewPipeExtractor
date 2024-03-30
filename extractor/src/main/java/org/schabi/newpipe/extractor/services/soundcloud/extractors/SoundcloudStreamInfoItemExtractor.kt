package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.utils.Utils

class SoundcloudStreamInfoItemExtractor(private val itemObject: JsonObject?) : StreamInfoItemExtractor {
    override val url: String?
        get() {
            return Utils.replaceHttpWithHttps(itemObject!!.getString("permalink_url"))
        }
    override val name: String?
        get() {
            return itemObject!!.getString("title")
        }
    override val duration: Long
        get() {
            return itemObject!!.getLong("duration") / 1000L
        }
    override val uploaderName: String?
        get() {
            return itemObject!!.getObject("user").getString("username")
        }
    override val uploaderUrl: String?
        get() {
            return Utils.replaceHttpWithHttps(itemObject!!.getObject("user").getString("permalink_url"))
        }

    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(
                    itemObject!!.getObject("user").getString("avatar_url"))
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return itemObject!!.getObject("user").getBoolean("verified")
        }
    override val textualUploadDate: String?
        get() {
            return itemObject!!.getString("created_at")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return DateWrapper(SoundcloudParsingHelper.parseDateFrom(textualUploadDate))
        }
    override val viewCount: Long
        get() {
            return itemObject!!.getLong("playback_count")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromTrackObject(itemObject)
        }
    override val streamType: StreamType
        get() {
            return StreamType.AUDIO_STREAM
        }
    override val isAd: Boolean
        get() {
            return false
        }
}
