package org.schabi.newpipe.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getPlaylistExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor

class PeertubePlaylistExtractorTest {
    class Shocking {
        @Test
        @Throws(ParsingException::class)
        fun testGetName() {
            Assertions.assertEquals("Shocking !", extractor!!.name)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetThumbnails() {
            DefaultTests.defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        fun testGetUploaderUrl() {
            Assertions.assertEquals("https://skeptikon.fr/accounts/metadechoc", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetUploaderAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        fun testGetUploaderName() {
            Assertions.assertEquals("Méta de Choc", extractor!!.uploaderName)
        }

        @Test
        fun testGetStreamCount() {
            assertGreaterOrEqual(39, extractor!!.streamCount)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetDescription() {
            ExtractorAsserts.assertContains("épisodes de Shocking", extractor!!.description.content)
        }

        @Test
        fun testGetSubChannelUrl() {
            Assertions.assertEquals("https://skeptikon.fr/video-channels/metadechoc_channel", extractor!!.subChannelUrl)
        }

        @Test
        fun testGetSubChannelName() {
            Assertions.assertEquals("SHOCKING !", extractor!!.subChannelName)
        }

        @Test
        @Throws(ParsingException::class)
        fun testGetSubChannelAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.subChannelAvatars)
        }

        companion object {
            private var extractor: PeertubePlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = PeerTube.getPlaylistExtractor(
                        "https://framatube.org/videos/watch/playlist/96b0ee2b-a5a7-4794-8769-58d8ccb79ab7")
                extractor!!.fetchPage()
            }
        }
    }
}
