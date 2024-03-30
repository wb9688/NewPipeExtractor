package org.schabi.newpipe.extractor.services.soundcloud.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Utils
import java.util.Locale

class SoundcloudChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        Utils.checkUrl(URL_PATTERN, url)
        try {
            return SoundcloudParsingHelper.resolveIdWithWidgetApi(url)
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer(
                    "https://api.soundcloud.com/users/" + id)
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        return Parser.isMatch(URL_PATTERN, url!!.lowercase(Locale.getDefault()))
    }

    companion object {
        val instance: SoundcloudChannelLinkHandlerFactory = SoundcloudChannelLinkHandlerFactory()
        private val URL_PATTERN: String = ("^https?://(www\\.|m\\.)?soundcloud.com/[0-9a-z_-]+"
                + "(/((tracks|albums|sets|reposts|followers|following)/?)?)?([#?].*)?$")
    }
}
