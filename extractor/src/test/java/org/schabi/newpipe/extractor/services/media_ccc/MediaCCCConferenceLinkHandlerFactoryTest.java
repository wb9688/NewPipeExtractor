package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MediaCCCConferenceLinkHandlerFactoryTest {
    private static MediaCCCConferenceLinkHandlerFactory linkHandler;

    @BeforeAll
    public static void setUp() {
        linkHandler = MediaCCCConferenceLinkHandlerFactory.getInstance();
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getId() throws ParsingException {
        assertEquals("jh20",
                linkHandler.fromUrl("https://media.ccc.de/c/jh20#278").id);
        assertEquals("jh20",
                linkHandler.fromUrl("https://media.ccc.de/b/jh20?a=b").id);
        assertEquals("jh20",
                linkHandler.fromUrl("https://api.media.ccc.de/public/conferences/jh20&a=b&b=c").id);
    }

    @Test
    public void getUrl() throws ParsingException {
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromUrl("https://media.ccc.de/c/jh20#278").url);
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromUrl("https://media.ccc.de/b/jh20?a=b").url);
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromUrl("https://api.media.ccc.de/public/conferences/jh20&a=b&b=c").url);
        assertEquals("https://media.ccc.de/c/jh20",
                linkHandler.fromId("jh20").url);
    }
}
