package org.schabi.newpipe.extractor.services.soundcloud.extractors

import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionItem
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

/**
 * Extract the "followings" from a user in SoundCloud.
 */
class SoundcloudSubscriptionExtractor(service: SoundcloudService) : SubscriptionExtractor(service, listOf(ContentSource.CHANNEL_URL)) {
    override val relatedUrl: String?
        get() {
            return "https://soundcloud.com/you"
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun fromChannelUrl(channelUrl: String?): List<SubscriptionItem> {
        if (channelUrl == null) {
            throw InvalidSourceException("Channel url is null")
        }
        val id: String?
        try {
            id = service.getChannelLHFactory().fromUrl(getUrlFrom(channelUrl)).getId()
        } catch (e: ExtractionException) {
            throw InvalidSourceException(e)
        }
        val apiUrl: String = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "users/" + id + "/followings" + "?client_id="
                + SoundcloudParsingHelper.clientId() + "&limit=200")
        val collector: ChannelInfoItemsCollector = ChannelInfoItemsCollector(service
                .getServiceId())
        // ± 2000 is the limit of followings on SoundCloud, so this minimum should be enough
        SoundcloudParsingHelper.getUsersFromApiMinItems(2500, collector, apiUrl)
        return toSubscriptionItems(collector.getItems())
    }

    private fun getUrlFrom(channelUrl: String): String {
        val fixedUrl: String? = Utils.replaceHttpWithHttps(channelUrl)
        if (fixedUrl!!.startsWith(Utils.HTTPS)) {
            return channelUrl
        } else if (!fixedUrl.contains("soundcloud.com/")) {
            return "https://soundcloud.com/" + fixedUrl
        } else {
            return Utils.HTTPS + fixedUrl
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////// */
    private fun toSubscriptionItems(items: List<ChannelInfoItem?>?): List<SubscriptionItem> {
        val result: MutableList<SubscriptionItem> = ArrayList(items!!.size)
        for (item: ChannelInfoItem? in items) {
            result.add(SubscriptionItem(item.getServiceId(), item.getUrl(), item.getName()))
        }
        return result
    }
}
