package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MediaCCCStreamLinkHandlerFactoryTest {
    private static MediaCCCStreamLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = MediaCCCStreamLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").id);
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020?a=b").id);
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020#3").id);
        assertEquals("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://api.media.ccc.de/public/events/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020&a=b").id);
    }

    @Test
    public void getUrl() throws ParsingException {
        assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").url);
        assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromUrl("https://api.media.ccc.de/public/events/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020?b=a&a=b").url);
        assertEquals("https://media.ccc.de/v/jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020",
                linkHandler.fromId("jhremote20-3001-abschlusspraesentation_jugend_hackt_remote_2020").url);
    }
}
