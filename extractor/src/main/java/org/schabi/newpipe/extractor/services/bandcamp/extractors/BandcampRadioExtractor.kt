// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
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

class BandcampRadioExtractor(streamingService: StreamingService,
                             linkHandler: ListLinkHandler?,
                             kioskId: String) : KioskExtractor<StreamInfoItem?>(streamingService, linkHandler, kioskId) {
    private var json: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        try {
            json = JsonParser.`object`().from(
                    getDownloader().get(RADIO_API_URL).responseBody())
        } catch (e: JsonParserException) {
            throw ExtractionException("Could not parse Bandcamp Radio API response", e)
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return KIOSK_RADIO
        }

    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            val radioShows: JsonArray = json!!.getArray("results")
            for (i in radioShows.indices) {
                val radioShow: JsonObject = radioShows.getObject(i)
                collector.commit(BandcampRadioInfoItemExtractor(radioShow))
            }
            return InfoItemsPage(collector, null)
        }

    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        return null
    }

    companion object {
        val KIOSK_RADIO: String = "Radio"
        val RADIO_API_URL: String = BandcampExtractorHelper.BASE_API_URL + "/bcweekly/1/list"
    }
}
