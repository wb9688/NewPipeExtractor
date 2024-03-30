package org.schabi.newpipe.extractor.services.peertube

import org.junit.jupiter.api.BeforeAll
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getChannelTabExtractorFromId
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelTabExtractor

internal class PeertubeAccountTabExtractorTest {
    internal class Videos : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return PeerTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.VIDEOS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "accounts/framasoft"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://framatube.org/accounts/framasoft/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://framatube.org/accounts/framasoft/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: PeertubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube
                        .getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.VIDEOS)
                extractor!!.fetchPage()
            }
        }
    }

    internal class Channels : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return PeerTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.CHANNELS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "accounts/framasoft"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://framatube.org/accounts/framasoft/video-channels"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://framatube.org/accounts/framasoft/video-channels"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: PeertubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                // setting instance might break test when running in parallel
                PeerTube.instance = PeertubeInstance("https://framatube.org", "Framatube")
                extractor = PeerTube
                        .getChannelTabExtractorFromId("accounts/framasoft", ChannelTabs.CHANNELS)
                extractor!!.fetchPage()
            }
        }
    }
}
