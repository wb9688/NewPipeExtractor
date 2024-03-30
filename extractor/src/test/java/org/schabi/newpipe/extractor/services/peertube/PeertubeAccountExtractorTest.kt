package org.schabi.newpipe.extractor.services.peertube

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
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountExtractor

/**
 * Test for [PeertubeAccountExtractor]
 */
class PeertubeAccountExtractorTest {
    class Framasoft : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            Assertions.assertEquals("Framasoft", extractor!!.name)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("accounts/framasoft", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/accounts/framasoft", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/accounts/framasoft", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(ParsingException::class)
        override fun testDescription() {
            Assertions.assertNull(extractor!!.description)
        }

        @Test
        override fun testAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.avatars)
        }

        @Test
        override fun testBanners() {
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?accountId=3", extractor!!.feedUrl)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(700, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.CHANNELS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: PeertubeAccountExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube
                        .getChannelExtractor("https://framatube.org/accounts/framasoft")
                extractor!!.fetchPage()
            }
        }
    }

    class FreeSoftwareFoundation : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(PeerTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testName() {
            Assertions.assertEquals("Free Software Foundation", extractor!!.name)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("accounts/fsf", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/accounts/fsf", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/accounts/fsf", extractor!!.originalUrl)
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
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?accountId=8178", extractor!!.feedUrl)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(100, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.CHANNELS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: PeertubeAccountExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube
                        .getChannelExtractor("https://framatube.org/api/v1/accounts/fsf")
                extractor!!.fetchPage()
            }
        }
    }
}
