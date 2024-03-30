package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
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
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampDiscographStreamInfoItemExtractor
import java.io.IOException

class BandcampChannelTabExtractor(service: StreamingService,
                                  linkHandler: ListLinkHandler?) : ChannelTabExtractor(service, linkHandler) {
    private var discography: JsonArray? = null
    private var filter: String? = null

    init {
        val tab: String? = linkHandler.getContentFilters().get(0)
        when (tab) {
            ChannelTabs.TRACKS -> filter = "track"
            ChannelTabs.ALBUMS -> filter = "album"
            else -> throw IllegalArgumentException("Unsupported channel tab: " + tab)
        }
    }

    @Throws(ParsingException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        if (discography == null) {
            discography = BandcampExtractorHelper.getArtistDetails(getId())
                    .getArray("discography")
        }
    }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            for (discograph: Any? in discography!!) {
                // A discograph is as an item appears in a discography
                if (!(discograph is JsonObject)) {
                    continue
                }
                val discographJsonObject: JsonObject = discograph
                val itemType: String = discographJsonObject.getString("item_type", "")
                if (!(itemType == filter)) {
                    continue
                }
                when (itemType) {
                    "track" -> collector.commit(BandcampDiscographStreamInfoItemExtractor(
                            discographJsonObject, getUrl()))

                    "album" -> collector.commit(BandcampAlbumInfoItemExtractor(
                            discographJsonObject, getUrl()))
                }
            }
            return InfoItemsPage(collector, null)
        }

    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        return null
    }

    companion object {
        fun fromDiscography(service: StreamingService,
                            linkHandler: ListLinkHandler?,
                            discography: JsonArray?): BandcampChannelTabExtractor {
            val tabExtractor: BandcampChannelTabExtractor = BandcampChannelTabExtractor(service, linkHandler)
            tabExtractor.discography = discography
            return tabExtractor
        }
    }
}
