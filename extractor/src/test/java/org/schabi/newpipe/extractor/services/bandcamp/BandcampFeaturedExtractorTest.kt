// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.kiosk.KioskList.defaultKioskExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.BaseListExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.kioskList
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.kioskList
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.kioskList
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList
import java.io.IOException

/**
 * Tests for [BandcampFeaturedExtractor]
 */
class BandcampFeaturedExtractorTest : BaseListExtractorTest {
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testFeaturedCount() {
        val list: List<PlaylistInfoItem>? = extractor!!.initialPage!!.items
        Assertions.assertTrue(list!!.size > 5)
    }

    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testHttps() {
        val list: List<PlaylistInfoItem>? = extractor!!.initialPage!!.items
        Assertions.assertTrue(list!![0].url!!.contains("https://"))
    }

    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testMorePages() {
        val page2 = extractor!!.initialPage!!.nextPage
        val page3 = extractor!!.getPage(page2)!!.nextPage
        Assertions.assertTrue(extractor!!.getPage(page2)!!.items!!.size > 5)

        // Compare first item of second page with first item of third page
        Assertions.assertNotEquals(
                extractor!!.getPage(page2)!!.items!![0],
                extractor!!.getPage(page3)!!.items!![0]
        )
    }

    @Throws(Exception::class)
    override fun testRelatedItems() {
        DefaultTests.defaultTestRelatedItems(extractor!!)
    }

    @Throws(Exception::class)
    override fun testMoreRelatedItems() {
        // more items not implemented
    }

    override fun testServiceId() {
        Assertions.assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
    }

    @Throws(Exception::class)
    override fun testName() {
        Assertions.assertEquals("Featured", extractor!!.name)
    }

    override fun testId() {
        Assertions.assertEquals("", extractor!!.getId())
    }

    @Throws(Exception::class)
    override fun testUrl() {
        Assertions.assertEquals("", extractor!!.url)
    }

    @Throws(Exception::class)
    override fun testOriginalUrl() {
        Assertions.assertEquals("", extractor!!.originalUrl)
    }

    companion object {
        private var extractor: BandcampFeaturedExtractor? = null
        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = Bandcamp
                    .kioskList.defaultKioskExtractor
            extractor!!.fetchPage()
        }
    }
}
