// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import java.util.Locale

/**
 * Just as with streams, the album ids are essentially useless for us.
 */
class BandcampPlaylistLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return getUrl(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(url: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return url
    }

    /**
     * Accepts all bandcamp URLs that contain /album/ behind their domain name.
     */
    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {

        // Exclude URLs which do not lead to an album
        if (!url!!.lowercase(Locale.getDefault()).matches("https?://.+\\..+/album/.+".toRegex())) {
            return false
        }

        // Test whether domain is supported
        return BandcampExtractorHelper.isSupportedDomain(url)
    }

    companion object {
        val instance: BandcampPlaylistLinkHandlerFactory = BandcampPlaylistLinkHandlerFactory()
    }
}
