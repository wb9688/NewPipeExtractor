package org.schabi.newpipe.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getPlaylistExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.services.BasePlaylistExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor

/**
 * Test for [PlaylistExtractor]
 */
class SoundcloudPlaylistExtractorTest {
    class LuvTape : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("THE PERFECT LUV TAPE®️", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("246349810", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123", extractor!!.originalUrl)
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
        override fun testThumbnails() {
            DefaultTests.defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("liluzivert", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertTrue(extractor!!.uploaderName!!.contains("Lil Uzi Vert"))
        }

        @Test
        override fun testUploaderAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            assertGreaterOrEqual(10, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertTrue(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getPlaylistExtractor("https://soundcloud.com/liluzivert/sets/the-perfect-luv-tape-r?test=123")
                extractor!!.fetchPage()
            }
        }
    }

    class RandomHouseMusic : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("House", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("123062856", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/micky96/sets/house", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/micky96/sets/house", extractor!!.originalUrl)
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
        override fun testThumbnails() {
            DefaultTests.defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("micky96", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertEquals("_mickyyy", extractor!!.uploaderName)
        }

        @Test
        override fun testUploaderAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            assertGreaterOrEqual(10, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getPlaylistExtractor("https://soundcloud.com/micky96/sets/house")
                extractor!!.fetchPage()
            }
        }
    }

    class EDMxxx : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Additional Testing
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        @Throws(Exception::class)
        fun testGetPageInNewExtractor() {
            val newExtractor: PlaylistExtractor = SoundCloud.getPlaylistExtractor(extractor!!.url)
            DefaultTests.defaultTestGetPageInNewExtractor(extractor, newExtractor)
        }

        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("EDM xXx", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("136000376", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/user350509423/sets/edm-xxx", extractor!!.originalUrl)
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
            // Test for 2 more levels
            for (i in 0..1) {
                currentPage = extractor!!.getPage(currentPage!!.nextPage)
                DefaultTests.defaultTestListOfItems(SoundCloud, currentPage!!.items, currentPage.errors)
            }
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testThumbnails() {
            DefaultTests.defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("user350509423", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertEquals("user350509423", extractor!!.uploaderName)
        }

        @Test
        override fun testUploaderAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            assertGreaterOrEqual(370, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getPlaylistExtractor("https://soundcloud.com/user350509423/sets/edm-xxx")
                extractor!!.fetchPage()
            }
        }
    }

    class SmallPlaylist : BasePlaylistExtractorTest {
        /*//////////////////////////////////////////////////////////////////////////
        // Extractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testServiceId() {
            Assertions.assertEquals(SoundCloud.serviceId, extractor!!.serviceId)
        }

        @Test
        override fun testName() {
            Assertions.assertEquals("EMPTY PLAYLIST", extractor!!.name)
        }

        @Test
        override fun testId() {
            Assertions.assertEquals("23483459", extractor!!.id)
        }

        @Test
        @Throws(Exception::class)
        override fun testUrl() {
            Assertions.assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist", extractor!!.url)
        }

        @Test
        @Throws(Exception::class)
        override fun testOriginalUrl() {
            Assertions.assertEquals("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123", extractor!!.originalUrl)
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
        @Disabled("Test broken? Playlist has 2 entries, each page has 1 entry meaning it has 2 pages.")
        @Throws(Exception::class)
        override fun testMoreRelatedItems() {
            try {
                DefaultTests.defaultTestMoreItems(extractor!!)
            } catch (ignored: Throwable) {
                return
            }
            Assertions.fail<Any>("This playlist doesn't have more items, it should throw an error")
        }

        /*//////////////////////////////////////////////////////////////////////////
        // PlaylistExtractor
        ////////////////////////////////////////////////////////////////////////// */
        @Test
        override fun testThumbnails() {
            DefaultTests.defaultTestImageCollection(extractor!!.thumbnails)
        }

        @Test
        @Throws(ParsingException::class)
        override fun testBanners() {
            // SoundCloud playlists do not have a banner
            ExtractorAsserts.assertEmpty(extractor!!.banners)
        }

        @Test
        fun testUploaderUrl() {
            val uploaderUrl = extractor!!.uploaderUrl
            ExtractorAsserts.assertIsSecureUrl(uploaderUrl)
            ExtractorAsserts.assertContains("breezy-123", uploaderUrl)
        }

        @Test
        override fun testUploaderName() {
            Assertions.assertEquals("breezy-123", extractor!!.uploaderName)
        }

        @Test
        override fun testUploaderAvatars() {
            DefaultTests.defaultTestImageCollection(extractor!!.uploaderAvatars)
        }

        @Test
        override fun testStreamCount() {
            Assertions.assertEquals(2, extractor!!.streamCount)
        }

        @Test
        @Throws(Exception::class)
        override fun testUploaderVerified() {
            Assertions.assertFalse(extractor!!.isUploaderVerified)
        }

        companion object {
            private var extractor: SoundcloudPlaylistExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = SoundCloud
                        .getPlaylistExtractor("https://soundcloud.com/breezy-123/sets/empty-playlist?test=123")
                extractor!!.fetchPage()
            }
        }
    }
}
