package org.schabi.newpipe.extractor.services.youtube.linkHandler

import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.net.MalformedURLException
import java.net.URL

class YoutubePlaylistLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?, contentFilters: List<String?>?,
                               sortFilter: String?): String? {
        return "https://www.youtube.com/playlist?list=" + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        try {
            val urlObj: URL? = Utils.stringToURL(url)
            if (!Utils.isHTTP(urlObj) || !((YoutubeParsingHelper.isYoutubeURL(urlObj)
                            || YoutubeParsingHelper.isInvidiousURL(urlObj)))) {
                throw ParsingException("the url given is not a YouTube-URL")
            }
            val path: String = urlObj!!.getPath()
            if (!(path == "/watch") && !(path == "/playlist")) {
                throw ParsingException("the url given is neither a video nor a playlist URL")
            }
            val listID: String? = Utils.getQueryValue(urlObj, "list")
            if (listID == null) {
                throw ParsingException("the URL given does not include a playlist")
            }
            if (!listID.matches("[a-zA-Z0-9_-]{10,}".toRegex())) {
                throw ParsingException(
                        "the list-ID given in the URL does not match the list pattern")
            }
            if ((YoutubeParsingHelper.isYoutubeChannelMixId(listID)
                            && Utils.getQueryValue(urlObj, "v") == null)) {
                // Video id can't be determined from the channel mix id.
                // See YoutubeParsingHelper#extractVideoIdFromMixId
                throw ContentNotSupportedException(
                        "Channel Mix without a video id are not supported")
            }
            return listID
        } catch (exception: Exception) {
            throw ParsingException("Error could not parse URL: " + exception.message,
                    exception)
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            getId(url)
        } catch (e: ParsingException) {
            return false
        }
        return true
    }

    /**
     * If it is a mix (auto-generated playlist) URL, return a [LinkHandler] where the URL is
     * like `https://youtube.com/watch?v=videoId&list=playlistId`
     *
     * Otherwise use super
     */
    @Throws(ParsingException::class)
    public override fun fromUrl(url: String?): ListLinkHandler? {
        try {
            val urlObj: URL? = Utils.stringToURL(url)
            val listID: String? = Utils.getQueryValue(urlObj, "list")
            if (listID != null && YoutubeParsingHelper.isYoutubeMixId(listID)) {
                var videoID: String? = Utils.getQueryValue(urlObj, "v")
                if (videoID == null) {
                    videoID = YoutubeParsingHelper.extractVideoIdFromMixId(listID)
                }
                val newUrl: String = ("https://www.youtube.com/watch?v=" + videoID
                        + "&list=" + listID)
                return ListLinkHandler(LinkHandler(url, newUrl, listID))
            }
        } catch (exception: MalformedURLException) {
            throw ParsingException("Error could not parse URL: " + exception.message,
                    exception)
        }
        return super.fromUrl(url)
    }

    companion object {
        val instance: YoutubePlaylistLinkHandlerFactory = YoutubePlaylistLinkHandlerFactory()
    }
}
