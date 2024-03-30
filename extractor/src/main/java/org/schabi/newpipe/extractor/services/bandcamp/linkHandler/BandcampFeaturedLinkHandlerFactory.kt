// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor
import org.schabi.newpipe.extractor.utils.Utils

class BandcampFeaturedLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        if ((id == BandcampFeaturedExtractor.Companion.KIOSK_FEATURED)) {
            return BandcampFeaturedExtractor.Companion.FEATURED_API_URL // doesn't have a website
        } else if ((id == BandcampRadioExtractor.Companion.KIOSK_RADIO)) {
            return BandcampRadioExtractor.Companion.RADIO_API_URL // doesn't have its own website
        } else {
            return null
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        val fixedUrl: String? = Utils.replaceHttpWithHttps(url)
        if (BandcampExtractorHelper.isRadioUrl(fixedUrl) || (fixedUrl == BandcampRadioExtractor.Companion.RADIO_API_URL)) {
            return BandcampRadioExtractor.Companion.KIOSK_RADIO
        } else if ((fixedUrl == BandcampFeaturedExtractor.Companion.FEATURED_API_URL)) {
            return BandcampFeaturedExtractor.Companion.KIOSK_FEATURED
        } else {
            return null
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        val fixedUrl: String? = Utils.replaceHttpWithHttps(url)
        return ((fixedUrl == BandcampFeaturedExtractor.Companion.FEATURED_API_URL) || (fixedUrl == BandcampRadioExtractor.Companion.RADIO_API_URL) || BandcampExtractorHelper.isRadioUrl(fixedUrl))
    }

    companion object {
        val instance: BandcampFeaturedLinkHandlerFactory = BandcampFeaturedLinkHandlerFactory()
    }
}
