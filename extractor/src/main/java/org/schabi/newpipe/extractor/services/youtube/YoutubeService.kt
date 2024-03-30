package org.schabi.newpipe.extractor.services.youtube

import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.feed.FeedExtractor
import org.schabi.newpipe.extractor.kiosk.KioskList
import org.schabi.newpipe.extractor.kiosk.KioskList.KioskExtractorFactory
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMusicSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSubscriptionExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSuggestionExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeTrendingExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeTrendingLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.util.Arrays

/*
* Created by Christian Schabesberger on 23.08.15.
*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* YoutubeService.java is part of NewPipe Extractor.
*
* NewPipe Extractor is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* NewPipe Extractor is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
*/
class YoutubeService(id: Int) : StreamingService(id, "YouTube", Arrays.asList<MediaCapability?>(MediaCapability.AUDIO, MediaCapability.VIDEO, MediaCapability.LIVE, MediaCapability.COMMENTS)) {
    override val baseUrl: String?
        get() {
            return "https://youtube.com"
        }
    override val streamLHFactory: LinkHandlerFactory
        get() {
            return YoutubeStreamLinkHandlerFactory.Companion.getInstance()
        }
    override val channelLHFactory: ListLinkHandlerFactory
        get() {
            return YoutubeChannelLinkHandlerFactory.Companion.getInstance()
        }
    override val channelTabLHFactory: ListLinkHandlerFactory
        get() {
            return YoutubeChannelTabLinkHandlerFactory.Companion.getInstance()
        }
    override val playlistLHFactory: ListLinkHandlerFactory?
        get() {
            return YoutubePlaylistLinkHandlerFactory.Companion.getInstance()
        }
    override val searchQHFactory: SearchQueryHandlerFactory
        get() {
            return YoutubeSearchQueryHandlerFactory.Companion.getInstance()
        }

    public override fun getStreamExtractor(linkHandler: LinkHandler?): StreamExtractor {
        return YoutubeStreamExtractor(this, linkHandler)
    }

    public override fun getChannelExtractor(linkHandler: ListLinkHandler?): ChannelExtractor {
        return YoutubeChannelExtractor(this, linkHandler)
    }

    public override fun getChannelTabExtractor(linkHandler: ListLinkHandler?): ChannelTabExtractor? {
        if (linkHandler is ReadyChannelTabListLinkHandler) {
            return linkHandler.getChannelTabExtractor(this)
        } else {
            return YoutubeChannelTabExtractor(this, linkHandler)
        }
    }

    public override fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor? {
        if (YoutubeParsingHelper.isYoutubeMixId(linkHandler.getId())) {
            return YoutubeMixPlaylistExtractor(this, linkHandler)
        } else {
            return YoutubePlaylistExtractor(this, linkHandler)
        }
    }

    public override fun getSearchExtractor(query: SearchQueryHandler?): SearchExtractor {
        val contentFilters: List<String?>? = query.getContentFilters()
        if (!contentFilters!!.isEmpty() && contentFilters.get(0)!!.startsWith("music_")) {
            return YoutubeMusicSearchExtractor(this, query)
        } else {
            return YoutubeSearchExtractor(this, query)
        }
    }

    override val suggestionExtractor: SuggestionExtractor?
        get() {
            return YoutubeSuggestionExtractor(this)
        }

    @get:Throws(ExtractionException::class)
    override val kioskList: KioskList
        get() {
            val list: KioskList = KioskList(this)
            val h: ListLinkHandlerFactory = YoutubeTrendingLinkHandlerFactory.Companion.getInstance()

            // add kiosks here e.g.:
            try {
                list.addKioskEntry(
                        KioskExtractorFactory({ streamingService: StreamingService?, url: String?, id: String ->
                            YoutubeTrendingExtractor(
                                    this@YoutubeService,
                                    h.fromUrl(url),
                                    id
                            )
                        }),
                        h,
                        YoutubeTrendingExtractor.Companion.KIOSK_ID
                )
                list.setDefaultKiosk(YoutubeTrendingExtractor.Companion.KIOSK_ID)
            } catch (e: Exception) {
                throw ExtractionException(e)
            }
            return list
        }
    override val subscriptionExtractor: SubscriptionExtractor?
        get() {
            return YoutubeSubscriptionExtractor(this)
        }

    @Throws(ExtractionException::class)
    public override fun getFeedExtractor(channelUrl: String?): FeedExtractor? {
        return YoutubeFeedExtractor(this, channelLHFactory.fromUrl(channelUrl))
    }

    override val commentsLHFactory: ListLinkHandlerFactory?
        get() {
            return YoutubeCommentsLinkHandlerFactory.Companion.getInstance()
        }

    @Throws(ExtractionException::class)
    public override fun getCommentsExtractor(urlIdHandler: ListLinkHandler?): CommentsExtractor? {
        return YoutubeCommentsExtractor(this, urlIdHandler)
    }

    companion object {
        /*//////////////////////////////////////////////////////////////////////////
    // Localization
    ////////////////////////////////////////////////////////////////////////// */
        // https://www.youtube.com/picker_ajax?action_language_json=1
        val supportedLocalizations: List<Localization?> = Localization.Companion.listFrom(
                "en-GB" /*"af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "da", "de",
            "el", "en", "en-GB", "es", "es-419", "es-US", "et", "eu", "fa", "fi", "fil", "fr",
            "fr-CA", "gl", "gu", "hi", "hr", "hu", "hy", "id", "is", "it", "iw", "ja",
            "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv", "mk", "ml", "mn",
            "mr", "ms", "my", "ne", "nl", "no", "pa", "pl", "pt", "pt-PT", "ro", "ru",
            "si", "sk", "sl", "sq", "sr", "sr-Latn", "sv", "sw", "ta", "te", "th", "tr",
            "uk", "ur", "uz", "vi", "zh-CN", "zh-HK", "zh-TW", "zu"*/
        )
            get() {
                return Companion.field
            }

        // https://www.youtube.com/picker_ajax?action_country_json=1
        val supportedCountries: List<ContentCountry?> = ContentCountry.Companion.listFrom(
                "DZ", "AR", "AU", "AT", "AZ", "BH", "BD", "BY", "BE", "BO", "BA", "BR", "BG", "KH",
                "CA", "CL", "CO", "CR", "HR", "CY", "CZ", "DK", "DO", "EC", "EG", "SV", "EE", "FI",
                "FR", "GE", "DE", "GH", "GR", "GT", "HN", "HK", "HU", "IS", "IN", "ID", "IQ", "IE",
                "IL", "IT", "JM", "JP", "JO", "KZ", "KE", "KW", "LA", "LV", "LB", "LY", "LI", "LT",
                "LU", "MY", "MT", "MX", "ME", "MA", "NP", "NL", "NZ", "NI", "NG", "MK", "NO", "OM",
                "PK", "PA", "PG", "PY", "PE", "PH", "PL", "PT", "PR", "QA", "RO", "RU", "SA", "SN",
                "RS", "SG", "SK", "SI", "ZA", "KR", "ES", "LK", "SE", "CH", "TW", "TZ", "TH", "TN",
                "TR", "UG", "UA", "AE", "GB", "US", "UY", "VE", "VN", "YE", "ZW"
        )
            get() {
                return Companion.field
            }
    }
}
