package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.BaseListExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.getFeedExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor
import java.io.IOException

object YoutubeFeedExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/feed/"

    class Kurzgesagt : BaseListExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertTrue(extractor!!.name!!.startsWith("Kurzgesagt"))
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.id)
        }

        @Test
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/user/Kurzgesagt", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            DefaultTests.assertNoMoreItems(extractor!!)
        }

        companion object {
            private var extractor: YoutubeFeedExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH))
                extractor = YouTube
                        .getFeedExtractor("https://www.youtube.com/user/Kurzgesagt")
                extractor!!.fetchPage()
            }
        }
    }

    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun AccountTerminatedFetch() {
            val extractor = YouTube
                    .getFeedExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ") as YoutubeFeedExtractor
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        companion object {
            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notAvailable/"))
            }
        }
    }
}