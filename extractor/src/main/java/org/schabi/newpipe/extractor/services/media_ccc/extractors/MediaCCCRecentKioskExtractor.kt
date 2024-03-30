package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MediaCCCRecentKioskExtractor(private val event: JsonObject) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return event.getString("title")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return event.getString("frontend_link")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getImageListFromLogoImageUrl(event.getString("poster_url"))
        }

    @get:Throws(ParsingException::class)
    override val streamType: StreamType
        get() {
            return StreamType.VIDEO_STREAM
        }
    override val isAd: Boolean
        get() {
            return false
        }
    override val duration: Long
        get() {
            // duration and length have the same value, see
            // https://github.com/voc/voctoweb/blob/master/app/views/public/shared/_event.json.jbuilder
            return event.getInt("duration").toLong()
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            return event.getInt("view_count").toLong()
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return event.getString("conference_title")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            return MediaCCCConferenceLinkHandlerFactory.Companion.getInstance()
                    .fromUrl(event.getString("conference_url")) // API URL
                    .getUrl() // web URL
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            return event.getString("date")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val zonedDateTime: ZonedDateTime = ZonedDateTime.parse(event.getString("date"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSzzzz"))
            return DateWrapper(zonedDateTime.toOffsetDateTime(), false)
        }
}
