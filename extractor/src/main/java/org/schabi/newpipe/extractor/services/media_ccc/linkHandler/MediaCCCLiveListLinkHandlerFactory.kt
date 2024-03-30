package org.schabi.newpipe.extractor.services.media_ccc.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import java.util.regex.Pattern

class MediaCCCLiveListLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return "live"
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        return Pattern.matches(STREAM_PATTERN, url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        // FIXME: wrong URL; should be https://streaming.media.ccc.de/{conference_slug}/{room_slug}
        return "https://media.ccc.de/live"
    }

    companion object {
        val instance: MediaCCCLiveListLinkHandlerFactory = MediaCCCLiveListLinkHandlerFactory()
        private val STREAM_PATTERN: String = "^(?:https?://)?media\\.ccc\\.de/live$"
    }
}
