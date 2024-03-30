package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getChannelTabExtractorFromId
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache.size

/**
 * Test that it is possible to create and use a channel tab extractor ([ ]) without
 * passing through the conference extractor
 */
class MediaCCCChannelTabExtractorTest {
    class CCCamp2023 {
        @Test
        fun testName() {
            Assertions.assertEquals(ChannelTabs.VIDEOS, extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        fun testGetUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/camp2023", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        fun testGetOriginalUrl() {
            Assertions.assertEquals("https://media.ccc.de/c/camp2023", extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        fun testGetInitalPage() {
            assertEquals(177, extractor!!.initialPage.getItems().size())
        }

        companion object {
            private var extractor: ChannelTabExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUpClass() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = MediaCCC.getChannelTabExtractorFromId("camp2023", ChannelTabs.VIDEOS)
                extractor!!.fetchPage()
            }
        }
    }
}
