package org.schabi.newpipe.extractor.services.soundcloud.extractors

import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class SoundcloudChartsExtractor(service: StreamingService,
                                linkHandler: ListLinkHandler?,
                                kioskId: String) : KioskExtractor<StreamInfoItem?>(service, linkHandler, kioskId) {
    public override fun onFetchPage(downloader: Downloader?) {}

    @get:Nonnull
    override val name: String?
        get() {
            return getId()
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
        val nextPageUrl: String? = SoundcloudParsingHelper.getStreamsFromApi(collector,
                page.getUrl(), true)
        return InfoItemsPage(collector, Page(nextPageUrl))
    }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            var apiUrl: String = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "charts" + "?genre=soundcloud:genres:all-music"
                    + "&client_id=" + SoundcloudParsingHelper.clientId())
            if ((getId() == "Top 50")) {
                apiUrl += "&kind=top"
            } else {
                apiUrl += "&kind=trending"
            }
            val contentCountry: ContentCountry? = ServiceList.SoundCloud.getContentCountry()
            var apiUrlWithRegion: String? = null
            if (getService().getSupportedCountries().contains(contentCountry)) {
                apiUrlWithRegion = (apiUrl + "&region=soundcloud:regions:"
                        + contentCountry.getCountryCode())
            }
            var nextPageUrl: String?
            try {
                nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector,
                        if (apiUrlWithRegion == null) apiUrl else apiUrlWithRegion, true)
            } catch (e: IOException) {
                // Request to other region may be geo-restricted.
                // See https://github.com/TeamNewPipe/NewPipeExtractor/issues/537.
                // We retry without the specified region.
                nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl, true)
            }
            return InfoItemsPage(collector, Page(nextPageUrl))
        }
}
