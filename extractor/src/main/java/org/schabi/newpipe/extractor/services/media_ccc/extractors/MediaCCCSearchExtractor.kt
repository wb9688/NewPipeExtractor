package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory
import java.io.IOException
import java.util.Locale

class MediaCCCSearchExtractor(service: StreamingService,
                              linkHandler: SearchQueryHandler?) : SearchExtractor(service, linkHandler) {
    private var doc: JsonObject? = null
    private var conferenceKiosk: MediaCCCConferenceKiosk? = null

    init {
        try {
            conferenceKiosk = MediaCCCConferenceKiosk(service,
                    MediaCCCConferencesListLinkHandlerFactory.Companion.getInstance()
                            .fromId("conferences"),
                    "conferences")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override val searchSuggestion: String?
        get() {
            return ""
        }
    override val isCorrectedSearch: Boolean
        get() {
            return false
        }

    override val metaInfo: List<MetaInfo?>?
        get() {
            return emptyList<MetaInfo>()
        }

    override val initialPage: InfoItemsPage<R?>?
        get() {
            val searchItems: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            if ((getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.CONFERENCES)
                            || getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.ALL)
                            || getLinkHandler().getContentFilters().isEmpty())) {
                searchConferences(getSearchString(),
                        conferenceKiosk.getInitialPage().getItems(),
                        searchItems)
            }
            if ((getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.EVENTS)
                            || getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.ALL)
                            || getLinkHandler().getContentFilters().isEmpty())) {
                val events: JsonArray = doc!!.getArray("events")
                for (i in events.indices) {
                    // Ensure only uploaded talks are shown in the search results.
                    // If the release date is null, the talk has not been held or uploaded yet
                    // and no streams are going to be available anyway.
                    if (events.getObject(i).getString("release_date") != null) {
                        searchItems.commit(MediaCCCStreamInfoItemExtractor(
                                events.getObject(i)))
                    }
                }
            }
            return InfoItemsPage(searchItems, null)
        }

    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<InfoItem?>()
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        if ((getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.EVENTS)
                        || getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.ALL)
                        || getLinkHandler().getContentFilters().isEmpty())) {
            val site: String?
            val url: String? = getUrl()
            site = downloader.get(url, getExtractorLocalization()).responseBody()
            try {
                doc = JsonParser.`object`().from(site)
            } catch (jpe: JsonParserException) {
                throw ExtractionException("Could not parse JSON.", jpe)
            }
        }
        if ((getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.CONFERENCES)
                        || getLinkHandler().getContentFilters().contains(MediaCCCSearchQueryHandlerFactory.Companion.ALL)
                        || getLinkHandler().getContentFilters().isEmpty())) {
            conferenceKiosk!!.fetchPage()
        }
    }

    private fun searchConferences(searchString: String?,
                                  channelItems: List<ChannelInfoItem?>?,
                                  collector: MultiInfoItemsCollector) {
        for (item: ChannelInfoItem? in channelItems!!) {
            if (item.getName().uppercase(Locale.getDefault()).contains(
                            searchString!!.uppercase(Locale.getDefault()))) {
                collector.commit(object : ChannelInfoItemExtractor {
                    override val description: String?
                        get() {
                            return item.getDescription()
                        }
                    override val subscriberCount: Long
                        get() {
                            return item.getSubscriberCount()
                        }
                    override val streamCount: Long
                        get() {
                            return item.getStreamCount()
                        }
                    override val isVerified: Boolean
                        get() {
                            return false
                        }
                    override val name: String?
                        get() {
                            return item.getName()
                        }
                    override val url: String?
                        get() {
                            return item.getUrl()
                        }

                    override val thumbnails: List<Image?>?
                        get() {
                            return item.getThumbnails()
                        }
                })
            }
        }
    }
}
