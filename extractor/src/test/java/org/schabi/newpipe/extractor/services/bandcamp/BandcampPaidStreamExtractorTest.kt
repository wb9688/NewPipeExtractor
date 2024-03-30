package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getStreamExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.PaidContentException
import org.schabi.newpipe.extractor.stream.StreamExtractor

class BandcampPaidStreamExtractorTest {
    @Test
    @Throws(ExtractionException::class)
    fun testPaidTrack() {
        val extractor: StreamExtractor = Bandcamp.getStreamExtractor("https://radicaldreamland.bandcamp.com/track/hackmud-continuous-mix")
        Assertions.assertThrows(PaidContentException::class.java) { extractor.fetchPage() }
    }

    companion object {
        @BeforeAll
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}
