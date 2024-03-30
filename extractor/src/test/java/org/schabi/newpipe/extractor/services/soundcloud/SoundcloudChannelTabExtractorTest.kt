package org.schabi.newpipe.extractor.services.soundcloud

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getChannelTabExtractorFromId
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor
import java.io.IOException

internal class SoundcloudChannelTabExtractorTest {
    internal class Tracks : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.TRACKS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "10494998"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://soundcloud.com/liluzivert/tracks"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://soundcloud.com/liluzivert/tracks"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newTabExtractor: ChannelTabExtractor = SoundCloud.getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS)
            DefaultTests.defaultTestGetPageInNewExtractor(extractor, newTabExtractor)
        }

        companion object {
            private var extractor: SoundcloudChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getChannelTabExtractorFromId("10494998", ChannelTabs.TRACKS)
                extractor!!.fetchPage()
            }
        }
    }

    internal class Playlists : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.PLAYLISTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "323371733"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://soundcloud.com/trackaholic/sets"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://soundcloud.com/trackaholic/sets"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: SoundcloudChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getChannelTabExtractorFromId("323371733", ChannelTabs.PLAYLISTS)
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
            return SoundCloud
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.ALBUMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "4803918"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://soundcloud.com/bigsean-1/albums"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://soundcloud.com/bigsean-1/albums"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: SoundcloudChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getChannelTabExtractorFromId("4803918", ChannelTabs.ALBUMS)
                extractor!!.fetchPage()
            }
        }
    }
}
