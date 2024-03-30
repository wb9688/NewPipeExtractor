package org.schabi.newpipe.extractor.services.soundcloud.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser
import java.util.Locale

class SoundcloudChartsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        if (Parser.isMatch(TOP_URL_PATTERN, url!!.lowercase(Locale.getDefault()))) {
            return "Top 50"
        } else {
            return "New & hot"
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        if ((id == "Top 50")) {
            return "https://soundcloud.com/charts/top"
        } else {
            return "https://soundcloud.com/charts/new"
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        return Parser.isMatch(URL_PATTERN, url!!.lowercase(Locale.getDefault()))
    }

    companion object {
        val instance: SoundcloudChartsLinkHandlerFactory = SoundcloudChartsLinkHandlerFactory()
        private val TOP_URL_PATTERN: String = "^https?://(www\\.|m\\.)?soundcloud.com/charts(/top)?/?([#?].*)?$"
        private val URL_PATTERN: String = "^https?://(www\\.|m\\.)?soundcloud.com/charts(/top|/new)?/?([#?].*)?$"
    }
}
