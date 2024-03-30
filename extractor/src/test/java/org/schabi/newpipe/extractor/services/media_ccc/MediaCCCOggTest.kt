package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache.size

/**
 * Test [MediaCCCStreamExtractor]
 */
class MediaCCCOggTest {
    @get:Throws(Exception::class)
    @get:Test
    val audioStreamsCount: Unit
        get() {
            assertEquals(1, extractor!!.audioStreams.size())
        }

    @get:Throws(Exception::class)
    @get:Test
    val audioStreamsContainOgg: Unit
        get() {
            for (stream in extractor!!.audioStreams) {
                Assertions.assertEquals("OGG", stream!!.format.toString())
            }
        }

    companion object {
        // test against https://media.ccc.de/public/events/1317
        private var extractor: StreamExtractor? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = MediaCCC.getStreamExtractor("https://media.ccc.de/public/events/1317")
            extractor!!.fetchPage()
        }
    }
}
