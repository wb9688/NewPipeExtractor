// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.searchQHFactory
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.searchQHFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.searchQHFactory

class BandcampSearchQueryHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun testEncoding() {
        // Note: this isn't exactly as bandcamp does it (it wouldn't encode '!'), but both works
        Assertions.assertEquals("https://bandcamp.com/search?q=hello%21%22%C2%A7%24%25%26%2F%28%29%3D&page=1", searchQuery!!.getUrl("hello!\"§$%&/()="))
        // Note: bandcamp uses %20 instead of '+', but both works
        Assertions.assertEquals("https://bandcamp.com/search?q=search+query+with+spaces&page=1", searchQuery!!.getUrl("search query with spaces"))
    }

    companion object {
        var searchQuery: BandcampSearchQueryHandlerFactory? = null
        @BeforeAll
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            searchQuery = Bandcamp
                    .searchQHFactory
        }
    }
}
