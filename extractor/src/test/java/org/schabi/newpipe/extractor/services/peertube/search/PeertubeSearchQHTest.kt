package org.schabi.newpipe.extractor.services.peertube.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.extractor.Extractor.url
import org.schabi.newpipe.extractor.downloader.Request.url
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory.fromQuery
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory.fromQuery
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.searchQHFactory
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.searchQHFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.searchQHFactory
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor.url
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor.url

class PeertubeSearchQHTest {
    @Test
    @Throws(Exception::class)
    fun testVideoSearch() {
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=asdf", PeerTube.searchQHFactory.fromQuery("asdf").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=hans", PeerTube.searchQHFactory.fromQuery("hans").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=Poifj%26jaijf", PeerTube.searchQHFactory.fromQuery("Poifj&jaijf").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=G%C3%BCl%C3%BCm", PeerTube.searchQHFactory.fromQuery("Gülüm").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.searchQHFactory.fromQuery("?j$)H§B").url)
    }

    @Test
    @Throws(Exception::class)
    fun testSepiaVideoSearch() {
        Assertions.assertEquals("https://sepiasearch.org/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.searchQHFactory.fromQuery("?j$)H§B", listOf<String>(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS), "").url)
        Assertions.assertEquals("https://anotherpeertubeindex.com/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.searchQHFactory.fromQuery("?j$)H§B", listOf<String>(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS), "", "https://anotherpeertubeindex.com").url)
    }

    @Test
    @Throws(Exception::class)
    fun testPlaylistSearch() {
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-playlists?search=asdf", PeerTube.searchQHFactory.fromQuery("asdf", listOf<String>(PeertubeSearchQueryHandlerFactory.PLAYLISTS), "").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-playlists?search=hans", PeerTube.searchQHFactory.fromQuery("hans", listOf<String>(PeertubeSearchQueryHandlerFactory.PLAYLISTS), "").url)
    }

    @Test
    @Throws(Exception::class)
    fun testChannelSearch() {
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-channels?search=asdf", PeerTube.searchQHFactory.fromQuery("asdf", listOf<String>(PeertubeSearchQueryHandlerFactory.CHANNELS), "").url)
        Assertions.assertEquals("https://peertube.mastodon.host/api/v1/search/video-channels?search=hans", PeerTube.searchQHFactory.fromQuery("hans", listOf<String>(PeertubeSearchQueryHandlerFactory.CHANNELS), "").url)
    }

    companion object {
        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass() {
            // setting instance might break test when running in parallel
            PeerTube.instance = PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host")
        }
    }
}