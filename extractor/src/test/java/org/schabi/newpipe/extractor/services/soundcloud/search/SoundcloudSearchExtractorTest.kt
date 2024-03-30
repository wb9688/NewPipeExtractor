package org.schabi.newpipe.extractor.services.soundcloud.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getSearchExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException
import java.io.UnsupportedEncodingException

object SoundcloudSearchExtractorTest {
    private fun urlEncode(value: String): String {
        return try {
            encodeUrlUtf8(value)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        }
    }

    class All : DefaultSearchExtractorTest() {
        // @formatter:off
        override fun extractor(): SearchExtractor? {
            return extractor
        }
        override fun expectedService(): StreamingService {
            return SoundCloud
        }
        override fun expectedName(): String {
            return QUERY
        }
        override fun expectedId(): String {
            return QUERY
        }
        override fun expectedUrlContains(): String {
            return "soundcloud.com/search?q=" + urlEncode(QUERY)
        }
        override fun expectedOriginalUrlContains(): String {
            return "soundcloud.com/search?q=" + urlEncode(QUERY)
        }
        override fun expectedSearchString(): String {
            return QUERY
        }
        override fun expectedSearchSuggestion(): String? {
            return null
        } // @formatter:on

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "lill uzi vert"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    class Tracks : DefaultSearchExtractorTest() {
        // @formatter:off
        override fun extractor(): SearchExtractor? {
            return extractor
        }
        override fun expectedService(): StreamingService {
            return SoundCloud
        }
        override fun expectedName(): String {
            return QUERY
        }
        override fun expectedId(): String {
            return QUERY
        }
        override fun expectedUrlContains(): String {
            return "soundcloud.com/search/tracks?q=" + urlEncode(QUERY)
        }
        override fun expectedOriginalUrlContains(): String {
            return "soundcloud.com/search/tracks?q=" + urlEncode(QUERY)
        }
        override fun expectedSearchString(): String {
            return QUERY
        }
        override fun expectedSearchSuggestion(): String? {
            return null
        }
        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        } // @formatter:on

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "lill uzi vert"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.getSearchExtractor(QUERY, listOf<String>(SoundcloudSearchQueryHandlerFactory.TRACKS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class Users : DefaultSearchExtractorTest() {
        // @formatter:off
        override fun extractor(): SearchExtractor? {
            return extractor
        }
        override fun expectedService(): StreamingService {
            return SoundCloud
        }
        override fun expectedName(): String {
            return QUERY
        }
        override fun expectedId(): String {
            return QUERY
        }
        override fun expectedUrlContains(): String {
            return "soundcloud.com/search/users?q=" + urlEncode(QUERY)
        }
        override fun expectedOriginalUrlContains(): String {
            return "soundcloud.com/search/users?q=" + urlEncode(QUERY)
        }
        override fun expectedSearchString(): String {
            return QUERY
        }
        override fun expectedSearchSuggestion(): String? {
            return null
        }
        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        } // @formatter:on

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "lill uzi vert"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.getSearchExtractor(QUERY, listOf<String>(SoundcloudSearchQueryHandlerFactory.USERS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class Playlists : DefaultSearchExtractorTest() {
        // @formatter:off
        override fun extractor(): SearchExtractor? {
            return extractor
        }
        override fun expectedService(): StreamingService {
            return SoundCloud
        }
        override fun expectedName(): String {
            return QUERY
        }
        override fun expectedId(): String {
            return QUERY
        }
        override fun expectedUrlContains(): String {
            return "soundcloud.com/search/playlists?q=" + urlEncode(QUERY)
        }
        override fun expectedOriginalUrlContains(): String {
            return "soundcloud.com/search/playlists?q=" + urlEncode(QUERY)
        }
        override fun expectedSearchString(): String {
            return QUERY
        }
        override fun expectedSearchSuggestion(): String? {
            return null
        }
        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        } // @formatter:on

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "lill uzi vert"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.getSearchExtractor(QUERY, listOf<String>(SoundcloudSearchQueryHandlerFactory.PLAYLISTS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class PagingTest {
        @Test
        @Throws(Exception::class)
        fun duplicatedItemsCheck() {
            init(DownloaderTestImpl.Companion.getInstance())
            val extractor: SearchExtractor = SoundCloud.getSearchExtractor("cirque du soleil", listOf<String>(SoundcloudSearchQueryHandlerFactory.TRACKS), "")
            extractor.fetchPage()
            val page1 = extractor.initialPage
            val page2 = extractor.getPage(page1!!.nextPage)
            DefaultTests.assertNoDuplicatedItems(SoundCloud, page1, page2)
        }
    }

    class UserVerified : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "soundcloud.com/search/users?q=" + urlEncode(QUERY)
        }

        override fun expectedOriginalUrlContains(): String {
            return "soundcloud.com/search/users?q=" + urlEncode(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testIsVerified() {
            val items: List<InfoItem> = extractor!!.initialPage.getItems()
            var verified = false
            for (item in items) {
                if (item.url == "https://soundcloud.com/davidguetta") {
                    verified = (item as ChannelInfoItem).isVerified
                    break
                }
            }
            Assertions.assertTrue(verified)
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "David Guetta"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.getSearchExtractor(QUERY, listOf<String>(SoundcloudSearchQueryHandlerFactory.USERS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class NoNextPage : DefaultSearchExtractorTest() {
        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        @Throws(Exception::class)
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return QUERY
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "soundcloud.com/search?q=" + urlEncode(QUERY)
        }

        override fun expectedOriginalUrlContains(): String {
            return "soundcloud.com/search?q=" + urlEncode(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "wpghüä"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }
}
