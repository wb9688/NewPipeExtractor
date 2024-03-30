package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.JsonUtils
import java.io.IOException

class PeertubeAccountExtractor(service: StreamingService,
                               linkHandler: ListLinkHandler?) : ChannelExtractor(service, linkHandler) {
    private var json: JsonObject? = null
    private override val baseUrl: String?

    init {
        baseUrl = getBaseUrl()
    }

    @get:Nonnull
    override val avatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json)
        }

    @get:Nonnull
    override val banners: List<Image?>?
        get() {
            return PeertubeParsingHelper.getBannersFromAccountOrVideoChannelObject(baseUrl, json)
        }

    @get:Throws(ParsingException::class)
    override val feedUrl: String?
        get() {
            return getBaseUrl() + "/feeds/videos.xml?accountId=" + json!!.get("id")
        }

    @get:Throws(ParsingException::class)
    override val subscriberCount: Long
        get() {
            // The subscriber count cannot be retrieved directly. It needs to be calculated.
            // An accounts subscriber count is the number of the channel owner's subscriptions
            // plus the sum of all sub channels subscriptions.
            var subscribersCount: Long = json!!.getLong("followersCount")
            var accountVideoChannelUrl: String? = baseUrl + PeertubeChannelLinkHandlerFactory.Companion.API_ENDPOINT
            if (getId().contains(ACCOUNTS)) {
                accountVideoChannelUrl += getId()
            } else {
                accountVideoChannelUrl += ACCOUNTS + getId()
            }
            accountVideoChannelUrl += "/video-channels"
            try {
                val responseBody: String? = getDownloader().get(accountVideoChannelUrl).responseBody()
                val jsonResponse: JsonObject = JsonParser.`object`().from(responseBody)
                val videoChannels: JsonArray = jsonResponse.getArray("data")
                for (videoChannel: Any in videoChannels) {
                    val videoChannelJsonObject: JsonObject = videoChannel as JsonObject
                    subscribersCount += videoChannelJsonObject.getInt("followersCount").toLong()
                }
            } catch (ignored: IOException) {
                // something went wrong during video channels extraction,
                // only return subscribers of ownerAccount
            } catch (ignored: JsonParserException) {
            } catch (ignored: ReCaptchaException) {
            }
            return subscribersCount
        }
    override val description: String?
        get() {
            return json!!.getString("description")
        }
    override val parentChannelName: String?
        get() {
            return ""
        }
    override val parentChannelUrl: String?
        get() {
            return ""
        }

    @get:Nonnull
    override val parentChannelAvatars: List<Image?>?
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val tabs: List<ListLinkHandler>
        get() {
            return java.util.List.of<ListLinkHandler>(
                    PeertubeChannelTabLinkHandlerFactory.Companion.getInstance().fromQuery(getId(),
                            java.util.List.of<String?>(ChannelTabs.VIDEOS), "", getBaseUrl()),
                    PeertubeChannelTabLinkHandlerFactory.Companion.getInstance().fromQuery(getId(),
                            java.util.List.of<String?>(ChannelTabs.CHANNELS), "", getBaseUrl()))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val response: Response? = downloader!!.get((baseUrl
                + PeertubeChannelLinkHandlerFactory.Companion.API_ENDPOINT + getId()))
        if (response != null) {
            setInitialData(response.responseBody())
        } else {
            throw ExtractionException("Unable to extract PeerTube account data")
        }
    }

    @Throws(ExtractionException::class)
    private fun setInitialData(responseBody: String?) {
        try {
            json = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ExtractionException("Unable to extract PeerTube account data", e)
        }
        if (json == null) {
            throw ExtractionException("Unable to extract PeerTube account data")
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return JsonUtils.getString(json, "displayName")
        }

    companion object {
        private val ACCOUNTS: String = "accounts/"
    }
}
