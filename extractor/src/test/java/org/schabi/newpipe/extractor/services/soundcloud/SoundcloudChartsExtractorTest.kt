package org.schabi.newpipe.extractor.services.soundcloud

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
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChartsExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList

class SoundcloudChartsExtractorTest {
    class NewAndHot : BaseListExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("New & hot", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("New & hot", extractor!!.getId())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/new", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/new", extractor!!.originalUrl)
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
            private var extractor: SoundcloudChartsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.kioskList
                        .getExtractorById("New & hot", null)
                extractor!!.fetchPage()
            }
        }
    }

    class Top50Charts : BaseListExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("Top 50", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("Top 50", extractor!!.getId())
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/top", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/charts/top", extractor!!.originalUrl)
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
            private var extractor: SoundcloudChartsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud.kioskList
                        .getExtractorById("Top 50", null)
                extractor!!.fetchPage()
            }
        }
    }
}
