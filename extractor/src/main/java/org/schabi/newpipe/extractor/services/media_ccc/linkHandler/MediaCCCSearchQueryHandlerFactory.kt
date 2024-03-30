package org.schabi.newpipe.extractor.services.media_ccc.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.UnsupportedEncodingException

class MediaCCCSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ALL,
                    CONFERENCES,
                    EVENTS
            )
        }
    override val availableSortFilter: Array<String?>
        get() {
            return arrayOfNulls(0)
        }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(query: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        try {
            return "https://media.ccc.de/public/events/search?q=" + Utils.encodeUrlUtf8(query)
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not create search string with query: " + query, e)
        }
    }

    companion object {
        val instance: MediaCCCSearchQueryHandlerFactory = MediaCCCSearchQueryHandlerFactory()
        val ALL: String = "all"
        @JvmField
        val CONFERENCES: String = "conferences"
        @JvmField
        val EVENTS: String = "events"
    }
}
