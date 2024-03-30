package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.BeforeAll
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getChannelTabExtractorFromId
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelTabExtractor
import java.io.IOException

internal class BandcampChannelTabExtractorTest {
    internal class Tracks : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return Bandcamp
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.TRACKS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "2464198920"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://wintergatan.bandcamp.com/track"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://wintergatan.bandcamp.com/track"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: BandcampChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = Bandcamp
                        .getChannelTabExtractorFromId("2464198920", ChannelTabs.TRACKS)
                extractor!!.fetchPage()
            }
        }
    }

    internal class Albums : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return Bandcamp
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.ALBUMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "2450875064"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://toupie.bandcamp.com/album"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://toupie.bandcamp.com/album"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        companion object {
            private var extractor: BandcampChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = Bandcamp
                        .getChannelTabExtractorFromId("2450875064", ChannelTabs.ALBUMS)
                extractor!!.fetchPage()
            }
        }
    }
}