package org.schabi.newpipe.extractor.services.soundcloud.linkHandler

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

class SoundcloudChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class)
    public override fun getId(url: String?): String? {
        return SoundcloudChannelLinkHandlerFactory.Companion.getInstance().getId(url)
    }

    @Throws(ParsingException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return (SoundcloudChannelLinkHandlerFactory.Companion.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter!!.get(0)))
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        return SoundcloudChannelLinkHandlerFactory.Companion.getInstance().onAcceptUrl(url)
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ChannelTabs.TRACKS,
                    ChannelTabs.PLAYLISTS,
                    ChannelTabs.ALBUMS)
        }

    companion object {
        val instance: SoundcloudChannelTabLinkHandlerFactory = SoundcloudChannelTabLinkHandlerFactory()
        @Nonnull
        @Throws(UnsupportedOperationException::class)
        fun getUrlSuffix(tab: String?): String {
            when (tab) {
                ChannelTabs.TRACKS -> return "/tracks"
                ChannelTabs.PLAYLISTS -> return "/sets"
                ChannelTabs.ALBUMS -> return "/albums"
            }
            throw UnsupportedTabException(tab)
        }
    }
}
