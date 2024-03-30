// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.suggestionExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampSuggestionExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.suggestionExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.suggestionExtractor
import java.io.IOException

/**
 * Tests for [BandcampSuggestionExtractor]
 */
class BandcampSuggestionExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testSearchExample() {
        val c418 = extractor!!.suggestionList("c418")
        Assertions.assertTrue(c418.contains("C418"))

        // There should be five results, but we can't be sure of that forever
        Assertions.assertTrue(c418.size > 2)
    }

    companion object {
        private var extractor: BandcampSuggestionExtractor? = null
        @BeforeAll
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = Bandcamp.suggestionExtractor
        }
    }
}
