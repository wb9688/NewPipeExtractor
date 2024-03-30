package org.schabi.newpipe.extractor.services.media_ccc.search

import org.junit.jupiter.api.BeforeAll
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getSearchExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory

class MediaCCCSearchExtractorTest {
    class All : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "kde"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = MediaCCC.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    class Conferences : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
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

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "c3"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = MediaCCC.getSearchExtractor(QUERY, listOf<String>(MediaCCCSearchQueryHandlerFactory.CONFERENCES), "")
                extractor!!.fetchPage()
            }
        }
    }

    class Events : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return MediaCCC
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "media.ccc.de/public/events/search?q=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "linux"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = MediaCCC.getSearchExtractor(QUERY, listOf<String>(MediaCCCSearchQueryHandlerFactory.EVENTS), "")
                extractor!!.fetchPage()
            }
        }
    }
}
