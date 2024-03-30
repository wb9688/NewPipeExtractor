package org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType

class MediaCCCStreamInfoItemExtractor(private val event: JsonObject) : StreamInfoItemExtractor {
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
            return event.getInt("length").toLong()
        }
    override val viewCount: Long
        get() {
            return event.getInt("view_count").toLong()
        }
    override val uploaderName: String?
        get() {
            return event.getString("conference_title")
        }
    override val uploaderUrl: String?
        get() {
            return event.getString("conference_url")
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val textualUploadDate: String?
        get() {
            return event.getString("release_date")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val date: String? = textualUploadDate
            if (date == null) {
                return null // event is in the future...
            }
            return DateWrapper(MediaCCCParsingHelper.parseDateFrom(date))
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return event.getString("title")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return ("https://media.ccc.de/public/events/"
                    + event.getString("guid"))
        }

    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getThumbnailsFromStreamItem(event)
        }
}
