package org.schabi.newpipe.extractor.services.youtube.linkHandler

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

class YoutubeChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return "https://www.youtube.com/" + id + getUrlSuffix(contentFilter!!.get(0))
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return YoutubeChannelLinkHandlerFactory.Companion.getInstance().getId(url)
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            getId(url)
        } catch (e: ParsingException) {
            return false
        }
        return true
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ChannelTabs.VIDEOS,
                    ChannelTabs.SHORTS,
                    ChannelTabs.LIVESTREAMS,
                    ChannelTabs.ALBUMS,
                    ChannelTabs.PLAYLISTS
            )
        }

    companion object {
        val instance: YoutubeChannelTabLinkHandlerFactory = YoutubeChannelTabLinkHandlerFactory()
        @Nonnull
        @Throws(UnsupportedTabException::class)
        fun getUrlSuffix(tab: String?): String {
            when (tab) {
                ChannelTabs.VIDEOS -> return "/videos"
                ChannelTabs.SHORTS -> return "/shorts"
                ChannelTabs.LIVESTREAMS -> return "/streams"
                ChannelTabs.ALBUMS -> return "/releases"
                ChannelTabs.PLAYLISTS -> return "/playlists"
                else -> throw UnsupportedTabException(tab)
            }
        }
    }
}
