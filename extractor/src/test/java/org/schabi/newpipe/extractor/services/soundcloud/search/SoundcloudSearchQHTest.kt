package org.schabi.newpipe.extractor.services.soundcloud.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.url
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.downloader.Request.url
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.availableContentFilter
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.availableSortFilter
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.fromQuery
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory.fromQuery
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.searchQHFactory
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.searchQHFactory
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor.url
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor.url
import java.util.Arrays

class SoundcloudSearchQHTest {
    @Test
    @Throws(Exception::class)
    fun testRegularValues() {
        Assertions.assertEquals("https://api-v2.soundcloud.com/search?q=asdf&limit=10&offset=0",
                removeClientId(SoundCloud.searchQHFactory.fromQuery("asdf").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search?q=hans&limit=10&offset=0",
                removeClientId(SoundCloud.searchQHFactory.fromQuery("hans").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search?q=Poifj%26jaijf&limit=10&offset=0",
                removeClientId(SoundCloud.searchQHFactory.fromQuery("Poifj&jaijf").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search?q=G%C3%BCl%C3%BCm&limit=10&offset=0",
                removeClientId(SoundCloud.searchQHFactory.fromQuery("Gülüm").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search?q=%3Fj%24%29H%C2%A7B&limit=10&offset=0",
                removeClientId(SoundCloud.searchQHFactory.fromQuery("?j$)H§B").url))
    }

    @Test
    @Throws(Exception::class)
    fun testGetContentFilter() {
        Assertions.assertEquals("tracks", SoundCloud.searchQHFactory
                .fromQuery("", Arrays.asList<String>(*arrayOf<String>("tracks")), "").contentFilters.get(0))
        Assertions.assertEquals("users", SoundCloud.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>("users")), "").contentFilters.get(0))
    }

    @Test
    @Throws(Exception::class)
    fun testWithContentfilter() {
        Assertions.assertEquals("https://api-v2.soundcloud.com/search/tracks?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(SoundcloudSearchQueryHandlerFactory.TRACKS)), "").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search/users?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(SoundcloudSearchQueryHandlerFactory.USERS)), "").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search/playlists?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>(SoundcloudSearchQueryHandlerFactory.PLAYLISTS)), "").url))
        Assertions.assertEquals("https://api-v2.soundcloud.com/search?q=asdf&limit=10&offset=0", removeClientId(SoundCloud.searchQHFactory
                .fromQuery("asdf", Arrays.asList<String>(*arrayOf<String>("fjiijie")), "").url))
    }

    @Test
    fun testGetAvailableContentFilter() {
        val contentFilter: Array<String> = SoundCloud.searchQHFactory.availableContentFilter
        Assertions.assertEquals(4, contentFilter.size)
        Assertions.assertEquals("all", contentFilter[0])
        Assertions.assertEquals("tracks", contentFilter[1])
        Assertions.assertEquals("users", contentFilter[2])
        Assertions.assertEquals("playlists", contentFilter[3])
    }

    @Test
    fun testGetAvailableSortFilter() {
        val contentFilter: Array<String> = SoundCloud.searchQHFactory.availableSortFilter
        Assertions.assertEquals(0, contentFilter.size)
    }

    companion object {
        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass() {
            init(DownloaderTestImpl.Companion.getInstance())
        }

        private fun removeClientId(url: String): String {
            val splitUrl = url.split("client_id=[a-zA-Z0-9]*&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return splitUrl[0] + splitUrl[1]
        }
    }
}
