package org.schabi.newpipe.extractor.services.peertube.search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.services.peertube.PeertubeInstance;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

public class PeertubeSearchQHTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        // setting instance might break test when running in parallel
        PeerTube.instance = new PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host");
    }

    @Test
    void testVideoSearch() throws Exception {
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=asdf", PeerTube.getSearchQHFactory().fromQuery("asdf").url);
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=hans", PeerTube.getSearchQHFactory().fromQuery("hans").url);
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=Poifj%26jaijf", PeerTube.getSearchQHFactory().fromQuery("Poifj&jaijf").url);
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=G%C3%BCl%C3%BCm", PeerTube.getSearchQHFactory().fromQuery("Gülüm").url);
        assertEquals("https://peertube.mastodon.host/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.getSearchQHFactory().fromQuery("?j$)H§B").url);
    }

    @Test
    void testSepiaVideoSearch() throws Exception {
        assertEquals("https://sepiasearch.org/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.getSearchQHFactory().fromQuery("?j$)H§B", singletonList(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS), "").url);
        assertEquals("https://anotherpeertubeindex.com/api/v1/search/videos?search=%3Fj%24%29H%C2%A7B", PeerTube.getSearchQHFactory().fromQuery("?j$)H§B", singletonList(PeertubeSearchQueryHandlerFactory.SEPIA_VIDEOS), "", "https://anotherpeertubeindex.com").url);
    }

    @Test
    void testPlaylistSearch() throws Exception {
        assertEquals("https://peertube.mastodon.host/api/v1/search/video-playlists?search=asdf", PeerTube.getSearchQHFactory().fromQuery("asdf", singletonList(PeertubeSearchQueryHandlerFactory.PLAYLISTS), "").url);
        assertEquals("https://peertube.mastodon.host/api/v1/search/video-playlists?search=hans", PeerTube.getSearchQHFactory().fromQuery("hans", singletonList(PeertubeSearchQueryHandlerFactory.PLAYLISTS), "").url);
    }

    @Test
    void testChannelSearch() throws Exception {
        assertEquals("https://peertube.mastodon.host/api/v1/search/video-channels?search=asdf", PeerTube.getSearchQHFactory().fromQuery("asdf", singletonList(PeertubeSearchQueryHandlerFactory.CHANNELS), "").url);
        assertEquals("https://peertube.mastodon.host/api/v1/search/video-channels?search=hans", PeerTube.getSearchQHFactory().fromQuery("hans", singletonList(PeertubeSearchQueryHandlerFactory.CHANNELS), "").url);

    }
}