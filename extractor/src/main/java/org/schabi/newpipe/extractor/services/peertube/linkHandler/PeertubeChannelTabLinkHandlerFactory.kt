package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

class PeertubeChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return PeertubeChannelLinkHandlerFactory.Companion.getInstance().getId(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?, contentFilter: List<String?>?, sortFilter: String?): String? {
        return (PeertubeChannelLinkHandlerFactory.Companion.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter!!.get(0)))
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?,
                               baseUrl: String?): String? {
        return (PeertubeChannelLinkHandlerFactory.Companion.getInstance().getUrl(id, null, null, baseUrl)
                + getUrlSuffix(contentFilter!!.get(0)))
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        return PeertubeChannelLinkHandlerFactory.Companion.getInstance().onAcceptUrl(url)
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ChannelTabs.VIDEOS,
                    ChannelTabs.CHANNELS,
                    ChannelTabs.PLAYLISTS)
        }

    companion object {
        val instance: PeertubeChannelTabLinkHandlerFactory = PeertubeChannelTabLinkHandlerFactory()
        @Nonnull
        @Throws(UnsupportedTabException::class)
        fun getUrlSuffix(@Nonnull tab: String?): String {
            when (tab) {
                ChannelTabs.VIDEOS -> return "/videos"
                ChannelTabs.CHANNELS -> return "/video-channels"
                ChannelTabs.PLAYLISTS -> return "/video-playlists"
            }
            throw UnsupportedTabException(tab)
        }
    }
}
