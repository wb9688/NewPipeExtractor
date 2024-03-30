package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import java.io.IOException

class MediaCCCConferenceExtractor(service: StreamingService,
                                  linkHandler: ListLinkHandler?) : ChannelExtractor(service, linkHandler) {
    private var conferenceData: JsonObject? = null

    override val avatars: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getImageListFromLogoImageUrl(conferenceData!!.getString("logo_url"))
        }

    override val banners: List<Image?>?
        get() {
            return emptyList<Image>()
        }
    override val feedUrl: String?
        get() {
            return null
        }
    override val subscriberCount: Long
        get() {
            return -1
        }
    override val description: String?
        get() {
            return null
        }
    override val parentChannelName: String?
        get() {
            return ""
        }
    override val parentChannelUrl: String?
        get() {
            return ""
        }

    override val parentChannelAvatars: List<Image?>?
        get() {
            return emptyList<Image>()
        }
    override val isVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val tabs: List<ListLinkHandler>
        get() {
            // avoid keeping a reference to MediaCCCConferenceExtractor inside the lambda
            val theConferenceData: JsonObject? = conferenceData
            return java.util.List.of<ListLinkHandler>(ReadyChannelTabListLinkHandler(getUrl(), getId(), ChannelTabs.VIDEOS,
                    ChannelTabExtractorBuilder({ service: StreamingService, linkHandler: ListLinkHandler? -> MediaCCCChannelTabExtractor(service, linkHandler, theConferenceData) })))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        conferenceData = fetchConferenceData(downloader, getId())
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return conferenceData!!.getString("title")
        }

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun fetchConferenceData(downloader: Downloader?,
                                conferenceId: String?): JsonObject {
            val conferenceUrl: String = MediaCCCConferenceLinkHandlerFactory.Companion.CONFERENCE_API_ENDPOINT + conferenceId
            try {
                return JsonParser.`object`().from(downloader!!.get(conferenceUrl).responseBody())
            } catch (jpe: JsonParserException) {
                throw ExtractionException("Could not parse json returned by URL: " + conferenceUrl)
            }
        }
    }
}
