package org.schabi.newpipe.extractor.services.bandcamp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

public class BandcampCommentsExtractorTest {

    private static CommentsExtractor extractor;

    @BeforeAll
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = Bandcamp.getCommentsExtractor("https://floatingpoints.bandcamp.com/album/promises");
        extractor.fetchPage();
    }

    @Test
    void hasComments() throws IOException, ExtractionException {
        assertTrue(extractor.initialPage.getItems().size() >= 3);
    }

    @Test
    void testGetCommentsAllData() throws IOException, ExtractionException {
        ListExtractor.InfoItemsPage<CommentsInfoItem> comments = extractor.initialPage;
        assertTrue(comments.hasNextPage());

        DefaultTests.defaultTestListOfItems(Bandcamp, comments.getItems(), comments.errors);
        for (final CommentsInfoItem c : comments.getItems()) {
            assertFalse(Utils.isBlank(c.uploaderName));
            BandcampTestUtils.testImages(c.uploaderAvatars);
            assertFalse(Utils.isBlank(c.commentText.content));
            assertFalse(Utils.isBlank(c.name));
            BandcampTestUtils.testImages(c.thumbnails);
            assertFalse(Utils.isBlank(c.url));
            assertEquals(-1, c.likeCount);
            assertTrue(Utils.isBlank(c.textualLikeCount));
        }
    }
}
