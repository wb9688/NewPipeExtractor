package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType

class MediaCCCLiveStreamKioskExtractor(private val conferenceInfo: JsonObject,
                                       private val group: String,
                                       private val roomInfo: JsonObject) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return roomInfo.getObject("talks").getObject("current").getString("title")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return roomInfo.getString("link")
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getThumbnailsFromLiveStreamItem(roomInfo)
        }

    @get:Throws(ParsingException::class)
    override val streamType: StreamType
        get() {
            var isVideo: Boolean = false
            for (stream: Any in roomInfo.getArray("streams")) {
                if (("video" == (stream as JsonObject).getString("type"))) {
                    isVideo = true
                    break
                }
            }
            return if (isVideo) StreamType.LIVE_STREAM else StreamType.AUDIO_LIVE_STREAM
        }

    @get:Throws(ParsingException::class)
    override val isAd: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val duration: Long
        get() {
            return 0
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return (conferenceInfo.getString("conference") + " - " + group
                    + " - " + roomInfo.getString("display"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            return "https://media.ccc.de/c/" + conferenceInfo.getString("slug")
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return null
        }
}
