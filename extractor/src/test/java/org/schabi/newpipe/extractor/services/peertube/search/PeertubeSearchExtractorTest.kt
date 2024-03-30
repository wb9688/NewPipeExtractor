package org.schabi.newpipe.extractor.services.peertube.search

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getSearchExtractor
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory

class PeertubeSearchExtractorTest {
    class All : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "fsf"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    class SepiaSearch : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return PeerTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "/search/videos?search=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "kde"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.getSearchExtractor(QUERY, listOf<String>(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class PagingTest {
        @Test
        @Disabled("Exception in CI: javax.net.ssl.SSLHandshakeException: PKIX path validation failed: java.security.cert.CertPathValidatorException: validity check failed")
        @Throws(Exception::class)
        fun duplicatedItemsCheck() {
            init(DownloaderTestImpl.Companion.getInstance())
            val extractor: SearchExtractor = PeerTube.getSearchExtractor("internet", listOf<String>(PeertubeSearchQueryHandlerFactory.VIDEOS), "")
            extractor.fetchPage()
            val page1 = extractor.initialPage
            val page2 = extractor.getPage(page1!!.nextPage)
            DefaultTests.assertNoDuplicatedItems(PeerTube, page1, page2)
        }
    }
}
