package org.schabi.newpipe.extractor.services.soundcloud.extractors

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class SoundcloudChannelTabExtractor(service: StreamingService,
                                    linkHandler: ListLinkHandler?) : ChannelTabExtractor(service, linkHandler) {
    override val id: String?

    init {
        id = getLinkHandler().getId()
    }

    @get:Throws(ParsingException::class)
    private val endpoint: String
        private get() {
            when (getName()) {
                ChannelTabs.TRACKS -> return "/tracks"
                ChannelTabs.PLAYLISTS -> return "/playlists_without_albums"
                ChannelTabs.ALBUMS -> return "/albums"
            }
            throw ParsingException("Unsupported tab: " + getName())
        }

    public override fun onFetchPage(downloader: Downloader?) {}

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            return getPage(Page((USERS_ENDPOINT + id + endpoint + "?client_id="
                    + SoundcloudParsingHelper.clientId() + "&limit=20" + "&linked_partitioning=1")))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
        val nextPageUrl: String? = SoundcloudParsingHelper.getInfoItemsFromApi(
                collector, page.getUrl())
        return InfoItemsPage(collector, Page(nextPageUrl))
    }

    companion object {
        private val USERS_ENDPOINT: String = SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "users/"
    }
}
