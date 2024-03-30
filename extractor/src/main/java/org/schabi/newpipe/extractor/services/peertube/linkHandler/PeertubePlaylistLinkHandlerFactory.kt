package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser
import java.net.MalformedURLException
import java.net.URL

class PeertubePlaylistLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?): String? {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.getBaseUrl())
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?,
                               baseUrl: String?): String? {
        return baseUrl + "/api/v1/video-playlists/" + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        try {
            return Parser.matchGroup(ID_PATTERN, url, 2)
        } catch (ignored: ParsingException) {
            // might also be an API url, no reason to throw an exception here
        }
        return Parser.matchGroup1(API_ID_PATTERN, url)
    }

    public override fun onAcceptUrl(url: String?): Boolean {
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
        val instance: PeertubePlaylistLinkHandlerFactory = PeertubePlaylistLinkHandlerFactory()
        private val ID_PATTERN: String = "(/videos/watch/playlist/|/w/p/)([^/?&#]*)"
        private val API_ID_PATTERN: String = "/video-playlists/([^/?&#]*)"
    }
}
