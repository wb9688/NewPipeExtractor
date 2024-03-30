package org.schabi.newpipe.extractor.services.media_ccc.extractors

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
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import java.io.IOException
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

class MediaCCCRecentKiosk(streamingService: StreamingService,
                          linkHandler: ListLinkHandler?,
                          kioskId: String) : KioskExtractor<StreamInfoItem?>(streamingService, linkHandler, kioskId) {
    private var doc: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val site: String? = downloader.get("https://api.media.ccc.de/public/events/recent",
                getExtractorLocalization()).responseBody()
        try {
            doc = JsonParser.`object`().from(site)
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json.", jpe)
        }
    }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val events: JsonArray = doc!!.getArray("events")

            // Streams in the recent kiosk are not ordered by the release date.
            // Sort them to have the latest stream at the beginning of the list.
            val comparator: Comparator<StreamInfoItem> = Comparator
                    .comparing(Function({ obj: StreamInfoItem -> obj.getUploadDate() }), Comparator
                            .nullsLast(Comparator.comparing(Function({ obj: DateWrapper? -> obj!!.offsetDateTime() }))))
                    .reversed()
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId(),
                    comparator)
            events.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ event: JsonObject -> MediaCCCRecentKioskExtractor(event) })) // #813 / voc/voctoweb#609 -> returns faulty data -> filter it out
                    .filter(Predicate({ extractor: MediaCCCRecentKioskExtractor -> extractor.getDuration() > 0 }))
                    .forEach(Consumer({ extractor: MediaCCCRecentKioskExtractor -> collector.commit(extractor) }))
            return InfoItemsPage(collector, null)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<StreamInfoItem?>()
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return KIOSK_ID
        }

    companion object {
        val KIOSK_ID: String = "recent"
    }
}
