package org.schabi.newpipe.extractor.services.soundcloud.linkHandler

import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import java.io.IOException

class SoundcloudCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        try {
            return ("https://api-v2.soundcloud.com/tracks/" + id + "/comments" + "?client_id="
                    + SoundcloudParsingHelper.clientId() + "&threaded=0" + "&filter_replies=1")
            // Anything but 1 = sort by new
            // + "&limit=NUMBER_OF_ITEMS_PER_REQUEST". We let the API control (default = 10)
            // + "&offset=OFFSET". We let the API control (default = 0, then we use nextPageUrl)
        } catch (e: ExtractionException) {
            throw ParsingException("Could not get comments")
        } catch (e: IOException) {
            throw ParsingException("Could not get comments")
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        // Delegation to avoid duplicate code, as we need the same id
        return SoundcloudStreamLinkHandlerFactory.Companion.getInstance().getId(url)
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            getId(url)
            return true
        } catch (e: ParsingException) {
            return false
        }
    }

    companion object {
        val instance: SoundcloudCommentsLinkHandlerFactory = SoundcloudCommentsLinkHandlerFactory()
    }
}
