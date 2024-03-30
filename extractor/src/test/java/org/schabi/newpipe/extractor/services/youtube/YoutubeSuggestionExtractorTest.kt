/*
 * Created by Christian Schabesberger on 18.11.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeSuggestionExtractorTest.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.suggestionExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.suggestionExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.suggestionExtractor
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import java.io.IOException

/**
 * Test for [YoutubeSuggestionExtractor]
 */
internal class YoutubeSuggestionExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testIfSuggestions() {
        Assertions.assertFalse(suggestionExtractor!!.suggestionList("hello").isEmpty())
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/suggestions/"
        private var suggestionExtractor: SuggestionExtractor? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeTestsUtils.ensureStateless()
            init(DownloaderFactory.getDownloader(RESOURCE_PATH), Localization("de", "DE"))
            suggestionExtractor = YouTube.suggestionExtractor
        }
    }
}
