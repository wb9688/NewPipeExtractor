package org.schabi.newpipe.extractor.services.youtube.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getSearchExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.DefaultSearchExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.utils.Utils.encodeUrlUtf8
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

object YoutubeSearchExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/search/"

    class All : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "test"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "all"))
                extractor = YouTube.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    class Channel : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "test"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channel"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.CHANNELS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class Playlists : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.PLAYLIST
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "test"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "playlist"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.PLAYLISTS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class Videos : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "test"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "videos"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * Test for YT's "Did you mean...".
     *
     *
     *
     * Hint: YT mostly shows "did you mean..." when you are searching in another language.
     *
     */
    class Suggestion : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + encodeUrlUtf8(QUERY)
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + encodeUrlUtf8(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return EXPECTED_SUGGESTION
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "on board ing"
            private const val EXPECTED_SUGGESTION = "on boarding"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "suggestions"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * Test for YT's "Showing results for...".
     */
    class CorrectedSearch : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return EXPECTED_SUGGESTION
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        override val isCorrectedSearch: Boolean
            get() = true

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "pewdeipie"
            private const val EXPECTED_SUGGESTION = "pewdiepie"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "corrected"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class RandomQueryNoMorePages : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Test Overrides
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            val initialPage = extractor()!!.initialPage
            // YouTube actually gives us an empty next page, but after that, no more pages.
            Assertions.assertTrue(initialPage!!.hasNextPage())
            val nextEmptyPage = extractor!!.getPage(initialPage.nextPage)
            Assertions.assertEquals(0, nextEmptyPage!!.items!!.size)
            ExtractorAsserts.assertEmptyErrors("Empty page has errors", nextEmptyPage.errors)
            Assertions.assertFalse(nextEmptyPage.hasNextPage(), "More items available when it shouldn't")
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "UCO6AK"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "random"))
                extractor = YouTube.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }

    internal class PagingTest {
        @Test
        @Throws(Exception::class)
        fun duplicatedItemsCheck() {
            YoutubeTestsUtils.ensureStateless()
            init(DownloaderFactory.getDownloader(RESOURCE_PATH + "paging"))
            val extractor: SearchExtractor = YouTube.getSearchExtractor("cirque du soleil", listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
            extractor.fetchPage()
            val page1 = extractor.initialPage
            val page2 = extractor.getPage(page1!!.nextPage)
            DefaultTests.assertNoDuplicatedItems(YouTube, page1, page2)
        }
    }

    class MetaInfoTest : DefaultSearchExtractorTest() {
        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        @Throws(MalformedURLException::class)
        override fun expectedMetaInfo(): List<MetaInfo> {
            return listOf(MetaInfo(
                    "COVID-19",
                    Description(
                            "Get the latest information from the WHO about coronavirus.",
                            Description.PLAIN_TEXT), listOf(
                    URL("https://www.who.int/emergencies/diseases/novel-coronavirus-2019")), listOf("Learn more")))
        }

        // testMoreRelatedItems is broken because a video has no duration shown
        @Test
        override fun testMoreRelatedItems() {
        }

        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "Covid"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "metaInfo"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class ChannelVerified : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.CHANNEL
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testAtLeastOneVerified() {
            val items: List<InfoItem> = extractor!!.initialPage.getItems()
            var verified = false
            for (item in items) {
                if ((item as ChannelInfoItem).isVerified) {
                    verified = true
                    break
                }
            }
            Assertions.assertTrue(verified)
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "bbc"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "verified"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.CHANNELS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class VideoUploaderAvatar : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testUploaderAvatars() {
            extractor!!.initialPage
                    .getItems()
                    .stream()
                    .filter { o: Any? -> StreamInfoItem::class.java.isInstance(o) }
                    .map { obj: Any? -> StreamInfoItem::class.java.cast(obj) }
                    .forEach { streamInfoItem -> YoutubeTestsUtils.testImages(streamInfoItem.uploaderAvatars) }
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "sidemen"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "video_uploader_avatar"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class VideoDescription : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + QUERY
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testVideoDescription() {
            val items: List<InfoItem> = extractor!!.initialPage.getItems()
            Assertions.assertNotNull((items[0] as StreamInfoItem).shortDescription)
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "44wLAzydRFU"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "video_description"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    class ShortFormContent : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + encodeUrlUtf8(QUERY)
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + encodeUrlUtf8(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        override fun expectedInfoItemType(): InfoType? {
            return InfoType.STREAM
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testShortFormContent() {
            assertTrue(extractor!!.initialPage
                    .getItems()
                    .stream()
                    .filter { o: Any? -> StreamInfoItem::class.java.isInstance(o) }
                    .map { obj: Any? -> StreamInfoItem::class.java.cast(obj) }
                    .anyMatch(StreamInfoItem::isShortFormContent))
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "#shorts"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shorts"))
                extractor = YouTube.getSearchExtractor(QUERY, listOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS), "")
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * A [SearchExtractor] test to check if crisis resources preventing search results to be
     * returned are bypassed (searches with content filters are not tested in this test, even if
     * they should work as bypasses are used with them too).
     *
     *
     *
     * See [
 * https://support.google.com/youtube/answer/10726080?hl=en](https://support.google.com/youtube/answer/10726080?hl=en) for more info on crisis
     * resources.
     *
     */
    class CrisisResources : DefaultSearchExtractorTest() {
        override fun extractor(): SearchExtractor? {
            return extractor
        }

        override fun expectedService(): StreamingService {
            return YouTube
        }

        override fun expectedName(): String {
            return QUERY
        }

        override fun expectedId(): String {
            return QUERY
        }

        @Throws(Exception::class)
        override fun expectedUrlContains(): String {
            return "youtube.com/results?search_query=" + encodeUrlUtf8(QUERY)
        }

        @Throws(Exception::class)
        override fun expectedOriginalUrlContains(): String {
            return "youtube.com/results?search_query=" + encodeUrlUtf8(QUERY)
        }

        override fun expectedSearchString(): String {
            return QUERY
        }

        override fun expectedSearchSuggestion(): String? {
            return null
        }

        @Test
        @Throws(Exception::class)
        override fun testMetaInfo() {
            val metaInfoList = extractor()!!.metaInfo

            // the meta info will have different text and language depending on where in the world
            // the connection is established from, so we can't check the actual content
            Assertions.assertEquals(1, metaInfoList!!.size)
        }

        companion object {
            private var extractor: SearchExtractor? = null
            private const val QUERY = "blue whale"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "crisis_resources"))
                extractor = YouTube.getSearchExtractor(QUERY)
                extractor!!.fetchPage()
            }
        }
    }
}
