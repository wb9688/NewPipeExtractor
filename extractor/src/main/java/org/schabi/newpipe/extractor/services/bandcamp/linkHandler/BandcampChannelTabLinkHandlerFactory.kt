package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.UnsupportedTabException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

class BandcampChannelTabLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return BandcampChannelLinkHandlerFactory.Companion.getInstance().getId(url)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?, contentFilter: List<String?>?, sortFilter: String?): String? {
        return (BandcampChannelLinkHandlerFactory.Companion.getInstance().getUrl(id)
                + getUrlSuffix(contentFilter!!.get(0)))
    }

    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        return BandcampChannelLinkHandlerFactory.Companion.getInstance().onAcceptUrl(url)
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ChannelTabs.TRACKS,
                    ChannelTabs.ALBUMS)
        }

    companion object {
        val instance: BandcampChannelTabLinkHandlerFactory = BandcampChannelTabLinkHandlerFactory()

        /**
         * Get a tab's URL suffix.
         *
         *
         *
         * These URLs don't actually exist on the Bandcamp website, as both albums and tracks are
         * listed on the main page, but redirect to the main page, which is perfect for us as we need a
         * unique URL for each tab.
         *
         *
         * @param tab the tab value, which must not be null
         * @return a URL suffix
         * @throws UnsupportedTabException if the tab is not supported
         */
        @Nonnull
        @Throws(UnsupportedTabException::class)
        fun getUrlSuffix(@Nonnull tab: String?): String {
            when (tab) {
                ChannelTabs.TRACKS -> return "/track"
                ChannelTabs.ALBUMS -> return "/album"
            }
            throw UnsupportedTabException(tab)
        }
    }
}
