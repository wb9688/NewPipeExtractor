package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.utils.JsonUtils

open class PeertubeStreamInfoItemExtractor(protected val item: JsonObject, private var baseUrl: String?) : StreamInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            val uuid: String? = JsonUtils.getString(item, "uuid")
            return ServiceList.PeerTube.getStreamLHFactory().fromId(uuid, baseUrl).getUrl()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem(baseUrl, item)
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return JsonUtils.getString(item, "name")
        }
    override val isAd: Boolean
        get() {
            return false
        }
    override val viewCount: Long
        get() {
            return item.getLong("views")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            val name: String? = JsonUtils.getString(item, "account.name")
            val host: String? = JsonUtils.getString(item, "account.host")
            return ServiceList.PeerTube.getChannelLHFactory()
                    .fromId("accounts/" + name + "@" + host, baseUrl).getUrl()
        }

    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item.getObject("account"))
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return JsonUtils.getString(item, "account.displayName")
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            return JsonUtils.getString(item, "publishedAt")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualUploadDate: String? = textualUploadDate
            if (textualUploadDate == null) {
                return null
            }
            return DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate))
        }
    override val streamType: StreamType
        get() {
            return if (item.getBoolean("isLive")) StreamType.LIVE_STREAM else StreamType.VIDEO_STREAM
        }
    override val duration: Long
        get() {
            return item.getLong("duration")
        }

    protected fun setBaseUrl(baseUrl: String?) {
        this.baseUrl = baseUrl
    }
}
