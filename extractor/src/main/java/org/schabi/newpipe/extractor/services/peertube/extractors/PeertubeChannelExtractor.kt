package org.schabi.newpipe.extractor.services.peertube.extractors

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
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.JsonUtils
import java.io.IOException

class PeertubeChannelExtractor(service: StreamingService,
                               linkHandler: ListLinkHandler?) : ChannelExtractor(service, linkHandler) {
    private var json: JsonObject? = null
    private override val baseUrl: String?

    init {
        baseUrl = getBaseUrl()
    }

    override val avatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json)
        }

    override val banners: List<Image?>?
        get() {
            return PeertubeParsingHelper.getBannersFromAccountOrVideoChannelObject(baseUrl, json)
        }

    @get:Throws(ParsingException::class)
    override val feedUrl: String?
        get() {
            return getBaseUrl() + "/feeds/videos.xml?videoChannelId=" + json!!.get("id")
        }
    override val subscriberCount: Long
        get() {
            return json!!.getLong("followersCount")
        }
    override val description: String?
        get() {
            return json!!.getString("description")
        }

    @get:Throws(ParsingException::class)
    override val parentChannelName: String?
        get() {
            return JsonUtils.getString(json, "ownerAccount.name")
        }

    @get:Throws(ParsingException::class)
    override val parentChannelUrl: String?
        get() {
            return JsonUtils.getString(json, "ownerAccount.url")
        }

    override val parentChannelAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(
                    baseUrl, json!!.getObject("ownerAccount"))
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val tabs: List<ListLinkHandler>
        get() {
            return java.util.List.of<ListLinkHandler>(
                    PeertubeChannelTabLinkHandlerFactory.Companion.getInstance().fromQuery(getId(),
                            java.util.List.of<String?>(ChannelTabs.VIDEOS), "", getBaseUrl()),
                    PeertubeChannelTabLinkHandlerFactory.Companion.getInstance().fromQuery(getId(),
                            java.util.List.of<String?>(ChannelTabs.PLAYLISTS), "", getBaseUrl()))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val response: Response? = downloader!!.get(
                baseUrl + PeertubeChannelLinkHandlerFactory.Companion.API_ENDPOINT + getId())
        if (response != null) {
            setInitialData(response.responseBody())
        } else {
            throw ExtractionException("Unable to extract PeerTube channel data")
        }
    }

    @Throws(ExtractionException::class)
    private fun setInitialData(responseBody: String?) {
        try {
            json = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ExtractionException("Unable to extract PeerTube channel data", e)
        }
        if (json == null) {
            throw ExtractionException("Unable to extract PeerTube channel data")
        }
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return JsonUtils.getString(json, "displayName")
        }
}
