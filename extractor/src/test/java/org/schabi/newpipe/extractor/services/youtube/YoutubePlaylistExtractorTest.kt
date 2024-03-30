package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getPlaylistExtractor
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor
import org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty
import java.io.IOException

/**
 * Test for [YoutubePlaylistExtractor]
 */
object YoutubePlaylistExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/playlist/"

    class NotAvailable {
        @Test
        @Throws(Exception::class)
        fun nonExistentFetch() {
            val extractor: PlaylistExtractor = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=PL11111111111111111111111111111111")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        @Test
        @Throws(Exception::class)
        fun invalidId() {
            val extractor: PlaylistExtractor = YouTube.getPlaylistExtractor("https://www.youtube.com/playlist?list=INVALID_ID")
            Assertions.assertThrows(ContentNotAvailableException::class.java) { extractor.fetchPage() }
        }

        companion object {
            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notAvailable"))
            }
        }
    }

    class TimelessPopHits : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertTrue(extractor!!.name!!.startsWith("Pop Music Playlist"))
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/playlist?list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            DefaultTests.defaultTestMoreItems(extractor!!)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        fun testUploaderUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCs72iRpTEuwV3y6pdWYLgiw", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            val uploaderName = extractor!!.uploaderName
            ExtractorAsserts.assertContains("Just Hits", uploaderName)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            assertGreater(100, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.NORMAL, extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            ExtractorAsserts.assertContains("pop songs list", description.content)
        }

        companion object {
            private var extractor: YoutubePlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "TimelessPopHits"))
                extractor = YouTube
                        .getPlaylistExtractor("http://www.youtube.com/watch?v=lp-EO5I60KA&list=PLMC9KNkIncKtPzgY-5rmhvj7fax8fdxoj")
                extractor!!.fetchPage()
            }
        }
    }

    class HugePlaylist : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newExtractor: PlaylistExtractor = YouTube.getPlaylistExtractor(extractor!!.url)
            DefaultTests.defaultTestGetPageInNewExtractor(extractor, newExtractor)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            val name = extractor!!.name
            Assertions.assertEquals("I Wanna Rock Super Gigantic Playlist 1: Hardrock, AOR, Metal and more !!! 5000 music videos !!!", name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/playlist?list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            var currentPage = DefaultTests.defaultTestMoreItems(extractor!!)

            // test for 2 more levels
            for (i in 0..1) {
                currentPage = extractor!!.getPage(currentPage!!.nextPage)
                DefaultTests.defaultTestListOfItems(YouTube, currentPage!!.items, currentPage.errors)
            }
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        fun testUploaderUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCHSPWoY1J5fbDVbcnyeqwdw", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            Assertions.assertEquals("Tomas Nilsson TOMPA571", extractor!!.uploaderName)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            assertGreater(100, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.NORMAL, extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            ExtractorAsserts.assertContains("I Wanna Rock Super Gigantic Playlist", description.content)
        }

        companion object {
            private var extractor: YoutubePlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "huge"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=8SbUC-UaAxE&list=PLWwAypAcFRgKAIIFqBr9oy-ZYZnixa_Fj")
                extractor!!.fetchPage()
            }
        }
    }

    class LearningPlaylist : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            Assertions.assertTrue(extractor!!.name!!.startsWith("Anatomy & Physiology"))
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor!!.id)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor!!.url)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8", extractor!!.originalUrl)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // ListExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            Assertions.assertFalse(extractor!!.initialPage!!.hasNextPage())
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        fun testUploaderUrl() {
            Assertions.assertEquals("https://www.youtube.com/channel/UCX6b17PVsYBQ0ip5gyeme-Q", extractor!!.uploaderUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            val uploaderName = extractor!!.uploaderName
            ExtractorAsserts.assertContains("CrashCourse", uploaderName)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            assertGreater(40, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.NORMAL, extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            val description = extractor!!.description
            ExtractorAsserts.assertContains("47 episodes", description.content)
        }

        companion object {
            private var extractor: YoutubePlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "learning"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/playlist?list=PL8dPuuaLjXtOAKed_MxxWBNaPno5h3Zs8")
                extractor!!.fetchPage()
            }
        }
    }

    internal class ShortsUI : BasePlaylistExtractorTest {
        @Test
        @Throws(Exception::class)
        override fun testServiceId() {
            Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
        }

        @Test
        @Throws(Exception::class)
        override fun testName() {
            assertEquals("Short videos", extractor!!.name)
        }

        @Test
        @Throws(Exception::class)
        override fun testId() {
            Assertions.assertEquals("UUSHBR8-60-B28hp2BmDPdntcQ", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ",
                    extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ",
                    extractor!!.originalUrl)
        }

        @Test
        @Throws(Exception::class)
        override fun testRelatedItems() {
            DefaultTests.defaultTestRelatedItems(extractor!!)
        }

        // TODO: enable test when continuations are available
        @Disabled("Shorts UI doesn't return any continuation, even if when there are more than 100 "
                + "items: this is a bug on YouTube's side, which is not related to the requirement "
                + "of a valid visitorData like it is for Shorts channel tab")
        @Test
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            DefaultTests.defaultTestMoreItems(extractor!!)
        }

        @Test
        @Throws(Exception::class)
        override fun testThumbnails() {
            YoutubeTestsUtils.testImages(extractor!!.thumbnails)
        }

        @Test
        @Throws(Exception::class)
        override fun testBanners() {
            YoutubeTestsUtils.testImages(extractor!!.banners)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderName() {
            assertEquals("YouTube", extractor!!.uploaderName)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderAvatars() {
            YoutubeTestsUtils.testImages(extractor!!.uploaderAvatars)
        }

        @Test
        @Throws(Exception::class)
        override fun testStreamCount() {
            assertGreater(250, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            // YouTube doesn't provide this information for playlists
            assertFalse(extractor!!.isUploaderVerified)
        }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.NORMAL, extractor!!.playlistType)
            }

        @Test
        @Throws(ParsingException::class)
        fun testDescription() {
            Assertions.assertTrue(isNullOrEmpty<Any, Any>(extractor!!.description.content))
        }

        companion object {
            private var extractor: PlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "shortsUI"))
                extractor = YouTube.getPlaylistExtractor(
                        "https://www.youtube.com/playlist?list=UUSHBR8-60-B28hp2BmDPdntcQ")
                extractor!!.fetchPage()
            }
        }
    }

    class ContinuationsTests {
        @Test
        @Throws(Exception::class)
        fun testNoContinuations() {
            val extractor = YouTube
                    .getPlaylistExtractor(
                            "https://www.youtube.com/playlist?list=PLXJg25X-OulsVsnvZ7RVtSDW-id9_RzAO") as YoutubePlaylistExtractor
            extractor.fetchPage()
            DefaultTests.assertNoMoreItems(extractor)
        }

        @Test
        @Throws(Exception::class)
        fun testOnlySingleContinuation() {
            val extractor = YouTube
                    .getPlaylistExtractor(
                            "https://www.youtube.com/playlist?list=PLoumn5BIsUDeGF1vy5Nylf_RJKn5aL_nr") as YoutubePlaylistExtractor
            extractor.fetchPage()
            val page = DefaultTests.defaultTestMoreItems(
                    extractor)
            Assertions.assertFalse(page!!.hasNextPage(), "More items available when it shouldn't")
        }

        companion object {
            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "continuations"))
            }
        }
    }
}
