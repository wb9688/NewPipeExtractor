package org.schabi.newpipe.extractor.services.soundcloud.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Utils
import java.util.Locale

class SoundcloudStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?): String? {
        try {
            return SoundcloudParsingHelper.resolveUrlWithEmbedPlayer(
                    "https://api.soundcloud.com/tracks/" + id)
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        if (Parser.isMatch(API_URL_PATTERN, url)) {
            return Parser.matchGroup1(API_URL_PATTERN, url)
        }
        Utils.checkUrl(URL_PATTERN, url)
        try {
            return SoundcloudParsingHelper.resolveIdWithWidgetApi(url)
        } catch (e: Exception) {
            throw ParsingException(e.message, e)
        }
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        return Parser.isMatch(URL_PATTERN, url!!.lowercase(Locale.getDefault()))
    }

    companion object {
        val instance: SoundcloudStreamLinkHandlerFactory = SoundcloudStreamLinkHandlerFactory()
        private val URL_PATTERN: String = ("^https?://(www\\.|m\\.)?soundcloud.com/[0-9a-z_-]+"
                + "/(?!(tracks|albums|sets|reposts|followers|following)/?$)[0-9a-z_-]+/?([#?].*)?$")
        private val API_URL_PATTERN: String = ("^https?://api-v2\\.soundcloud.com"
                + "/(tracks|albums|sets|reposts|followers|following)/([0-9a-z_-]+)/")
    }
}
