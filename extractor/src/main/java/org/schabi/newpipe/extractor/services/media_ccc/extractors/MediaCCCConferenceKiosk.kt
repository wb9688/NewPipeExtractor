package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCConferenceInfoItemExtractor
import java.io.IOException

class MediaCCCConferenceKiosk(streamingService: StreamingService,
                              linkHandler: ListLinkHandler?,
                              kioskId: String) : KioskExtractor<ChannelInfoItem?>(streamingService, linkHandler, kioskId) {
    private var doc: JsonObject? = null

    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val conferences: JsonArray = doc!!.getArray("conferences")
            val collector: ChannelInfoItemsCollector = ChannelInfoItemsCollector(getServiceId())
            for (i in conferences.indices) {
                collector.commit(MediaCCCConferenceInfoItemExtractor(conferences.getObject(i)))
            }
            return InfoItemsPage(collector, null)
        }

    public override fun getPage(page: Page?): InfoItemsPage<ChannelInfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<ChannelInfoItem?>()
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val site: String? = downloader.get(getLinkHandler().getUrl(), getExtractorLocalization())
                .responseBody()
        try {
            doc = JsonParser.`object`().from(site)
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json.", jpe)
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return doc!!.getString("Conferences")
        }

    companion object {
        val KIOSK_ID: String = "conferences"
    }
}
