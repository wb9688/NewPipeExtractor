package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper

class PeertubeChannelInfoItemExtractor(@param:Nonnull private val item: JsonObject,
                                       @param:Nonnull private val baseUrl: String?) : ChannelInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return item.getString("displayName")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return item.getString("url")
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item)
        }

    @get:Throws(ParsingException::class)
    override val description: String?
        get() {
            return item.getString("description")
        }

    @get:Throws(ParsingException::class)
    override val subscriberCount: Long
        get() {
            return item.getInt("followersCount").toLong()
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            return ListExtractor.Companion.ITEM_COUNT_UNKNOWN
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return false
        }
}
