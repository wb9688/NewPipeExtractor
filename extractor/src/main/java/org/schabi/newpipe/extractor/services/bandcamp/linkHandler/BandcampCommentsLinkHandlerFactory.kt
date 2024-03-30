package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import java.util.Locale

/**
 * Like in [BandcampStreamLinkHandlerFactory], tracks have no meaningful IDs except for
 * their URLs
 */
class BandcampCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return url
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        if (BandcampExtractorHelper.isRadioUrl(url)) {
            return true
        }

        // Don't accept URLs that don't point to a track
        if (!url!!.lowercase(Locale.getDefault()).matches("https?://.+\\..+/(track|album)/.+".toRegex())) {
            return false
        }

        // Test whether domain is supported
        return BandcampExtractorHelper.isSupportedDomain(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return id
    }

    companion object {
        val instance: BandcampCommentsLinkHandlerFactory = BandcampCommentsLinkHandlerFactory()
    }
}
