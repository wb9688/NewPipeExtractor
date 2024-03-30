package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor
import java.io.IOException
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

/**
 * MediaCCC does not really have channel tabs, but rather a list of videos for each conference,
 * so this class just acts as a videos channel tab extractor.
 */
class MediaCCCChannelTabExtractor
/**
 * @param conferenceData will be not-null if conference data has already been fetched by
 * [MediaCCCConferenceExtractor]. Otherwise, if this parameter is
 * `null`, conference data will be fetched anew.
 */(service: StreamingService,
    linkHandler: ListLinkHandler?,
    private var conferenceData: JsonObject?) : ChannelTabExtractor(service, linkHandler) {
    @Throws(ExtractionException::class, IOException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        if (conferenceData == null) {
            // only fetch conference data if we don't have it already
            conferenceData = MediaCCCConferenceExtractor.Companion.fetchConferenceData(downloader, getId())
        }
    }

    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
            Objects.requireNonNull(conferenceData) // will surely be != null after onFetchPage
                    .getArray("events")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .forEach(Consumer({ event: JsonObject -> collector.commit(MediaCCCStreamInfoItemExtractor(event)) }))
            return InfoItemsPage(collector, null)
        }

    public override fun getPage(page: Page?): InfoItemsPage<InfoItem?>? {
        return InfoItemsPage.Companion.emptyPage<InfoItem?>()
    }
}
