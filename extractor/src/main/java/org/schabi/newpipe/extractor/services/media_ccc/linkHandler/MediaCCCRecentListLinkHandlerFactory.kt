package org.schabi.newpipe.extractor.services.media_ccc.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import java.util.regex.Pattern

class MediaCCCRecentListLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return "recent"
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        return Pattern.matches(PATTERN, url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return "https://media.ccc.de/recent"
    }

    companion object {
        val instance: MediaCCCRecentListLinkHandlerFactory = MediaCCCRecentListLinkHandlerFactory()
        private val PATTERN: String = "^(https?://)?media\\.ccc\\.de/recent/?$"
    }
}
