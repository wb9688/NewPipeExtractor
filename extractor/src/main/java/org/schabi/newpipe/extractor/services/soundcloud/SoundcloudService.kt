package org.schabi.newpipe.extractor.services.soundcloud

import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.kiosk.KioskList
import org.schabi.newpipe.extractor.kiosk.KioskList.KioskExtractorFactory
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChartsExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudCommentsExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudSearchExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudStreamExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudSubscriptionExtractor
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudSuggestionExtractor
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChartsLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudCommentsLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudPlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.util.Arrays

class SoundcloudService(id: Int) : StreamingService(id, "SoundCloud", Arrays.asList<MediaCapability?>(MediaCapability.AUDIO, MediaCapability.COMMENTS)) {
    override val baseUrl: String?
        get() {
            return "https://soundcloud.com"
        }
    override val searchQHFactory: SearchQueryHandlerFactory
        get() {
            return SoundcloudSearchQueryHandlerFactory.Companion.getInstance()
        }
    override val streamLHFactory: LinkHandlerFactory
        get() {
            return SoundcloudStreamLinkHandlerFactory.Companion.getInstance()
        }
    override val channelLHFactory: ListLinkHandlerFactory
        get() {
            return SoundcloudChannelLinkHandlerFactory.Companion.getInstance()
        }
    override val channelTabLHFactory: ListLinkHandlerFactory
        get() {
            return SoundcloudChannelTabLinkHandlerFactory.Companion.getInstance()
        }
    override val playlistLHFactory: ListLinkHandlerFactory?
        get() {
            return SoundcloudPlaylistLinkHandlerFactory.Companion.getInstance()
        }
    override val supportedCountries: List<ContentCountry?>
        get() {
            // Country selector here: https://soundcloud.com/charts/top?genre=all-music
            return ContentCountry.Companion.listFrom(
                    "AU", "CA", "DE", "FR", "GB", "IE", "NL", "NZ", "US"
            )
        }

    public override fun getStreamExtractor(linkHandler: LinkHandler?): StreamExtractor {
        return SoundcloudStreamExtractor(this, linkHandler)
    }

    public override fun getChannelExtractor(linkHandler: ListLinkHandler?): ChannelExtractor {
        return SoundcloudChannelExtractor(this, linkHandler)
    }

    public override fun getChannelTabExtractor(linkHandler: ListLinkHandler?): ChannelTabExtractor? {
        return SoundcloudChannelTabExtractor(this, linkHandler)
    }

    public override fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor? {
        return SoundcloudPlaylistExtractor(this, linkHandler)
    }

    public override fun getSearchExtractor(queryHandler: SearchQueryHandler?): SearchExtractor {
        return SoundcloudSearchExtractor(this, queryHandler)
    }

    override val suggestionExtractor: SuggestionExtractor?
        get() {
            return SoundcloudSuggestionExtractor(this)
        }

    @get:Throws(ExtractionException::class)
    override val kioskList: KioskList
        get() {
            val list: KioskList = KioskList(this)
            val h: SoundcloudChartsLinkHandlerFactory = SoundcloudChartsLinkHandlerFactory.Companion.getInstance()
            val chartsFactory: KioskExtractorFactory = KioskExtractorFactory({ streamingService: StreamingService?, url: String?, id: String ->
                SoundcloudChartsExtractor(this@SoundcloudService,
                        h.fromUrl(url), id)
            })

            // add kiosks here e.g.:
            try {
                list.addKioskEntry(chartsFactory, h, "Top 50")
                list.addKioskEntry(chartsFactory, h, "New & hot")
                list.setDefaultKiosk("New & hot")
            } catch (e: Exception) {
                throw ExtractionException(e)
            }
            return list
        }
    override val subscriptionExtractor: SubscriptionExtractor?
        get() {
            return SoundcloudSubscriptionExtractor(this)
        }
    override val commentsLHFactory: ListLinkHandlerFactory?
        get() {
            return SoundcloudCommentsLinkHandlerFactory.Companion.getInstance()
        }

    @Throws(ExtractionException::class)
    public override fun getCommentsExtractor(linkHandler: ListLinkHandler?): CommentsExtractor? {
        return SoundcloudCommentsExtractor(this, linkHandler)
    }
}
