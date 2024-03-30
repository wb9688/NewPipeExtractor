package org.schabi.newpipe.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskList.getExtractorById
import org.schabi.newpipe.extractor.services.BaseListExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.kioskList
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.kioskList
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.kioskList
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList

class PeertubeTrendingExtractorTest {
    class Trending : BaseListExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("Trending", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("Trending", extractor!!.getId())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/videos?sort=-trending", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/videos?sort=-trending", extractor!!.originalUrl)
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
            DefaultTests.defaultTestMoreItems(extractor!!)
        }

        companion object {
            private var extractor: PeertubeTrendingExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube.kioskList
                        .getExtractorById("Trending", null)
                extractor!!.fetchPage()
            }
        }
    }
}
