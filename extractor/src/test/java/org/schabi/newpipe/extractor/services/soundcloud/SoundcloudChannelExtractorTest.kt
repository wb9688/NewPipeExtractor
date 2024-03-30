package org.schabi.newpipe.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor

/**
 * Test for [SoundcloudChannelExtractor]
 */
class SoundcloudChannelExtractorTest {
    class LilUzi : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("Lil Uzi Vert", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("10494998", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/liluzivert", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("http://soundcloud.com/liluzivert/sets", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.description)
        }

        @Test
        override fun testAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.avatars)
        }

        @Test
        override fun testBanners() {
            DefaultTests.defaultTestImageCollection(extractor!!.banners)
        }

        @Test
        override fun testFeedUrl() {
            ExtractorAsserts.assertEmpty(extractor!!.feedUrl)
        }

        @Test
        override fun testSubscriberCount() {
            Assertions.assertTrue(extractor!!.subscriberCount >= 1e6, "Wrong subscriber count")
        }

        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.TRACKS, ChannelTabs.PLAYLISTS,
                    ChannelTabs.ALBUMS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: SoundcloudChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getChannelExtractor("http://soundcloud.com/liluzivert/sets")
                extractor!!.fetchPage()
            }
        }
    }

    class DubMatix : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("dubmatix", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("542134", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/dubmatix", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/dubmatix", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.description)
        }

        @Test
        override fun testAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.avatars)
        }

        @Test
        override fun testBanners() {
            DefaultTests.defaultTestImageCollection(extractor!!.banners)
        }

        @Test
        override fun testFeedUrl() {
            ExtractorAsserts.assertEmpty(extractor!!.feedUrl)
        }

        @Test
        override fun testSubscriberCount() {
            Assertions.assertTrue(extractor!!.subscriberCount >= 2e6, "Wrong subscriber count")
        }

        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.TRACKS, ChannelTabs.PLAYLISTS,
                    ChannelTabs.ALBUMS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: SoundcloudChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getChannelExtractor("https://soundcloud.com/dubmatix")
                extractor!!.fetchPage()
            }
        }
    }
}
