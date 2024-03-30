// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.kiosk.KioskList.getExtractorById
import org.schabi.newpipe.extractor.services.BaseListExtractorTest
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.kioskList
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.kioskList
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.kioskList
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException

/**
 * Tests for [BandcampRadioExtractor]
 */
class BandcampRadioExtractorTest : BaseListExtractorTest {
    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testRadioCount() {
        val list: List<StreamInfoItem>? = extractor!!.initialPage!!.items
        Assertions.assertTrue(list!!.size > 300)
    }

    @Test
    @Throws(Exception::class)
    override fun testRelatedItems() {
        // DefaultTests.defaultTestRelatedItems(extractor);
        // Would fail because BandcampRadioInfoItemExtractor.getUploaderName() returns an empty String
    }

    @Test
    @Throws(Exception::class)
    override fun testMoreRelatedItems() {
        // All items are on one page
    }

    @Test
    override fun testServiceId() {
        Assertions.assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
    }

    @Test
    @Throws(Exception::class)
    override fun testName() {
        Assertions.assertEquals("Radio", extractor!!.name)
    }

    @Test
    override fun testId() {
        Assertions.assertEquals("Radio", extractor!!.getId())
    }

    @Test
    @Throws(Exception::class)
    override fun testUrl() {
        Assertions.assertEquals("https://bandcamp.com/api/bcweekly/1/list", extractor!!.url)
    }

    @Test
    @Throws(Exception::class)
    override fun testOriginalUrl() {
        Assertions.assertEquals("https://bandcamp.com/api/bcweekly/1/list", extractor!!.originalUrl)
    }

    companion object {
        private var extractor: BandcampRadioExtractor? = null
        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = Bandcamp
                    .kioskList
                    .getExtractorById("Radio", null)
            extractor!!.fetchPage()
        }
    }
}
