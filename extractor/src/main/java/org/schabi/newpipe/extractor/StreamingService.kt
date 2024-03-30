package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.feed.FeedExtractor
import org.schabi.newpipe.extractor.kiosk.KioskList
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import org.schabi.newpipe.extractor.utils.Utils
import java.util.Collections

/*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* StreamingService.java is part of NewPipe Extractor.
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
abstract class StreamingService(@JvmField val serviceId: Int,
                                name: String,
                                capabilities: List<MediaCapability?>?) {
    /**
     * This class holds meta information about the service implementation.
     */
    class ServiceInfo(@JvmField val name: String, mediaCapabilities: List<MediaCapability?>?) {
        val mediaCapabilities: List<MediaCapability?>

        /**
         * Creates a new instance of a ServiceInfo
         * @param name the name of the service
         * @param mediaCapabilities the type of media this service can handle
         */
        init {
            this.mediaCapabilities = Collections.unmodifiableList(mediaCapabilities)
        }

        enum class MediaCapability {
            AUDIO,
            VIDEO,
            LIVE,
            COMMENTS
        }
    }

    /**
     * LinkType will be used to determine which type of URL you are handling, and therefore which
     * part of NewPipe should handle a certain URL.
     */
    enum class LinkType {
        NONE,
        STREAM,
        CHANNEL,
        PLAYLIST
    }

    @JvmField
    val serviceInfo: ServiceInfo

    /**
     * Creates a new Streaming service.
     * If you Implement one do not set id within your implementation of this extractor, instead
     * set the id when you put the extractor into [ServiceList]
     * All other parameters can be set directly from the overriding constructor.
     * @param id the number of the service to identify him within the NewPipe frontend
     * @param name the name of the service
     * @param capabilities the type of media this service can handle
     */
    init {
        serviceInfo = ServiceInfo(name, capabilities)
    }

    public override fun toString(): String {
        return serviceId.toString() + ":" + serviceInfo.name
    }

    abstract val baseUrl: String?
    /*//////////////////////////////////////////////////////////////////////////
    // Url Id handler
    ////////////////////////////////////////////////////////////////////////// */
    /**
     * Must return a new instance of an implementation of LinkHandlerFactory for streams.
     * @return an instance of a LinkHandlerFactory for streams
     */
    abstract val streamLHFactory: LinkHandlerFactory

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for channels.
     * If support for channels is not given null must be returned.
     * @return an instance of a ListLinkHandlerFactory for channels or null
     */
    abstract val channelLHFactory: ListLinkHandlerFactory

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for channel tabs.
     * If support for channel tabs is not given null must be returned.
     *
     * @return an instance of a ListLinkHandlerFactory for channels or null
     */
    abstract val channelTabLHFactory: ListLinkHandlerFactory

    /**
     * Must return a new instance of an implementation of ListLinkHandlerFactory for playlists.
     * If support for playlists is not given null must be returned.
     * @return an instance of a ListLinkHandlerFactory for playlists or null
     */
    abstract val playlistLHFactory: ListLinkHandlerFactory?

    /**
     * Must return an instance of an implementation of SearchQueryHandlerFactory.
     * @return an instance of a SearchQueryHandlerFactory
     */
    abstract val searchQHFactory: SearchQueryHandlerFactory
    abstract val commentsLHFactory: ListLinkHandlerFactory?
    /*//////////////////////////////////////////////////////////////////////////
    // Extractors
    ////////////////////////////////////////////////////////////////////////// */
    /**
     * Must create a new instance of a SearchExtractor implementation.
     * @param queryHandler specifies the keyword lock for, and the filters which should be applied.
     * @return a new SearchExtractor instance
     */
    abstract fun getSearchExtractor(queryHandler: SearchQueryHandler?): SearchExtractor

    /**
     * Must create a new instance of a SuggestionExtractor implementation.
     * @return a new SuggestionExtractor instance
     */
    abstract val suggestionExtractor: SuggestionExtractor?

    /**
     * Outdated or obsolete. null can be returned.
     * @return just null
     */
    abstract val subscriptionExtractor: SubscriptionExtractor?

    /**
     * This method decides which strategy will be chosen to fetch the feed. In YouTube, for example,
     * a separate feed exists which is lightweight and made specifically to be used like this.
     *
     *
     * In services which there's no other way to retrieve them, null should be returned.
     *
     * @return a [FeedExtractor] instance or null.
     */
    @Throws(ExtractionException::class)
    open fun getFeedExtractor(url: String?): FeedExtractor? {
        return null
    }

    @JvmField
    @get:Throws(ExtractionException::class)
    abstract val kioskList: KioskList

    /**
     * Must create a new instance of a ChannelExtractor implementation.
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @return a new ChannelExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getChannelExtractor(linkHandler: ListLinkHandler?): ChannelExtractor

    /**
     * Must create a new instance of a ChannelTabExtractor implementation.
     *
     * @param linkHandler is pointing to the channel which should be handled by this new instance.
     * @return a new ChannelTabExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getChannelTabExtractor(linkHandler: ListLinkHandler?): ChannelTabExtractor?

    /**
     * Must crete a new instance of a PlaylistExtractor implementation.
     * @param linkHandler is pointing to the playlist which should be handled by this new instance.
     * @return a new PlaylistExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getPlaylistExtractor(linkHandler: ListLinkHandler?): PlaylistExtractor?

    /**
     * Must create a new instance of a StreamExtractor implementation.
     * @param linkHandler is pointing to the stream which should be handled by this new instance.
     * @return a new StreamExtractor
     */
    @Throws(ExtractionException::class)
    abstract fun getStreamExtractor(linkHandler: LinkHandler?): StreamExtractor
    @Throws(ExtractionException::class)
    abstract fun getCommentsExtractor(linkHandler: ListLinkHandler?): CommentsExtractor?

    /*//////////////////////////////////////////////////////////////////////////
    // Extractors without link handler
    ////////////////////////////////////////////////////////////////////////// */
    @Throws(ExtractionException::class)
    fun getSearchExtractor(query: String?,
                           contentFilter: List<String?>?,
                           sortFilter: String?): SearchExtractor {
        return getSearchExtractor(searchQHFactory
                .fromQuery(query, contentFilter, sortFilter))
    }

    @Throws(ExtractionException::class)
    fun getChannelExtractor(id: String?,
                            contentFilter: List<String?>?,
                            sortFilter: String?): ChannelExtractor {
        return getChannelExtractor(channelLHFactory
                .fromQuery(id, contentFilter, sortFilter))
    }

    @Throws(ExtractionException::class)
    fun getPlaylistExtractor(id: String?,
                             contentFilter: List<String?>?,
                             sortFilter: String?): PlaylistExtractor? {
        return getPlaylistExtractor(playlistLHFactory
                .fromQuery(id, contentFilter, sortFilter))
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Short extractors overloads
    ////////////////////////////////////////////////////////////////////////// */
    @Throws(ExtractionException::class)
    fun getSearchExtractor(query: String?): SearchExtractor {
        return getSearchExtractor(searchQHFactory.fromQuery(query))
    }

    @Throws(ExtractionException::class)
    fun getChannelExtractor(url: String?): ChannelExtractor {
        return getChannelExtractor(channelLHFactory.fromUrl(url))
    }

    @Throws(ExtractionException::class)
    fun getChannelTabExtractorFromId(id: String?, tab: String): ChannelTabExtractor? {
        return getChannelTabExtractor(channelTabLHFactory.fromQuery(
                id, listOf(tab), ""))
    }

    @Throws(ExtractionException::class)
    fun getChannelTabExtractorFromIdAndBaseUrl(id: String?,
                                               tab: String,
                                               baseUrl: String?): ChannelTabExtractor? {
        return getChannelTabExtractor(channelTabLHFactory.fromQuery(
                id, listOf(tab), "", baseUrl))
    }

    @Throws(ExtractionException::class)
    fun getPlaylistExtractor(url: String?): PlaylistExtractor? {
        return getPlaylistExtractor(playlistLHFactory!!.fromUrl(url))
    }

    @Throws(ExtractionException::class)
    fun getStreamExtractor(url: String?): StreamExtractor {
        return getStreamExtractor(streamLHFactory.fromUrl(url))
    }

    @Throws(ExtractionException::class)
    fun getCommentsExtractor(url: String?): CommentsExtractor? {
        val listLinkHandlerFactory: ListLinkHandlerFactory? = commentsLHFactory
        if (listLinkHandlerFactory == null) {
            return null
        }
        return getCommentsExtractor(listLinkHandlerFactory.fromUrl(url))
    }
    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////// */
    /**
     * Figures out where the link is pointing to (a channel, a video, a playlist, etc.)
     * @param url the url on which it should be decided of which link type it is
     * @return the link type of url
     */
    @Throws(ParsingException::class)
    fun getLinkTypeByUrl(url: String?): LinkType {
        val polishedUrl: String? = Utils.followGoogleRedirectIfNeeded(url)
        val sH: LinkHandlerFactory? = streamLHFactory
        val cH: LinkHandlerFactory? = channelLHFactory
        val pH: LinkHandlerFactory? = playlistLHFactory
        if (sH != null && sH.acceptUrl(polishedUrl)) {
            return LinkType.STREAM
        } else if (cH != null && cH.acceptUrl(polishedUrl)) {
            return LinkType.CHANNEL
        } else if (pH != null && pH.acceptUrl(polishedUrl)) {
            return LinkType.PLAYLIST
        } else {
            return LinkType.NONE
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    ////////////////////////////////////////////////////////////////////////// */
    open val supportedLocalizations: List<Localization>
        /**
         * Returns a list of localizations that this service supports.
         */
        get() {
            return listOf<Localization>(Localization.Companion.DEFAULT)
        }
    open val supportedCountries: List<ContentCountry?>
        /**
         * Returns a list of countries that this service supports.<br></br>
         */
        get() {
            return listOf<ContentCountry>(ContentCountry.Companion.DEFAULT)
        }
    val localization: Localization?
        /**
         * Returns the localization that should be used in this service. It will get which localization
         * the user prefer (using [NewPipe.getPreferredLocalization]), then it will:
         *
         *  * Check if the exactly localization is supported by this service.
         *  * If not, check if a less specific localization is available, using only the language
         * code.
         *  * Fallback to the [default][Localization.DEFAULT] localization.
         *
         */
        get() {
            val preferredLocalization: Localization = NewPipe.getPreferredLocalization()

            // Check the localization's language and country
            if (supportedLocalizations.contains(preferredLocalization)) {
                return preferredLocalization
            }

            // Fallback to the first supported language that matches the preferred language
            for (supportedLanguage: Localization in supportedLocalizations) {
                if ((supportedLanguage.languageCode
                                == preferredLocalization.languageCode)) {
                    return supportedLanguage
                }
            }
            return Localization.Companion.DEFAULT
        }
    val contentCountry: ContentCountry?
        /**
         * Returns the country that should be used to fetch content in this service. It will get which
         * country the user prefer (using [NewPipe.getPreferredContentCountry]), then it will:
         *
         *  * Check if the country is supported by this service.
         *  * If not, fallback to the [default][ContentCountry.DEFAULT] country.
         *
         */
        get() {
            val preferredContentCountry: ContentCountry? = NewPipe.getPreferredContentCountry()
            if (supportedCountries.contains(preferredContentCountry)) {
                return preferredContentCountry
            }
            return ContentCountry.Companion.DEFAULT
        }

    /**
     * Get an instance of the time ago parser using the patterns related to the passed localization.
     * <br></br><br></br>
     * Just like [.getLocalization], it will also try to fallback to a less specific
     * localization if the exact one is not available/supported.
     *
     * @throws IllegalArgumentException if the localization is not supported (parsing patterns are
     * not present).
     */
    fun getTimeAgoParser(localization: Localization?): TimeAgoParser {
        val targetParser: TimeAgoParser? = TimeAgoPatternsManager.getTimeAgoParserFor(localization)
        if (targetParser != null) {
            return targetParser
        }
        if (!localization!!.getCountryCode().isEmpty()) {
            val lessSpecificLocalization: Localization = Localization(localization.languageCode)
            val lessSpecificParser: TimeAgoParser? = TimeAgoPatternsManager.getTimeAgoParserFor(lessSpecificLocalization)
            if (lessSpecificParser != null) {
                return lessSpecificParser
            }
        }
        throw IllegalArgumentException(
                "Localization is not supported (\"" + localization + "\")")
    }
}
