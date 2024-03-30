package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import java.io.IOException

class MediaCCCLiveStreamKiosk(streamingService: StreamingService,
                              linkHandler: ListLinkHandler?,
                              kioskId: String) : KioskExtractor<StreamInfoItem?>(streamingService, linkHandler, kioskId) {
    private var doc: JsonArray? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        doc = MediaCCCParsingHelper.getLiveStreams(downloader, getExtractorLocalization())
    }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            for (c in doc!!.indices) {
                val conference: JsonObject = doc!!.getObject(c)
                if (conference.getBoolean("isCurrentlyStreaming")) {
                    val groups: JsonArray = conference.getArray("groups")
                    for (g in groups.indices) {
                        val group: String = groups.getObject(g).getString("group")
                        val rooms: JsonArray = groups.getObject(g).getArray("rooms")
                        for (r in rooms.indices) {
                            val room: JsonObject = rooms.getObject(r)
                            collector.commit(MediaCCCLiveStreamKioskExtractor(
                                    conference, group, room))
                        }
                    }
                }
            }
            return InfoItemsPage(collector, null)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<StreamInfoItem?>()
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return KIOSK_ID
        }

    companion object {
        val KIOSK_ID: String = "live"
    }
}
