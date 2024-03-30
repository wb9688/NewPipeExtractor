// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

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
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelTabExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampCommentsExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampPlaylistExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSearchExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSuggestionExtractor
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampCommentsLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampFeaturedLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampPlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.util.Arrays

class BandcampService(id: Int) : StreamingService(id, "Bandcamp", Arrays.asList<MediaCapability?>(MediaCapability.AUDIO, MediaCapability.COMMENTS)) {
    override val baseUrl: String?
        get() {
            return BandcampExtractorHelper.BASE_URL
        }
    override val streamLHFactory: LinkHandlerFactory
        get() {
            return BandcampStreamLinkHandlerFactory.Companion.getInstance()
        }
    override val channelLHFactory: ListLinkHandlerFactory
        get() {
            return BandcampChannelLinkHandlerFactory.Companion.getInstance()
        }
    override val channelTabLHFactory: ListLinkHandlerFactory
        get() {
            return BandcampChannelTabLinkHandlerFactory.Companion.getInstance()
        }
    override val playlistLHFactory: ListLinkHandlerFactory?
        get() {
            return BandcampPlaylistLinkHandlerFactory.Companion.getInstance()
        }
    override val searchQHFactory: SearchQueryHandlerFactory
        get() {
            return BandcampSearchQueryHandlerFactory.Companion.getInstance()
        }
    override val commentsLHFactory: ListLinkHandlerFactory?
        get() {
            return BandcampCommentsLinkHandlerFactory.Companion.getInstance()
        }

    public override fun getSearchExtractor(queryHandler: SearchQueryHandler?): SearchExtractor {
        return BandcampSearchExtractor(this, queryHandler)
    }

    override val suggestionExtractor: SuggestionExtractor?
        get() {
            return BandcampSuggestionExtractor(this)
        }
    override val subscriptionExtractor: SubscriptionExtractor?
        get() {
            return null
        }

    @get:Throws(ExtractionException::class)
    override val kioskList: KioskList
        get() {
            val kioskList: KioskList = KioskList(this)
            val h: ListLinkHandlerFactory = BandcampFeaturedLinkHandlerFactory.Companion.getInstance()
            try {
                kioskList.addKioskEntry(
                        KioskExtractorFactory({ streamingService: StreamingService?, url: String?, kioskId: String ->
                            BandcampFeaturedExtractor(
                                    this@BandcampService,
                                    h.fromUrl(BandcampFeaturedExtractor.Companion.FEATURED_API_URL),
                                    kioskId
                            )
                        }),
                        h,
                        BandcampFeaturedExtractor.Companion.KIOSK_FEATURED
                )
                kioskList.addKioskEntry(
                        KioskExtractorFactory({ streamingService: StreamingService?, url: String?, kioskId: String ->
                            BandcampRadioExtractor(
                                    this@BandcampService,
                                    h.fromUrl(BandcampRadioExtractor.Companion.RADIO_API_URL),
                                    kioskId
                            )
                        }),
                        h,
                        BandcampRadioExtractor.Companion.KIOSK_RADIO
                )
                kioskList.setDefaultKiosk(BandcampFeaturedExtractor.Companion.KIOSK_FEATURED)
            } catch (e: Exception) {
                throw ExtractionException(e)
            }
            return kioskList
        }

    public override fun getChannelExtractor(linkHandler: ListLinkHandler?): ChannelExtractor {
        return BandcampChannelExtractor(this, linkHandler)
    }

    public override fun getChannelTabExtractor(linkHandler: ListLinkHandler?): ChannelTabExtractor? {
        if (linkHandler is ReadyChannelTabListLinkHandler) {
            return linkHandler.getChannelTabExtractor(this)
        } else {
            return BandcampChannelTabExtractor(this, linkHandler)
        }
    }

    public override fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor? {
        return BandcampPlaylistExtractor(this, linkHandler)
    }

    public override fun getStreamExtractor(linkHandler: LinkHandler?): StreamExtractor {
        if (BandcampExtractorHelper.isRadioUrl(linkHandler.getUrl())) {
            return BandcampRadioStreamExtractor(this, linkHandler)
        }
        return BandcampStreamExtractor(this, linkHandler)
    }

    public override fun getCommentsExtractor(linkHandler: ListLinkHandler?): CommentsExtractor? {
        return BandcampCommentsExtractor(this, linkHandler)
    }
}
