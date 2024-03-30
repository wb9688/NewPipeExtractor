package org.schabi.newpipe.extractor.services.youtube

import com.grack.nanojson.JsonWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.NewPipe.getPreferredContentCountry
import org.schabi.newpipe.extractor.NewPipe.getPreferredLocalization
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService.getPlaylistExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getKey
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.prepareDesktopJsonBuilder
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Map
import java.util.function.Consumer

object YoutubeMixPlaylistExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/mix/"
    private val dummyCookie = Map.of(YoutubeMixPlaylistExtractor.COOKIE_NAME, "whatever")
    private var extractor: YoutubeMixPlaylistExtractor? = null

    class Mix {
        @get:Test
        val serviceId: Unit
            get() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @get:Throws(Exception::class)
        @get:Test
        val name: Unit
            get() {
                val name = extractor!!.name
                ExtractorAsserts.assertContains("Mix", name)
                ExtractorAsserts.assertContains(VIDEO_TITLE, name)
            }

        @get:Throws(Exception::class)
        @get:Test
        val thumbnails: Unit
            get() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails!!.forEach(Consumer { thumbnail: Image -> ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.url) })
            }

        @get:Throws(Exception::class)
        @get:Test
        val initialPage: Unit
            get() {
                val streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val page: Unit
            get() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getPreferredLocalization(), getPreferredContentCountry())
                        .value("videoId", VIDEO_ID)
                        .value("playlistId", "RD" + VIDEO_ID)
                        .value("params", "OAE%3D")
                        .done())
                        .toByteArray(StandardCharsets.UTF_8)
                val streams = extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?key=" + getKey() + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val continuations: Unit
            get() {
                var streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                val urls: MutableSet<String?> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())
                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item!!.url)
                    }
                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @get:Test
        val streamCount: Unit
            get() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor!!.streamCount)
            }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.MIX_STREAM, extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "FAqYW76GLPA"
            private const val VIDEO_TITLE = "Mix – "
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "mix"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                                + "&list=RD" + VIDEO_ID)
                extractor!!.fetchPage()
            }
        }
    }

    class MixWithIndex {
        @get:Throws(Exception::class)
        @get:Test
        val name: Unit
            get() {
                val name = extractor!!.name
                ExtractorAsserts.assertContains("Mix", name)
                ExtractorAsserts.assertContains(VIDEO_TITLE, name)
            }

        @get:Throws(Exception::class)
        @get:Test
        val thumbnails: Unit
            get() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails!!.forEach(Consumer { thumbnail: Image -> ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.url) })
            }

        @get:Throws(Exception::class)
        @get:Test
        val initialPage: Unit
            get() {
                val streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val page: Unit
            get() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getPreferredLocalization(), getPreferredContentCountry())
                        .value("videoId", VIDEO_ID)
                        .value("playlistId", "RD" + VIDEO_ID)
                        .value("playlistIndex", INDEX)
                        .value("params", "OAE%3D")
                        .done())
                        .toByteArray(StandardCharsets.UTF_8)
                val streams = extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?key=" + getKey() + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val continuations: Unit
            get() {
                var streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                val urls: MutableSet<String?> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())
                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item!!.url)
                    }
                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @get:Test
        val streamCount: Unit
            get() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor!!.streamCount)
            }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.MIX_STREAM, extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "FAqYW76GLPA"
            private const val VIDEO_TITLE = "Mix – "
            private const val INDEX = 7 // YT starts the index with 1...
            private const val VIDEO_ID_AT_INDEX = "F90Cw4l-8NY"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "mixWithIndex"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID_AT_INDEX
                                + "&list=RD" + VIDEO_ID + "&index=" + INDEX)
                extractor!!.fetchPage()
            }
        }
    }

    class MyMix {
        @get:Test
        val serviceId: Unit
            get() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @get:Throws(Exception::class)
        @get:Test
        val name: Unit
            get() {
                val name = extractor!!.name
                Assertions.assertEquals("My Mix", name)
            }

        @get:Throws(Exception::class)
        @get:Test
        val thumbnails: Unit
            get() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails!!.forEach(Consumer { thumbnail: Image -> ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.url) })
            }

        @get:Throws(Exception::class)
        @get:Test
        val initialPage: Unit
            get() {
                val streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val page: Unit
            get() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getPreferredLocalization(), getPreferredContentCountry())
                        .value("videoId", VIDEO_ID)
                        .value("playlistId", "RDMM" + VIDEO_ID)
                        .value("params", "OAE%3D")
                        .done())
                        .toByteArray(StandardCharsets.UTF_8)
                val streams = extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?key=" + getKey() + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val continuations: Unit
            get() {
                var streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                val urls: MutableSet<String?> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())
                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item!!.url)
                    }
                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @get:Test
        val streamCount: Unit
            get() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor!!.streamCount)
            }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.MIX_STREAM, extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "YVkUvmDQ3HY"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "myMix"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                                + "&list=RDMM" + VIDEO_ID)
                extractor!!.fetchPage()
            }
        }
    }

    class Invalid {
        @get:Throws(Exception::class)
        @get:Test
        val pageEmptyUrl: Unit
            get() {
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                                + "&list=RD" + VIDEO_ID)
                extractor!!.fetchPage()
                Assertions.assertThrows(IllegalArgumentException::class.java) { extractor!!.getPage(Page("")) }
            }

        @Test
        @Throws(Exception::class)
        fun invalidVideoId() {
            extractor = YouTube
                    .getPlaylistExtractor("https://www.youtube.com/watch?v=" + "abcde"
                            + "&list=RD" + "abcde")
            Assertions.assertThrows(ExtractionException::class.java) { extractor!!.fetchPage() }
        }

        companion object {
            private const val VIDEO_ID = "QMVCAPd5cwBcg"
            @BeforeAll
            @Throws(IOException::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "invalid"))
            }
        }
    }

    class ChannelMix {
        @get:Throws(Exception::class)
        @get:Test
        val name: Unit
            get() {
                val name = extractor!!.name
                ExtractorAsserts.assertContains("Mix", name)
                ExtractorAsserts.assertContains(CHANNEL_TITLE, name)
            }

        @get:Throws(Exception::class)
        @get:Test
        val thumbnails: Unit
            get() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails!!.forEach(Consumer { thumbnail: Image -> ExtractorAsserts.assertContains(VIDEO_ID_OF_CHANNEL, thumbnail.url) })
            }

        @get:Throws(Exception::class)
        @get:Test
        val initialPage: Unit
            get() {
                val streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val page: Unit
            get() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getPreferredLocalization(), getPreferredContentCountry())
                        .value("videoId", VIDEO_ID_OF_CHANNEL)
                        .value("playlistId", "RDCM" + CHANNEL_ID)
                        .value("params", "OAE%3D")
                        .done())
                        .toByteArray(StandardCharsets.UTF_8)
                val streams = extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?key=" + getKey() + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Test
        val streamCount: Unit
            get() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor!!.streamCount)
            }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.MIX_CHANNEL, extractor!!.playlistType)
            }

        companion object {
            private const val CHANNEL_ID = "UCXuqSBlHAE6Xw-yeJA0Tunw"
            private const val VIDEO_ID_OF_CHANNEL = "mnk6gnOBYIo"
            private const val CHANNEL_TITLE = "Linus Tech Tips"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channelMix"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID_OF_CHANNEL
                                + "&list=RDCM" + CHANNEL_ID)
                extractor!!.fetchPage()
            }
        }
    }

    class GenreMix {
        @get:Test
        val serviceId: Unit
            get() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @get:Throws(Exception::class)
        @get:Test
        val name: Unit
            get() {
                Assertions.assertEquals(MIX_TITLE, extractor!!.name)
            }

        @get:Throws(Exception::class)
        @get:Test
        val thumbnails: Unit
            get() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails!!.forEach(Consumer { thumbnail: Image -> ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.url) })
            }

        @get:Throws(Exception::class)
        @get:Test
        val initialPage: Unit
            get() {
                val streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val page: Unit
            get() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getPreferredLocalization(), getPreferredContentCountry())
                        .value("videoId", VIDEO_ID)
                        .value("playlistId", "RD" + VIDEO_ID)
                        .value("params", "OAE%3D")
                        .done())
                        .toByteArray(StandardCharsets.UTF_8)
                val streams = extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?key=" + getKey() + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val continuations: Unit
            get() {
                var streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                val urls: MutableSet<String?> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())
                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item!!.url)
                    }
                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @get:Test
        val streamCount: Unit
            get() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor!!.streamCount)
            }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.MIX_GENRE, extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "kINJeTNFbpg"
            private const val MIX_TITLE = "Mix – Electronic music"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "genreMix"))
                extractor = YouTube
                        .getPlaylistExtractor("https://www.youtube.com/watch?v=" + VIDEO_ID
                                + "&list=RDGMEMYH9CUrFO7CfLJpaD7UR85w")
                extractor!!.fetchPage()
            }
        }
    }

    class Music {
        @get:Test
        val serviceId: Unit
            get() {
                Assertions.assertEquals(YouTube.serviceId, extractor!!.serviceId)
            }

        @get:Throws(Exception::class)
        @get:Test
        val name: Unit
            get() {
                Assertions.assertEquals(MIX_TITLE, extractor!!.name)
            }

        @get:Throws(Exception::class)
        @get:Test
        val thumbnailUrl: Unit
            get() {
                YoutubeTestsUtils.testImages(extractor!!.thumbnails)
                extractor!!.thumbnails!!.forEach(Consumer { thumbnail: Image -> ExtractorAsserts.assertContains(VIDEO_ID, thumbnail.url) })
            }

        @get:Throws(Exception::class)
        @get:Test
        val initialPage: Unit
            get() {
                val streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val page: Unit
            get() {
                val body = JsonWriter.string(prepareDesktopJsonBuilder(
                        getPreferredLocalization(), getPreferredContentCountry())
                        .value("videoId", VIDEO_ID)
                        .value("playlistId", "RD" + VIDEO_ID)
                        .value("params", "OAE%3D")
                        .done())
                        .toByteArray(StandardCharsets.UTF_8)
                val streams = extractor!!.getPage(Page(
                        YOUTUBEI_V1_URL + "next?key=" + getKey() + DISABLE_PRETTY_PRINT_PARAMETER,
                        null, null, dummyCookie, body))
                Assertions.assertFalse(streams!!.items!!.isEmpty())
                Assertions.assertTrue(streams.hasNextPage())
            }

        @get:Throws(Exception::class)
        @get:Test
        val continuations: Unit
            get() {
                var streams: InfoItemsPage<StreamInfoItem?>? = extractor!!.initialPage
                val urls: MutableSet<String?> = HashSet()

                // Should work infinitely, but for testing purposes only 3 times
                for (i in 0..2) {
                    Assertions.assertTrue(streams!!.hasNextPage())
                    Assertions.assertFalse(streams.items!!.isEmpty())
                    for (item in streams.items!!) {
                        // TODO Duplicates are appearing
                        // assertFalse(urls.contains(item.getUrl()));
                        urls.add(item!!.url)
                    }
                    streams = extractor!!.getPage(streams.nextPage)
                }
                Assertions.assertTrue(streams!!.hasNextPage())
                Assertions.assertFalse(streams.items!!.isEmpty())
            }

        @get:Test
        val streamCount: Unit
            get() {
                Assertions.assertEquals(ListExtractor.ITEM_COUNT_INFINITE, extractor!!.streamCount)
            }

        @get:Throws(ParsingException::class)
        @get:Test
        val playlistType: Unit
            get() {
                Assertions.assertEquals(PlaylistType.MIX_MUSIC, extractor!!.playlistType)
            }

        companion object {
            private const val VIDEO_ID = "dQw4w9WgXcQ"
            private const val MIX_TITLE = "Mix – Rick Astley - Never Gonna Give You Up (Official Music Video)"
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                isConsentAccepted = true
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "musicMix"))
                extractor = YouTube.getPlaylistExtractor("https://m.youtube.com/watch?v=" + VIDEO_ID
                        + "&list=RDAMVM" + VIDEO_ID)
                extractor!!.fetchPage()
            }
        }
    }
}
