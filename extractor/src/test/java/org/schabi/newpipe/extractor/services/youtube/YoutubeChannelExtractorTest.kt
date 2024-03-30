package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getChannelExtractor
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.BaseChannelExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabPlaylistExtractor
import org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty
import java.io.IOException

/**
 * Test for [ChannelExtractor]
 */
object YoutubeChannelExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/"

    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun deletedFetch() {
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCAUc4iz6edWerIjlnL8OSSw")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun nonExistentFetch() {
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/DOESNT-EXIST")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedTOSFetch() {
            // "This account has been terminated for a violation of YouTube's Terms of Service."
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCTGjY2I-ZUGnwVoWAGRd7XQ")
            val ex = Assertions.assertThrows(AccountTerminatedException::class.java) { extractor.fetchPage() }
            Assertions.assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedCommunityFetch() {
            // "This account has been terminated for violating YouTube's Community Guidelines."
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UC0AuOxCr9TZ0TtEgL1zpIgA")
            val ex = Assertions.assertThrows(AccountTerminatedException::class.java) { extractor.fetchPage() }
            Assertions.assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedHateFetch() {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting hate speech."
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCPWXIOPK-9myzek6jHR5yrg")
            val ex = Assertions.assertThrows(AccountTerminatedException::class.java) { extractor.fetchPage() }
            Assertions.assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedBullyFetch() {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy prohibiting content designed to harass, bully or threaten."
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://youtube.com/channel/UCB1o7_gbFp2PLsamWxFenBg")
            val ex = Assertions.assertThrows(AccountTerminatedException::class.java) { extractor.fetchPage() }
            Assertions.assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedSpamFetch() {
            // "This account has been terminated due to multiple or severe violations
            // of YouTube's policy against spam, deceptive practices and misleading content
            // or other Terms of Service violations."
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCoaO4U_p7G7AwalqSbGCZOA")
            val ex = Assertions.assertThrows(AccountTerminatedException::class.java) { extractor.fetchPage() }
            Assertions.assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        @Test
        @Throws(Exception::class)
        fun accountTerminatedCopyrightFetch() {
            // "This account has been terminated because we received multiple third-party claims
            // of copyright infringement regarding material that the user posted."
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://www.youtube.com/channel/UCI4i4RgFT5ilfMpna4Z_Y8w")
            val ex = Assertions.assertThrows(AccountTerminatedException::class.java) { extractor.fetchPage() }
            Assertions.assertEquals(AccountTerminatedException.Reason.VIOLATION, ex.reason)
        }

        companion object {
            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notAvailable"))
            }
        }
    }

    internal class SystemTopic {
        @Test
        @Throws(Exception::class)
        fun noSupportedTab() {
            val extractor: ChannelExtractor = YouTube.getChannelExtractor("https://invidio.us/channel/UC-9-kyTW8ZkZNDHQJ6FgpwQ")
            extractor.fetchPage()
            assertTrue(extractor.tabs.isEmpty())
        }

        companion object {
            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "systemTopic"))
            }
        }
    }

    class Gronkh : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("Gronkh", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCYJ61XIK64sp6ZFFS8sctxw", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCYJ61XIK64sp6ZFFS8sctxw", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("http://www.youtube.com/@Gronkh", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
         // ChannelExtractor
         ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            ExtractorAsserts.assertContains("Ungebremster Spieltrieb seit 1896.", extractor!!.description)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCYJ61XIK64sp6ZFFS8sctxw", extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(4900000, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS,
                    ChannelTabs.LIVESTREAMS, ChannelTabs.PLAYLISTS)
            Assertions.assertTrue(extractor!!.tabs.stream()
                    .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                    .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.contains("gronkh"))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "gronkh"))
                extractor = YouTube
                        .getChannelExtractor("http://www.youtube.com/@Gronkh")
                extractor!!.fetchPage()
            }
        }
    }

    // YouTube RED/Premium ad blocking test
    class VSauce : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("Vsauce", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/user/Vsauce", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            ExtractorAsserts.assertContains("Our World is Amazing. \n\nQuestions? Ideas? Tweet me:", extractor!!.description)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UC6nSFpj9HTCZ5t-N3Rm3-HA", extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(17000000, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.LIVESTREAMS,
                    ChannelTabs.SHORTS, ChannelTabs.PLAYLISTS)
            Assertions.assertTrue(extractor!!.tabs.stream()
                    .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                    .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.containsAll(listOf("questions", "education",
                    "learning", "schools", "Science")))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "VSauce"))
                extractor = YouTube
                        .getChannelExtractor("https://www.youtube.com/user/Vsauce")
                extractor!!.fetchPage()
            }
        }
    }

    class Kurzgesagt : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertTrue(extractor!!.name!!.startsWith("Kurzgesagt"))
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            ExtractorAsserts.assertContains("science", extractor!!.description)
            ExtractorAsserts.assertContains("animators", extractor!!.description)
            //TODO: Description get cuts out, because the og:description is optimized and don't have all the content
            //assertTrue(description, description.contains("Currently we make one animation video per month"));
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCsXVk37bltHxD1rDPwtNM8Q", extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(17000000, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.SHORTS,
                    ChannelTabs.PLAYLISTS)
            Assertions.assertTrue(extractor!!.tabs.stream()
                    .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                    .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.containsAll(listOf("universe", "Science",
                    "black hole", "humanism", "evolution")))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "kurzgesagt"))
                extractor = YouTube
                        .getChannelExtractor("https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q")
                extractor!!.fetchPage()
            }
        }
    }

    class KurzgesagtAdditional {
        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newExtractor: ChannelExtractor = YouTube.getChannelExtractor(extractor!!.url)
            newExtractor.fetchPage()
            val newTabExtractor: ChannelTabExtractor = YouTube.getChannelTabExtractor(
                    newExtractor.tabs[0])
            DefaultTests.defaultTestGetPageInNewExtractor(tabExtractor, newTabExtractor)
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            private var tabExtractor: ChannelTabExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                // Test is not deterministic, mocks can't be used
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = YouTube.getChannelExtractor(
                        "https://www.youtube.com/channel/UCsXVk37bltHxD1rDPwtNM8Q")
                extractor!!.fetchPage()
                tabExtractor = YouTube.getChannelTabExtractor(extractor!!.tabs[0])
                tabExtractor!!.fetchPage()
            }
        }
    }

    class CaptainDisillusion : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("Captain Disillusion", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCEOXxzW2vU0P-0THehuIIeg", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/user/CaptainDisillusion/videos", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            ExtractorAsserts.assertContains("In a world where", extractor!!.description)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEOXxzW2vU0P-0THehuIIeg", extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(2000000, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS, ChannelTabs.PLAYLISTS)
            Assertions.assertTrue(extractor!!.tabs.stream()
                    .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                    .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.containsAll(listOf("critical thinking",
                    "visual effects", "VFX", "sci-fi", "humor")))
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "captainDisillusion"))
                extractor = YouTube
                        .getChannelExtractor("https://www.youtube.com/user/CaptainDisillusion/videos")
                extractor!!.fetchPage()
            }
        }
    }

    class RandomChannel : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("random channel", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ChannelExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            ExtractorAsserts.assertContains("Hey there iu will upoload a load of pranks onto this channel", extractor!!.description)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCUaQMQS9lY5lit3vurpXQ6w", extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(50, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertFalse(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertTabsContain(extractor!!.tabs, ChannelTabs.VIDEOS)
            Assertions.assertTrue(extractor!!.tabs.stream()
                    .filter { it: ListLinkHandler -> ChannelTabs.VIDEOS == it.contentFilters[0] }
                    .allMatch { o: ListLinkHandler? -> ReadyChannelTabListLinkHandler::class.java.isInstance(o) })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "random"))
                extractor = YouTube
                        .getChannelExtractor("https://www.youtube.com/channel/UCUaQMQS9lY5lit3vurpXQ6w")
                extractor!!.fetchPage()
            }
        }
    }

    class CarouselHeader : BaseChannelExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertEquals("Sports", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.originalUrl)
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
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        override fun testBanners() {
            // A CarouselHeaderRenderer doesn't contain a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            Assertions.assertEquals("https://www.youtube.com/feeds/videos.xml?channel_id=UCEgdi0XIXXZ-qJOFPf4JSKw", extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            assertGreaterOrEqual(70000000, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            Assertions.assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            ExtractorAsserts.assertEmpty(extractor!!.tabs)
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            ExtractorAsserts.assertEmpty(extractor!!.tags)
        }

        companion object {
            private var extractor: YoutubeChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "carouselHeader"))
                extractor = YouTube
                        .getChannelExtractor("https://www.youtube.com/channel/UCEgdi0XIXXZ-qJOFPf4JSKw")
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * A YouTube channel which is age-restricted and requires login to view its contents on a
     * channel page.
     *
     *
     *
     * Note that age-restrictions on channels may not apply for countries, so check that the
     * channel is age-restricted in the network you use to update the test's mocks before updating
     * them.
     *
     */
    internal class AgeRestrictedChannel : BaseChannelExtractorTest {
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            // Description cannot be extracted from age-restricted channels
            Assertions.assertTrue(isNullOrEmpty(extractor!!.description))
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            // Banners cannot be extracted from age-restricted channels
            assertEmpty(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals(
                    "https://www.youtube.com/feeds/videos.xml?channel_id=UCbfnHqxXs_K3kvaH-WlNlig",
                    extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            // Subscriber count cannot be extracted from age-restricted channels
            assertEquals(ChannelExtractor.UNKNOWN_SUBSCRIBER_COUNT, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Laphroaig Whisky", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCbfnHqxXs_K3kvaH-WlNlig", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig",
                    extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig",
                    extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            // Verification status cannot be extracted from age-restricted channels
            assertFalse(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            // Channel tabs which may be available and which will be extracted from channel system
            // uploads playlists
            ExtractorAsserts.assertTabsContain(extractor!!.tabs,
                    ChannelTabs.VIDEOS, ChannelTabs.SHORTS, ChannelTabs.LIVESTREAMS)

            // Check if all tabs are not classic tabs, so that link handlers are of the appropriate
            // type and build YoutubeChannelTabPlaylistExtractor instances
            assertTrue(extractor!!.tabs
                    .stream()
                    .allMatch { linkHandler ->
                        (linkHandler.getClass() === ReadyChannelTabListLinkHandler::class.java
                                && (linkHandler as ReadyChannelTabListLinkHandler)
                                .getChannelTabExtractor(extractor!!.service)
                                .javaClass == YoutubeChannelTabPlaylistExtractor::class.java)
                    })
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            // Tags cannot be extracted from age-restricted channels
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        companion object {
            private var extractor: ChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ageRestricted"))
                extractor = YouTube.getChannelExtractor(
                        "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig")
                extractor!!.fetchPage()
            }
        }
    }

    internal class InteractiveTabbedHeader : BaseChannelExtractorTest {
        @Test
        @Throws(Exception::class)
        override fun testDescription() {
            // The description changes frequently and there is no significant common word, so only
            // check if it is not empty
            assertNotEmpty(extractor!!.description)
        }

        @Test
        @Throws(Exception::class)
        override fun testAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testFeedUrl() {
            assertEquals(
                    "https://www.youtube.com/feeds/videos.xml?channel_id=UCQvWX73GQygcwXOTSf_VDVg",
                    extractor!!.feedUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testSubscriberCount() {
            // Subscriber count is not available on channels with an interactiveTabbedHeaderRenderer
            assertEquals(ChannelExtractor.UNKNOWN_SUBSCRIBER_COUNT, extractor!!.subscriberCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testVerified() {
            assertTrue(extractor!!.isVerified)
        }

        @Test
        @Throws(Exception::class)
        override fun testTabs() {
            // Gaming topic channels tabs are not yet supported, so an empty list should be returned
            assertTrue(extractor!!.tabs.isEmpty())
        }

        @Test
        @Throws(Exception::class)
        override fun testTags() {
            Assertions.assertTrue(extractor!!.tags.isEmpty())
        }

        @Test
        @Throws(Exception::class)
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            ExtractorAsserts.assertContains("Minecraft", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UCQvWX73GQygcwXOTSf_VDVg", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg",
                    extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg",
                    extractor!!.originalUrl)
        }

        companion object {
            private var extractor: ChannelExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "interactiveTabbedHeader"))
                extractor = YouTube.getChannelExtractor(
                        "https://www.youtube.com/channel/UCQvWX73GQygcwXOTSf_VDVg")
                extractor!!.fetchPage()
            }
        }
    }
}
