package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser
import java.net.MalformedURLException
import java.net.URL

class PeertubeChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return fixId(Parser.matchGroup(ID_PATTERN, url, 0))
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               searchFilter: String?): String? {
        return getUrl(id, contentFilters, searchFilter, ServiceList.PeerTube.getBaseUrl())
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?,
                               baseUrl: String?): String? {
        if (id!!.matches(ID_PATTERN.toRegex())) {
            return baseUrl + "/" + fixId(id)
        } else {
            // This is needed for compatibility with older versions were we didn't support
            // video channels yet
            return baseUrl + "/accounts/" + id
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            URL(url)
            return (url!!.contains("/accounts/") || url.contains("/a/")
                    || url.contains("/video-channels/") || url.contains("/c/"))
        } catch (e: MalformedURLException) {
            return false
        }
    }

    /**
     * Fix id
     *
     *
     *
     * a/:accountName and c/:channelName ids are supported
     * by the PeerTube web client (>= v3.3.0)
     * but not by the API.
     *
     *
     * @param id the id to fix
     * @return the fixed id
     */
    private fun fixId(id: String?): String? {
        if (id!!.startsWith("a/")) {
            return "accounts" + id.substring(1)
        } else if (id.startsWith("c/")) {
            return "video-channels" + id.substring(1)
        }
        return id
    }

    companion object {
        val instance: PeertubeChannelLinkHandlerFactory = PeertubeChannelLinkHandlerFactory()
        private val ID_PATTERN: String = "((accounts|a)|(video-channels|c))/([^/?&#]*)"
        val API_ENDPOINT: String = "/api/v1/"
    }
}
