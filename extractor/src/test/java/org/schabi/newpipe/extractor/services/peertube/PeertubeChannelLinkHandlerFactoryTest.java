package org.schabi.newpipe.extractor.services.peertube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs;

/**
 * Test for {@link PeertubeChannelLinkHandlerFactory}
 */
public class PeertubeChannelLinkHandlerFactoryTest {

    private static PeertubeChannelLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        PeerTube.instance = new PeertubeInstance("https://peertube.stream", "PeerTube on peertube.stream");
        linkHandler = PeertubeChannelLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void acceptUrlTest() throws ParsingException {
        assertTrue(linkHandler.acceptUrl("https://peertube.stream/accounts/kranti@videos.squat.net"));
        assertTrue(linkHandler.acceptUrl("https://peertube.stream/a/kranti@videos.squat.net"));
        assertTrue(linkHandler.acceptUrl("https://peertube.stream/api/v1/accounts/kranti@videos.squat.net/videos"));
        assertTrue(linkHandler.acceptUrl("https://peertube.stream/video-channels/kranti_channel@videos.squat.net/videos"));
        assertTrue(linkHandler.acceptUrl("https://peertube.stream/c/kranti_channel@videos.squat.net/videos"));
        assertTrue(linkHandler.acceptUrl("https://peertube.stream/api/v1/video-channels/7682d9f2-07be-4622-862e-93ec812e2ffa"));

        assertDoNotAcceptNonURLs(linkHandler);
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals("accounts/kranti@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/accounts/kranti@videos.squat.net").id);
        assertEquals("accounts/kranti@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/a/kranti@videos.squat.net").id);
        assertEquals("accounts/kranti@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/accounts/kranti@videos.squat.net/videos").id);
        assertEquals("accounts/kranti@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/a/kranti@videos.squat.net/videos").id);
        assertEquals("accounts/kranti@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/api/v1/accounts/kranti@videos.squat.net").id);
        assertEquals("accounts/kranti@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/api/v1/accounts/kranti@videos.squat.net/videos").id);

        assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/video-channels/kranti_channel@videos.squat.net/videos").id);
        assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/c/kranti_channel@videos.squat.net/videos").id);
        assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/c/kranti_channel@videos.squat.net/video-playlists").id);
        assertEquals("video-channels/kranti_channel@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/api/v1/video-channels/kranti_channel@videos.squat.net").id);
    }

    @Test
    public void getUrl() throws ParsingException {
        assertEquals("https://peertube.stream/video-channels/kranti_channel@videos.squat.net",
                linkHandler.fromId("video-channels/kranti_channel@videos.squat.net").url);
        assertEquals("https://peertube.stream/accounts/kranti@videos.squat.net",
                linkHandler.fromId("accounts/kranti@videos.squat.net").url);
        assertEquals("https://peertube.stream/accounts/kranti@videos.squat.net",
                linkHandler.fromId("kranti@videos.squat.net").url);
        assertEquals("https://peertube.stream/video-channels/kranti_channel@videos.squat.net",
                linkHandler.fromUrl("https://peertube.stream/api/v1/video-channels/kranti_channel@videos.squat.net").url);
    }
}
