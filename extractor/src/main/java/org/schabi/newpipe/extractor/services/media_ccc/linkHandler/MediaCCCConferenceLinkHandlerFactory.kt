package org.schabi.newpipe.extractor.services.media_ccc.linkHandler

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Parser

/**
 * Since MediaCCC does not really have channel tabs (i.e. it only has one single "tab" with videos),
 * this link handler acts both as the channel link handler and the channel tab link handler. That's
 * why [.getAvailableContentFilter] has been overridden.
 */
class MediaCCCConferenceLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return CONFERENCE_PATH + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return Parser.matchGroup1(ID_PATTERN, url)
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            return getId(url) != null
        } catch (e: ParsingException) {
            return false
        }
    }

    override val availableContentFilter: Array<String?>
        /**
         * @see MediaCCCConferenceLinkHandlerFactory
         *
         * @return MediaCCC's only channel "tab", i.e. [ChannelTabs.VIDEOS]
         */
        get() {
            return arrayOf(
                    ChannelTabs.VIDEOS)
        }

    companion object {
        val instance: MediaCCCConferenceLinkHandlerFactory = MediaCCCConferenceLinkHandlerFactory()
        val CONFERENCE_API_ENDPOINT: String = "https://api.media.ccc.de/public/conferences/"
        val CONFERENCE_PATH: String = "https://media.ccc.de/c/"
        private val ID_PATTERN: String = ("(?:(?:(?:api\\.)?media\\.ccc\\.de/public/conferences/)"
                + "|(?:media\\.ccc\\.de/[bc]/))([^/?&#]*)")
    }
}
