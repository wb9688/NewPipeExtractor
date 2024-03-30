package org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper

class MediaCCCConferenceInfoItemExtractor(private val conference: JsonObject) : ChannelInfoItemExtractor {
    override val description: String?
        get() {
            return ""
        }
    override val subscriberCount: Long
        get() {
            return -1
        }
    override val streamCount: Long
        get() {
            return ListExtractor.Companion.ITEM_COUNT_UNKNOWN
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return conference.getString("title")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return conference.getString("url")
        }

    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getImageListFromLogoImageUrl(conference.getString("logo_url"))
        }
}
