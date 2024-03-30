package org.schabi.newpipe.extractor.services.media_ccc.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

class MediaCCCConferencesListLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return "conferences"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return "https://media.ccc.de/public/conferences"
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        return ((url == "https://media.ccc.de/b/conferences") || (url == "https://media.ccc.de/public/conferences") || (url == "https://api.media.ccc.de/public/conferences"))
    }

    companion object {
        val instance: MediaCCCConferencesListLinkHandlerFactory = MediaCCCConferencesListLinkHandlerFactory()
    }
}
