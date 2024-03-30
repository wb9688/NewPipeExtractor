package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.UnsupportedEncodingException

class PeertubeSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(searchString: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?): String? {
        val baseUrl: String?
        if (!contentFilters!!.isEmpty() && contentFilters.get(0)!!.startsWith("sepia_")) {
            baseUrl = SEPIA_BASE_URL
        } else {
            baseUrl = ServiceList.PeerTube.getBaseUrl()
        }
        return getUrl(searchString, contentFilters, sortFilter, baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(searchString: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?,
                               baseUrl: String?): String? {
        try {
            val endpoint: String
            if ((contentFilters!!.isEmpty()
                            || (contentFilters.get(0) == VIDEOS) || (contentFilters.get(0) == SEPIA_VIDEOS))) {
                endpoint = SEARCH_ENDPOINT_VIDEOS
            } else if ((contentFilters.get(0) == CHANNELS)) {
                endpoint = SEARCH_ENDPOINT_CHANNELS
            } else {
                endpoint = SEARCH_ENDPOINT_PLAYLISTS
            }
            return baseUrl + endpoint + "?search=" + Utils.encodeUrlUtf8(searchString)
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not encode query", e)
        }
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    VIDEOS,
                    PLAYLISTS,
                    CHANNELS,
                    SEPIA_VIDEOS)
        }

    companion object {
        val instance: PeertubeSearchQueryHandlerFactory = PeertubeSearchQueryHandlerFactory()
        @JvmField
        val VIDEOS: String = "videos"
        @JvmField
        val SEPIA_VIDEOS: String = "sepia_videos" // sepia is the global index
        @JvmField
        val PLAYLISTS: String = "playlists"
        @JvmField
        val CHANNELS: String = "channels"
        val SEPIA_BASE_URL: String = "https://sepiasearch.org"
        val SEARCH_ENDPOINT_PLAYLISTS: String = "/api/v1/search/video-playlists"
        val SEARCH_ENDPOINT_VIDEOS: String = "/api/v1/search/videos"
        val SEARCH_ENDPOINT_CHANNELS: String = "/api/v1/search/video-channels"
    }
}
