package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getChannelExtractor
import org.schabi.newpipe.extractor.StreamingService.getChannelTabExtractorFromId
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor
import java.io.IOException

internal object YoutubeChannelTabExtractorTest {
    private const val RESOURCE_PATH = (DownloaderFactory.RESOURCE_PATH
            + "services/youtube/extractor/channelTabs/")

    internal class Videos : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.VIDEOS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UCTwECeGqMZee77BjdoYtI2Q"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UCTwECeGqMZee77BjdoYtI2Q/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/user/creativecommons/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "videos"))
                extractor = YouTube.getChannelTabExtractorFromId(
                        "user/creativecommons", ChannelTabs.VIDEOS)
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
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.PLAYLISTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UC2DjFE7Xf11URZqWBigcVOQ"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UC2DjFE7Xf11URZqWBigcVOQ/playlists"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/@EEVblog/playlists"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlists"))
                extractor = YouTube.getChannelTabExtractorFromId(
                        "@EEVblog", ChannelTabs.PLAYLISTS)
                extractor!!.fetchPage()
            }
        }
    }

    internal class Livestreams : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.LIVESTREAMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UCR-DXc1voovS8nhAvccRZhg"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UCR-DXc1voovS8nhAvccRZhg/streams"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/c/JeffGeerling/streams"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "livestreams"))
                extractor = YouTube.getChannelTabExtractorFromId(
                        "c/JeffGeerling", ChannelTabs.LIVESTREAMS)
                extractor!!.fetchPage()
            }
        }
    }

    internal class Shorts : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.SHORTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UCh8gHdtzO2tXd593_bjErWg"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/channel/UCh8gHdtzO2tXd593_bjErWg/shorts"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shorts"))
                extractor = YouTube.getChannelTabExtractorFromId(
                        "channel/UCh8gHdtzO2tXd593_bjErWg", ChannelTabs.SHORTS)
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
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.ALBUMS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UCq19-LqvG35A-30oyAiPiqA"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UCq19-LqvG35A-30oyAiPiqA/releases"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/@Radiohead/releases"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: YoutubeChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "albums"))
                extractor = YouTube.getChannelTabExtractorFromId(
                        "@Radiohead", ChannelTabs.ALBUMS)
                extractor!!.fetchPage()
            }
        }
    }

    // TESTS FOR TABS OF AGE RESTRICTED CHANNELS
    // Fetching the tabs individually would use the standard tabs without fallback to
    // system playlists for stream tabs, we need to fetch the channel extractor to get the
    // channel playlist tabs
    // TODO: implement system playlists fallback in YoutubeChannelTabExtractor for stream
    //  tabs
    internal class AgeRestrictedTabsVideos : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.VIDEOS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UCbfnHqxXs_K3kvaH-WlNlig"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/videos"
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override fun expectedHasMoreItems(): Boolean {
            return true
        }

        companion object {
            private var extractor: ChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ageRestrictedTabsVideos"))
                val channelExtractor: ChannelExtractor = YouTube.getChannelExtractor(
                        "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig")
                channelExtractor.fetchPage()

                // the videos tab is the first one
                extractor = YouTube.getChannelTabExtractor(channelExtractor.tabs[0])
                extractor!!.fetchPage()
            }
        }
    }

    internal class AgeRestrictedTabsShorts : DefaultListExtractorTest<ChannelTabExtractor?>() {
        @Throws(Exception::class)
        override fun extractor(): ChannelTabExtractor? {
            return extractor
        }

        @Throws(Exception::class)
        override fun expectedService(): StreamingService {
            return YouTube
        }

        @Throws(Exception::class)
        override fun expectedName(): String {
            return ChannelTabs.SHORTS
        }

        @Throws(Exception::class)
        override fun expectedId(): String {
            return "UCbfnHqxXs_K3kvaH-WlNlig"
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig/shorts"
        }

        override fun expectedHasMoreItems(): Boolean {
            return false
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            // this channel has no shorts, so an empty page is returned by the playlist extractor
            assertTrue(extractor!!.initialPage.getItems().isEmpty())
            assertTrue(extractor!!.initialPage!!.errors!!.isEmpty())
        }

        companion object {
            private var extractor: ChannelTabExtractor? = null
            @BeforeAll
            @Throws(IOException::class, ExtractionException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ageRestrictedTabsShorts"))
                val channelExtractor: ChannelExtractor = YouTube.getChannelExtractor(
                        "https://www.youtube.com/channel/UCbfnHqxXs_K3kvaH-WlNlig")
                channelExtractor.fetchPage()

                // the shorts tab is the second one
                extractor = YouTube.getChannelTabExtractor(channelExtractor.tabs[1])
                extractor!!.fetchPage()
            }
        }
    }
}
