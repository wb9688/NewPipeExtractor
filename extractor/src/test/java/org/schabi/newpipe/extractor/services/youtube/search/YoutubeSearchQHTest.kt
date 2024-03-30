package org.schabi.newpipe.extractor.services.youtube.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.schabi.newpipe.extractor.Extractor.url
import org.schabi.newpipe.extractor.downloader.Request.url
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.availableContentFilter
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.availableSortFilter
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.fromQuery
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory.fromQuery
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.searchQHFactory
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.searchQHFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor.url
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor.url
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import java.util.Arrays

class YoutubeSearchQHTest {
    @Test
    @Throws(Exception::class)
    fun testRegularValues() {
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB", YouTube.searchQHFactory.fromQuery("asdf").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=hans&sp=8AEB", YouTube.searchQHFactory.fromQuery("hans").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=Poifj%26jaijf&sp=8AEB", YouTube.searchQHFactory.fromQuery("Poifj&jaijf").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=G%C3%BCl%C3%BCm&sp=8AEB", YouTube.searchQHFactory.fromQuery("Gülüm").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=%3Fj%24%29H%C2%A7B&sp=8AEB", YouTube.searchQHFactory.fromQuery("?j$)H§B").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=asdf", YouTube.searchQHFactory.fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=hans", YouTube.searchQHFactory.fromQuery("hans", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=Poifj%26jaijf", YouTube.searchQHFactory.fromQuery("Poifj&jaijf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=G%C3%BCl%C3%BCm", YouTube.searchQHFactory.fromQuery("Gülüm", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=%3Fj%24%29H%C2%A7B", YouTube.searchQHFactory.fromQuery("?j$)H§B", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
    }

    @Test
    @Throws(Exception::class)
    fun testGetContentFilter() {
        Assertions.assertEquals(YoutubeSearchQueryHandlerFactory.VIDEOS, YouTube.searchQHFactory
                .fromQuery("", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS)), "").contentFilters.get(0))
        Assertions.assertEquals(YoutubeSearchQueryHandlerFactory.CHANNELS, YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.CHANNELS)), "").contentFilters.get(0))
        Assertions.assertEquals(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS, YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").contentFilters.get(0))
    }

    @Test
    @Throws(Exception::class)
    fun testWithContentfilter() {
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAfABAQ%253D%253D", YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.VIDEOS)), "").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQAvABAQ%253D%253D", YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.CHANNELS)), "").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=EgIQA_ABAQ%253D%253D", YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.PLAYLISTS)), "").url)
        Assertions.assertEquals("https://www.youtube.com/results?search_query=asdf&sp=8AEB", YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>("fjiijie")), "").url)
        Assertions.assertEquals("https://music.youtube.com/search?q=asdf", YouTube.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS)), "").url)
    }

    @Test
    fun testGetAvailableContentFilter() {
        val contentFilter: Array<String> = YouTube.searchQHFactory.availableContentFilter
        Assertions.assertEquals(8, contentFilter.size)
        Assertions.assertEquals("all", contentFilter[0])
        Assertions.assertEquals("videos", contentFilter[1])
        Assertions.assertEquals("channels", contentFilter[2])
        Assertions.assertEquals("playlists", contentFilter[3])
        Assertions.assertEquals("music_songs", contentFilter[4])
        Assertions.assertEquals("music_videos", contentFilter[5])
        Assertions.assertEquals("music_albums", contentFilter[6])
        Assertions.assertEquals("music_playlists", contentFilter[7])
    }

    @Test
    fun testGetAvailableSortFilter() {
        val contentFilter: Array<String> = YouTube.searchQHFactory.availableSortFilter
        Assertions.assertEquals(0, contentFilter.size)
    }
}
