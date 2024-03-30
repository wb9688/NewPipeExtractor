package org.schabi.newpipe.extractor.services.media_ccc

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
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCChannelTabExtractor
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceKiosk
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCLiveStreamExtractor
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCLiveStreamKiosk
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCRecentKiosk
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCSearchExtractor
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCStreamExtractor
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.util.Arrays

class MediaCCCService(id: Int) : StreamingService(id, "media.ccc.de", Arrays.asList<MediaCapability?>(MediaCapability.AUDIO, MediaCapability.VIDEO)) {
    public override fun getSearchExtractor(query: SearchQueryHandler?): SearchExtractor {
        return MediaCCCSearchExtractor(this, query)
    }

    override val streamLHFactory: LinkHandlerFactory
        get() {
            return MediaCCCStreamLinkHandlerFactory.Companion.getInstance()
        }
    override val channelLHFactory: ListLinkHandlerFactory
        get() {
            return MediaCCCConferenceLinkHandlerFactory.Companion.getInstance()
        }
    override val channelTabLHFactory: ListLinkHandlerFactory
        get() {
            // there is just one channel tab in MediaCCC, the one containing conferences, so there is
            // no need for a specific channel tab link handler, but we can just use the channel one
            return MediaCCCConferenceLinkHandlerFactory.Companion.getInstance()
        }
    override val playlistLHFactory: ListLinkHandlerFactory?
        get() {
            return null
        }
    override val searchQHFactory: SearchQueryHandlerFactory
        get() {
            return MediaCCCSearchQueryHandlerFactory.Companion.getInstance()
        }

    public override fun getStreamExtractor(linkHandler: LinkHandler?): StreamExtractor {
        if (MediaCCCParsingHelper.isLiveStreamId(linkHandler.getId())) {
            return MediaCCCLiveStreamExtractor(this, linkHandler)
        }
        return MediaCCCStreamExtractor(this, linkHandler)
    }

    public override fun getChannelExtractor(linkHandler: ListLinkHandler?): ChannelExtractor {
        return MediaCCCConferenceExtractor(this, linkHandler)
    }

    public override fun getChannelTabExtractor(linkHandler: ListLinkHandler?): ChannelTabExtractor? {
        if (linkHandler is ReadyChannelTabListLinkHandler) {
            // conference data has already been fetched, let the ReadyChannelTabListLinkHandler
            // create a MediaCCCChannelTabExtractor with that data
            return linkHandler.getChannelTabExtractor(this)
        } else {
            // conference data has not been fetched yet, so pass null instead
            return MediaCCCChannelTabExtractor(this, linkHandler, null)
        }
    }

    public override fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor? {
        return null
    }

    override val suggestionExtractor: SuggestionExtractor?
        get() {
            return null
        }

    @get:Throws(ExtractionException::class)
    override val kioskList: KioskList
        get() {
            val list: KioskList = KioskList(this)
            val h: ListLinkHandlerFactory = MediaCCCConferencesListLinkHandlerFactory.Companion.getInstance()

            // add kiosks here e.g.:
            try {
                list.addKioskEntry(
                        KioskExtractorFactory({ streamingService: StreamingService?, url: String?, kioskId: String ->
                            MediaCCCConferenceKiosk(
                                    this@MediaCCCService,
                                    h.fromUrl(url),
                                    kioskId
                            )
                        }),
                        h,
                        MediaCCCConferenceKiosk.Companion.KIOSK_ID
                )
                list.addKioskEntry(
                        KioskExtractorFactory({ streamingService: StreamingService?, url: String?, kioskId: String ->
                            MediaCCCRecentKiosk(
                                    this@MediaCCCService,
                                    h.fromUrl(url),
                                    kioskId
                            )
                        }),
                        h,
                        MediaCCCRecentKiosk.Companion.KIOSK_ID
                )
                list.addKioskEntry(
                        KioskExtractorFactory({ streamingService: StreamingService?, url: String?, kioskId: String ->
                            MediaCCCLiveStreamKiosk(
                                    this@MediaCCCService,
                                    h.fromUrl(url),
                                    kioskId
                            )
                        }),
                        h,
                        MediaCCCLiveStreamKiosk.Companion.KIOSK_ID
                )
                list.setDefaultKiosk(MediaCCCRecentKiosk.Companion.KIOSK_ID)
            } catch (e: Exception) {
                throw ExtractionException(e)
            }
            return list
        }
    override val subscriptionExtractor: SubscriptionExtractor?
        get() {
            return null
        }
    override val commentsLHFactory: ListLinkHandlerFactory?
        get() {
            return null
        }

    public override fun getCommentsExtractor(linkHandler: ListLinkHandler?): CommentsExtractor? {
        return null
    }

    override val baseUrl: String?
        get() {
            return "https://media.ccc.de"
        }
}
