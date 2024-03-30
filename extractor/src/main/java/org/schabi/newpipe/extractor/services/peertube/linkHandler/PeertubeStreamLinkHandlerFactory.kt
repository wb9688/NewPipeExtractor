package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser
import java.net.MalformedURLException
import java.net.URL

class PeertubeStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?): String? {
        return getUrl(id, ServiceList.PeerTube.getBaseUrl())
    }

    public override fun getUrl(id: String?, baseUrl: String?): String? {
        return baseUrl + VIDEO_PATH + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return Parser.matchGroup(ID_PATTERN, url, 4)
    }

    @Throws(FoundAdException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        if (url!!.contains("/playlist/")) {
            return false
        }
        try {
            URL(url)
            getId(url)
            return true
        } catch (e: ParsingException) {
            return false
        } catch (e: MalformedURLException) {
            return false
        }
    }

    companion object {
        val instance: PeertubeStreamLinkHandlerFactory = PeertubeStreamLinkHandlerFactory()
        private val ID_PATTERN: String = "(/w/|(/videos/(watch/|embed/)?))(?!p/)([^/?&#]*)"

        // we exclude p/ because /w/p/ is playlist, not video
        val VIDEO_API_ENDPOINT: String = "/api/v1/videos/"

        // From PeerTube 3.3.0, the default path is /w/.
        // We still use /videos/watch/ for compatibility reasons:
        // /videos/watch/ is still accepted by >=3.3.0 but /w/ isn't by <3.3.0
        private val VIDEO_PATH: String = "/videos/watch/"
    }
}
