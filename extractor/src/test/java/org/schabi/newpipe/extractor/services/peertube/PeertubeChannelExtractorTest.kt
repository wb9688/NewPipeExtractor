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
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor

/**
 * Test for [PeertubeChannelExtractor]
 */
class PeertubeChannelExtractorTest {
    class LaQuadratureDuNet : BaseChannelExtractorTest {
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
            Assertions.assertEquals("La Quadrature du Net", extractor!!.name)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("video-channels/lqdn_channel@video.lqdn.fr", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.description)
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelName() {
            Assertions.assertEquals("lqdn", extractor!!.parentChannelName)
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelUrl() {
            Assertions.assertEquals("https://video.lqdn.fr/accounts/lqdn", extractor!!.parentChannelUrl)
        }

        @Test
        fun testParentChannelAvatarUrl() {
            DefaultTests.defaultTestImageCollection(extractor!!.parentChannelAvatars)
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
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=1126", extractor!!.feedUrl)
        }

        @Test
        override fun testSubscriberCount() {
            assertGreaterOrEqual(230, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: PeertubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube
                        .getChannelExtractor("https://framatube.org/video-channels/lqdn_channel@video.lqdn.fr/videos")
                extractor!!.fetchPage()
            }
        }
    }

    class ChatSceptique : BaseChannelExtractorTest {
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
            Assertions.assertEquals("Chat Sceptique", extractor!!.name)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testId() {
            Assertions.assertEquals("video-channels/chatsceptique@skeptikon.fr", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://framatube.org/video-channels/chatsceptique@skeptikon.fr", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testDescription() {
            Assertions.assertNotNull(extractor!!.description)
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelName() {
            Assertions.assertEquals("nathan", extractor!!.parentChannelName)
        }

        @Test
        @Throws(ParsingException::class)
        fun testParentChannelUrl() {
            Assertions.assertEquals("https://skeptikon.fr/accounts/nathan", extractor!!.parentChannelUrl)
        }

        @Test
        fun testParentChannelAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.parentChannelAvatars)
        }

        @Test
        override fun testAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.avatars)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://framatube.org/feeds/videos.xml?videoChannelId=137", extractor!!.feedUrl)
        }

        @Test
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
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: PeertubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube
                        .getChannelExtractor("https://framatube.org/api/v1/video-channels/chatsceptique@skeptikon.fr")
                extractor!!.fetchPage()
            }
        }
    }
}
