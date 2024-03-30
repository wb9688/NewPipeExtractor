// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getChannelExtractor
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest

class BandcampChannelExtractorTest : BaseChannelExtractorTest {
    @Test
    @Throws(Exception::class)
    override fun testDescription() {
        assertEquals("making music:)", extractor!!.description)
    }

    @Test
    @Throws(Exception::class)
    override fun testAvatars() {
        BandcampTestUtils.testImages(extractor!!.avatars)
    }

    @Test
    @Throws(Exception::class)
    override fun testBanners() {
        BandcampTestUtils.testImages(extractor!!.banners)
    }

    @Test
    @Throws(Exception::class)
    override fun testFeedUrl() {
        Assertions.assertNull(extractor!!.feedUrl)
    }

    @Test
    @Throws(Exception::class)
    override fun testSubscriberCount() {
        assertEquals(-1, extractor!!.subscriberCount)
    }

    @Test
    @Throws(Exception::class)
    override fun testVerified() {
        assertFalse(extractor!!.isVerified)
    }

    @Test
    override fun testServiceId() {
        Assertions.assertEquals(Bandcamp.serviceId, extractor!!.serviceId)
    }

    @Test
    @Throws(Exception::class)
    override fun testName() {
        assertEquals("toupie", extractor!!.name)
    }

    @Test
    @Throws(Exception::class)
    override fun testId() {
        Assertions.assertEquals("2450875064", extractor!!.id)
    }

    @Test
    @Throws(Exception::class)
    override fun testUrl() {
        Assertions.assertEquals("https://toupie.bandcamp.com", extractor!!.url)
    }

    @Test
    @Throws(Exception::class)
    override fun testOriginalUrl() {
        Assertions.assertEquals("https://toupie.bandcamp.com", extractor!!.url)
    }

    @Test
    @Throws(Exception::class)
    override fun testTabs() {
        assertTabsContain(extractor!!.tabs, ChannelTabs.ALBUMS)
    }

    @Test
    @Throws(Exception::class)
    override fun testTags() {
        Assertions.assertTrue(extractor!!.tags.isEmpty())
    }

    companion object {
        private var extractor: ChannelExtractor? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = Bandcamp.getChannelExtractor("https://toupie.bandcamp.com/releases")
            extractor!!.fetchPage()
        }
    }
}
