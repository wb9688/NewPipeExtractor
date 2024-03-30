package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.utils.Utils

class SoundcloudChannelInfoItemExtractor(private val itemObject: JsonObject) : ChannelInfoItemExtractor {
    override val name: String?
        get() {
            return itemObject.getString("username")
        }
    override val url: String?
        get() {
            return Utils.replaceHttpWithHttps(itemObject.getString("permalink_url"))
        }

    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(itemObject.getString("avatar_url"))
        }
    override val subscriberCount: Long
        get() {
            return itemObject.getLong("followers_count")
        }
    override val streamCount: Long
        get() {
            return itemObject.getLong("track_count")
        }
    override val isVerified: Boolean
        get() {
            return itemObject.getBoolean("verified")
        }
    override val description: String?
        get() {
            return itemObject.getString("description", "")
        }
}
