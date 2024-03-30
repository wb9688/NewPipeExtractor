// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType

class BandcampRadioInfoItemExtractor(private val show: JsonObject) : StreamInfoItemExtractor {
    override val duration: Long
        get() {
            /* Duration is only present in the more detailed information that has to be queried
        separately. Therefore, over 300 queries would be needed every time the kiosk is opened if we
        were to display the real value. */
            //return query(show.getInt("id")).getLong("audio_duration");
            return 0
        }
    override val textualUploadDate: String?
        get() {
            return show.getString("date")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return BandcampExtractorHelper.parseDate(textualUploadDate)
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return show.getString("subtitle")
        }
    override val url: String?
        get() {
            return BandcampExtractorHelper.BASE_URL + "/?show=" + show.getInt("id")
        }

    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageId(show.getLong("image_id"), false)
        }
    override val streamType: StreamType
        get() {
            return StreamType.AUDIO_STREAM
        }
    override val viewCount: Long
        get() {
            return -1
        }
    override val uploaderName: String?
        get() {
            // JSON does not contain uploader name
            return ""
        }
    override val uploaderUrl: String?
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val isAd: Boolean
        get() {
            return false
        }
}
