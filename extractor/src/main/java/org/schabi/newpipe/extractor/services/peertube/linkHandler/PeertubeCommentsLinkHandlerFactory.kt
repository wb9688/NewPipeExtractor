package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import java.net.MalformedURLException
import java.net.URL

class PeertubeCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return PeertubeStreamLinkHandlerFactory.Companion.getInstance().getId(url) // the same id is needed
    }

    @Throws(FoundAdException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            URL(url)
            return url!!.contains("/videos/") || url.contains("/w/")
        } catch (e: MalformedURLException) {
            return false
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return getUrl(id, contentFilter, sortFilter, ServiceList.PeerTube.getBaseUrl())
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?,
                               baseUrl: String?): String? {
        return baseUrl + String.format(COMMENTS_ENDPOINT, id)
    }

    companion object {
        val instance: PeertubeCommentsLinkHandlerFactory = PeertubeCommentsLinkHandlerFactory()
        private val COMMENTS_ENDPOINT: String = "/api/v1/videos/%s/comment-threads"
    }
}
