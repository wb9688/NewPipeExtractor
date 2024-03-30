package org.schabi.newpipe.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.suggestionExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.suggestionExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.suggestionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.io.IOException

/**
 * Test for [SuggestionExtractor]
 */
class SoundcloudSuggestionExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testIfSuggestions() {
        Assertions.assertFalse(suggestionExtractor!!.suggestionList("lil uzi vert").isEmpty())
    }

    companion object {
        private var suggestionExtractor: SuggestionExtractor? = null
        @BeforeAll
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            suggestionExtractor = SoundCloud.suggestionExtractor
        }
    }
}
