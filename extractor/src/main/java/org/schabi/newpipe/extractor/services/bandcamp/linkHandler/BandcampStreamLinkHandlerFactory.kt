// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import java.util.Locale

/**
 *
 * Tracks don't have standalone ids, they are always in combination with the band id.
 * That's why id = url.
 *
 *
 * Radio (bandcamp weekly) shows do have ids.
 */
class BandcampStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    /**
     * @see BandcampStreamLinkHandlerFactory
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        if (BandcampExtractorHelper.isRadioUrl(url)) {
            return url!!.split("bandcamp.com/\\?show=".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(1)
        } else {
            return getUrl(url)
        }
    }

    /**
     * Clean up url
     * @see BandcampStreamLinkHandlerFactory
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(input: String?): String? {
        if (input!!.matches("\\d+".toRegex())) {
            return BandcampExtractorHelper.BASE_URL + "/?show=" + input
        } else {
            return input
        }
    }

    /**
     * Accepts URLs that point to a bandcamp radio show or that are a bandcamp
     * domain and point to a track.
     */
    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {

        // Accept Bandcamp radio
        if (BandcampExtractorHelper.isRadioUrl(url)) {
            return true
        }

        // Don't accept URLs that don't point to a track
        if (!url!!.lowercase(Locale.getDefault()).matches("https?://.+\\..+/track/.+".toRegex())) {
            return false
        }

        // Test whether domain is supported
        return BandcampExtractorHelper.isSupportedDomain(url)
    }

    companion object {
        val instance: BandcampStreamLinkHandlerFactory = BandcampStreamLinkHandlerFactory()
    }
}
