package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class PeertubePlaylistExtractor(service: StreamingService,
                                linkHandler: ListLinkHandler?) : PlaylistExtractor(service, linkHandler) {
    private var playlistInfo: JsonObject? = null

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem(getBaseUrl(), playlistInfo)
        }
    override val uploaderUrl: String?
        get() {
            return playlistInfo!!.getObject("ownerAccount").getString("url")
        }
    override val uploaderName: String?
        get() {
            return playlistInfo!!.getObject("ownerAccount").getString("displayName")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(getBaseUrl(),
                    playlistInfo!!.getObject("ownerAccount"))
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val streamCount: Long
        get() {
            return playlistInfo!!.getLong("videosLength")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val description: Description
        get() {
            val description: String = playlistInfo!!.getString("description")
            if (Utils.isNullOrEmpty(description)) {
                return Description.Companion.EMPTY_DESCRIPTION
            }
            return Description(description, Description.Companion.PLAIN_TEXT)
        }

    @get:Nonnull
    override val subChannelName: String?
        get() {
            return playlistInfo!!.getObject("videoChannel").getString("displayName")
        }

    @get:Nonnull
    override val subChannelUrl: String?
        get() {
            return playlistInfo!!.getObject("videoChannel").getString("url")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val subChannelAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(getBaseUrl(),
                    playlistInfo!!.getObject("videoChannel"))
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            return getPage(Page((getUrl() + "/videos?" + PeertubeParsingHelper.START_KEY + "=0&"
                    + PeertubeParsingHelper.COUNT_KEY + "=" + PeertubeParsingHelper.ITEMS_PER_PAGE)))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val response: Response? = getDownloader().get(page.getUrl())
        var json: JsonObject? = null
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.`object`().from(response.responseBody())
            } catch (e: Exception) {
                throw ParsingException("Could not parse json data for playlist info", e)
            }
        }
        if (json != null) {
            PeertubeParsingHelper.validate(json)
            val total: Long = json.getLong("total")
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            collectItemsFrom(collector, json, getBaseUrl())
            return InfoItemsPage(collector,
                    PeertubeParsingHelper.getNextPage(page.getUrl(), total))
        } else {
            throw ExtractionException("Unable to get PeerTube playlist info")
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val response: Response? = downloader!!.get(getUrl())
        try {
            playlistInfo = JsonParser.`object`().from(response!!.responseBody())
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json", jpe)
        }
        PeertubeParsingHelper.validate(playlistInfo)
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return playlistInfo!!.getString("displayName")
        }
}
