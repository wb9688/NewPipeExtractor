package org.schabi.newpipe.extractor.services.peertube

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
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSearchExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSuggestionExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeCommentsLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeTrendingLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.util.Arrays

class PeertubeService @JvmOverloads constructor(id: Int, @JvmField var instance: PeertubeInstance = PeertubeInstance.Companion.DEFAULT_INSTANCE) : StreamingService(id, "PeerTube", Arrays.asList<MediaCapability?>(MediaCapability.VIDEO, MediaCapability.COMMENTS)) {

    override val streamLHFactory: LinkHandlerFactory
        get() {
            return PeertubeStreamLinkHandlerFactory.Companion.getInstance()
        }
    override val channelLHFactory: ListLinkHandlerFactory
        get() {
            return PeertubeChannelLinkHandlerFactory.Companion.getInstance()
        }
    override val channelTabLHFactory: ListLinkHandlerFactory
        get() {
            return PeertubeChannelTabLinkHandlerFactory.Companion.getInstance()
        }
    override val playlistLHFactory: ListLinkHandlerFactory?
        get() {
            return PeertubePlaylistLinkHandlerFactory.Companion.getInstance()
        }
    override val searchQHFactory: SearchQueryHandlerFactory
        get() {
            return PeertubeSearchQueryHandlerFactory.Companion.getInstance()
        }
    override val commentsLHFactory: ListLinkHandlerFactory?
        get() {
            return PeertubeCommentsLinkHandlerFactory.Companion.getInstance()
        }

    public override fun getSearchExtractor(queryHandler: SearchQueryHandler?): SearchExtractor {
        val contentFilters: List<String?>? = queryHandler.getContentFilters()
        return PeertubeSearchExtractor(this, queryHandler,
                !contentFilters!!.isEmpty() && contentFilters.get(0)!!.startsWith("sepia_"))
    }

    override val suggestionExtractor: SuggestionExtractor?
        get() {
            return PeertubeSuggestionExtractor(this)
        }
    override val subscriptionExtractor: SubscriptionExtractor?
        get() {
            return null
        }

    @Throws(ExtractionException::class)
    public override fun getChannelExtractor(linkHandler: ListLinkHandler?): ChannelExtractor {
        if (linkHandler.getUrl().contains("/video-channels/")) {
            return PeertubeChannelExtractor(this, linkHandler)
        } else {
            return PeertubeAccountExtractor(this, linkHandler)
        }
    }

    @Throws(ExtractionException::class)
    public override fun getChannelTabExtractor(linkHandler: ListLinkHandler?): ChannelTabExtractor? {
        return PeertubeChannelTabExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    public override fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor? {
        return PeertubePlaylistExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    public override fun getStreamExtractor(linkHandler: LinkHandler?): StreamExtractor {
        return PeertubeStreamExtractor(this, linkHandler)
    }

    @Throws(ExtractionException::class)
    public override fun getCommentsExtractor(linkHandler: ListLinkHandler?): CommentsExtractor? {
        return PeertubeCommentsExtractor(this, linkHandler)
    }

    override val baseUrl: String?
        get() {
            return instance.getUrl()
        }

    @get:Throws(ExtractionException::class)
    override val kioskList: KioskList
        get() {
            val h: PeertubeTrendingLinkHandlerFactory = PeertubeTrendingLinkHandlerFactory.Companion.getInstance()
            val kioskFactory: KioskExtractorFactory = KioskExtractorFactory({ streamingService: StreamingService?, url: String?, id: String ->
                PeertubeTrendingExtractor(
                        this@PeertubeService,
                        h.fromId(id),
                        id
                )
            })
            val list: KioskList = KioskList(this)

            // add kiosks here e.g.:
            try {
                list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.Companion.KIOSK_TRENDING)
                list.addKioskEntry(kioskFactory, h,
                        PeertubeTrendingLinkHandlerFactory.Companion.KIOSK_MOST_LIKED)
                list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.Companion.KIOSK_RECENT)
                list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.Companion.KIOSK_LOCAL)
                list.setDefaultKiosk(PeertubeTrendingLinkHandlerFactory.Companion.KIOSK_TRENDING)
            } catch (e: Exception) {
                throw ExtractionException(e)
            }
            return list
        }
}
