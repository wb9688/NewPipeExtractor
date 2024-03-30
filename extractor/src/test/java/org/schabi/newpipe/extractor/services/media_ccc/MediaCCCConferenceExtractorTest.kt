package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.getChannelTabExtractor
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache.size

/**
 * Test [MediaCCCConferenceExtractor] and [ ]
 */
class MediaCCCConferenceExtractorTest {
    class FrOSCon2017 {
        @Test
        @Throws(Exception::class)
        fun testName() {
            Assertions.assertEquals("FrOSCon 2017", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        fun testGetUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/froscon2017", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        fun testGetOriginalUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/froscon2017", extractor!!.originalUrl)
        }

        @Test
        fun testGetThumbnails() {
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                    "https://static.media.ccc.de/media/events/froscon/2017/logo.png",
                    extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        fun testGetInitalPage() {
            assertEquals(97, tabExtractor!!.initialPage.getItems().size())
        }

        companion object {
            private var extractor: MediaCCCConferenceExtractor? = null
            private var tabExtractor: ChannelTabExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUpClass() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = MediaCCC.getChannelExtractor("https://media.ccc.de/c/froscon2017")
                extractor!!.fetchPage()
                tabExtractor = MediaCCC.getChannelTabExtractor(extractor!!.tabs[0])
                tabExtractor!!.fetchPage()
            }
        }
    }

    class Oscal2019 {
        @Test
        @Throws(Exception::class)
        fun testName() {
            Assertions.assertEquals("Open Source Conference Albania 2019", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        fun testGetUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/oscal19", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        fun testGetOriginalUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/oscal19", extractor!!.originalUrl)
        }

        @Test
        fun testGetThumbnailUrl() {
            ExtractorAsserts.assertContainsImageUrlInImageCollection(
                    "https://static.media.ccc.de/media/events/oscal/2019/oscal-19.png",
                    extractor!!.avatars)
        }

        @Test
        @Throws(Exception::class)
        fun testGetInitalPage() {
            Assertions.assertTrue(tabExtractor!!.initialPage.getItems().size() >= 21)
        }

        companion object {
            private var extractor: MediaCCCConferenceExtractor? = null
            private var tabExtractor: ChannelTabExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUpClass() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = MediaCCC.getChannelExtractor("https://media.ccc.de/c/oscal19")
                extractor!!.fetchPage()
                tabExtractor = MediaCCC.getChannelTabExtractor(extractor!!.tabs[0])
                tabExtractor!!.fetchPage()
            }
        }
    }
}
