package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getCommentsExtractor
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache.size
import org.schabi.newpipe.extractor.utils.Utils.isBlank
import java.io.IOException

class BandcampCommentsExtractorTest {
    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun hasComments() {
        Assertions.assertTrue(extractor!!.initialPage.getItems().size() >= 3)
    }

    @Test
    @Throws(IOException::class, ExtractionException::class)
    fun testGetCommentsAllData() {
        val comments = extractor!!.initialPage
        Assertions.assertTrue(comments!!.hasNextPage())
        DefaultTests.defaultTestListOfItems(Bandcamp, comments.items, comments.errors)
        for (c in comments.items!!) {
            Assertions.assertFalse(isBlank(c!!.uploaderName))
            BandcampTestUtils.testImages(c.uploaderAvatars)
            Assertions.assertFalse(isBlank(c.commentText!!.content))
            Assertions.assertFalse(isBlank(c.name))
            BandcampTestUtils.testImages(c.thumbnails)
            Assertions.assertFalse(isBlank(c.url))
            Assertions.assertEquals(-1, c.likeCount)
            Assertions.assertTrue(isBlank(c.textualLikeCount))
        }
    }

    companion object {
        private var extractor: CommentsExtractor? = null
        @BeforeAll
        @Throws(ExtractionException::class, IOException::class)
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = Bandcamp.getCommentsExtractor("https://floatingpoints.bandcamp.com/album/promises")
            extractor!!.fetchPage()
        }
    }
}
