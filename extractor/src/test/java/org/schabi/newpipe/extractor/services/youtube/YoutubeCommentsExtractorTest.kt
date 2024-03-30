package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getCommentsExtractor
import org.schabi.newpipe.extractor.comments.CommentsInfo
import org.schabi.newpipe.extractor.comments.CommentsInfo.Companion.getMoreItems
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.Localization.Companion.fromLocale
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor
import org.schabi.newpipe.extractor.utils.Utils.isBlank
import java.io.IOException
import java.util.Locale

object YoutubeCommentsExtractorTest {
    private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/comments/"

    /**
     * Test a "normal" YouTube
     */
    class Thomas {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            Assertions.assertTrue(getCommentsHelper(extractor))
        }

        @Throws(IOException::class, ExtractionException::class)
        private fun getCommentsHelper(extractor: YoutubeCommentsExtractor?): Boolean {
            var comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            var result = findInComments(comments, commentContent)
            while (comments!!.hasNextPage() && !result) {
                comments = extractor.getPage(comments.nextPage)
                result = findInComments(comments, commentContent)
            }
            return result
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFromCommentsInfo() {
            Assertions.assertTrue(getCommentsFromCommentsInfoHelper(url))
        }

        @Throws(IOException::class, ExtractionException::class)
        private fun getCommentsFromCommentsInfoHelper(url: String): Boolean {
            val commentsInfo = CommentsInfo.getInfo(url)
            Assertions.assertEquals("Comments", commentsInfo!!.name)
            var result: Boolean = findInComments(commentsInfo.relatedItems, commentContent)
            var nextPage = commentsInfo.nextPage
            var moreItems: InfoItemsPage<CommentsInfoItem?>? = InfoItemsPage(null, nextPage, null)
            while (moreItems!!.hasNextPage() && !result) {
                moreItems = getMoreItems(YouTube, commentsInfo, nextPage)
                result = findInComments(moreItems!!.items, commentContent)
                nextPage = moreItems!!.nextPage
            }
            return result
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            Assertions.assertTrue(extractor!!.commentsCount > 5) // at least 5 comments
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            for (c in comments.items!!) {
                Assertions.assertFalse(isBlank(c!!.uploaderUrl))
                Assertions.assertFalse(isBlank(c.uploaderName))
                YoutubeTestsUtils.testImages(c.uploaderAvatars)
                Assertions.assertFalse(isBlank(c.commentId))
                Assertions.assertFalse(isBlank(c.commentText!!.content))
                Assertions.assertFalse(isBlank(c.name))
                Assertions.assertFalse(isBlank(c.textualUploadDate))
                Assertions.assertNotNull(c.uploadDate)
                YoutubeTestsUtils.testImages(c.thumbnails)
                Assertions.assertFalse(isBlank(c.url))
                Assertions.assertTrue(c.likeCount >= 0)
            }
        }

        private fun findInComments(comments: InfoItemsPage<CommentsInfoItem?>?, comment: String): Boolean {
            return findInComments(comments!!.items, comment)
        }

        private fun findInComments(comments: List<CommentsInfoItem>, comment: String): Boolean {
            for (c in comments) {
                if (c.commentText!!.content!!.contains(comment)) {
                    return true
                }
            }
            return false
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=D00Au7k3i6o"
            private const val commentContent = "Category: Education"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "thomas"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * Test a video with an empty comment
     */
    class EmptyComment {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            for (c in comments.items!!) {
                Assertions.assertFalse(isBlank(c!!.uploaderUrl))
                Assertions.assertFalse(isBlank(c.uploaderName))
                YoutubeTestsUtils.testImages(c.uploaderAvatars)
                Assertions.assertFalse(isBlank(c.commentId))
                Assertions.assertFalse(isBlank(c.name))
                Assertions.assertFalse(isBlank(c.textualUploadDate))
                Assertions.assertNotNull(c.uploadDate)
                YoutubeTestsUtils.testImages(c.thumbnails)
                Assertions.assertFalse(isBlank(c.url))
                Assertions.assertTrue(c.likeCount >= 0)
                if (c.commentId == "Ugga_h1-EXdHB3gCoAEC") { // comment without text
                    Assertions.assertTrue(isBlank(c.commentText!!.content))
                } else {
                    Assertions.assertFalse(isBlank(c.commentText!!.content))
                }
            }
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=VM_6n762j6M"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "empty"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    class HeartedByCreator {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            var heartedByUploader = false
            for (c in comments.items!!) {
                Assertions.assertFalse(isBlank(c!!.uploaderUrl))
                Assertions.assertFalse(isBlank(c.uploaderName))
                YoutubeTestsUtils.testImages(c.uploaderAvatars)
                Assertions.assertFalse(isBlank(c.commentId))
                Assertions.assertFalse(isBlank(c.name))
                Assertions.assertFalse(isBlank(c.textualUploadDate))
                Assertions.assertNotNull(c.uploadDate)
                YoutubeTestsUtils.testImages(c.thumbnails)
                Assertions.assertFalse(isBlank(c.url))
                Assertions.assertTrue(c.likeCount >= 0)
                Assertions.assertFalse(isBlank(c.commentText!!.content))
                if (c.isHeartedByUploader) {
                    heartedByUploader = true
                }
            }
            Assertions.assertTrue(heartedByUploader, "No comments was hearted by uploader")
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=tR11b7uh17Y"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "hearted"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    class Pinned {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            for (c in comments.items!!) {
                Assertions.assertFalse(isBlank(c!!.uploaderUrl))
                Assertions.assertFalse(isBlank(c.uploaderName))
                YoutubeTestsUtils.testImages(c.uploaderAvatars)
                Assertions.assertFalse(isBlank(c.commentId))
                Assertions.assertFalse(isBlank(c.name))
                Assertions.assertFalse(isBlank(c.textualUploadDate))
                Assertions.assertNotNull(c.uploadDate)
                YoutubeTestsUtils.testImages(c.thumbnails)
                Assertions.assertFalse(isBlank(c.url))
                Assertions.assertTrue(c.likeCount >= 0)
                Assertions.assertFalse(isBlank(c.commentText!!.content))
            }
            Assertions.assertTrue(comments.items!![0]!!.isPinned, "First comment isn't pinned")
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=bjFtFMilb34"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "pinned"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * Checks if the likes/votes are handled correctly<br></br>
     * A pinned comment with >15K likes is used for the test
     */
    class LikesVotes {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFirst() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            val pinnedComment = comments.items!![0]
            Assertions.assertTrue(pinnedComment!!.isPinned, "First comment isn't pinned")
            Assertions.assertTrue(pinnedComment.likeCount > 0, "The first pinned comment has no likes")
            Assertions.assertFalse(isBlank(pinnedComment.textualLikeCount), "The first pinned comment has no vote count")
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=QqsLTNkzvaY"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "likes"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    /**
     * Checks if the vote count works localized<br></br>
     * A pinned comment with >15K likes is used for the test
     */
    class LocalizedVoteCount {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFirst() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            val pinnedComment = comments.items!![0]
            Assertions.assertTrue(pinnedComment!!.isPinned, "First comment isn't pinned")
            Assertions.assertFalse(isBlank(pinnedComment.textualLikeCount), "The first pinned comment has no vote count")
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=QqsLTNkzvaY"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "localized_vote_count"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                // Force non english local here
                extractor!!.forceLocalization(fromLocale(Locale.GERMANY))
                extractor!!.fetchPage()
            }
        }
    }

    class RepliesTest {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFirstReplies() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            val firstComment = comments.items!![0]
            Assertions.assertTrue(firstComment!!.isPinned, "First comment isn't pinned")
            val replies = extractor!!.getPage(firstComment.replies)
            Assertions.assertEquals("First", replies!!.items!![0]!!.commentText!!.content,
                    "First reply comment did not match")
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsReplyCount() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            val firstComment = comments.items!![0]
            Assertions.assertNotEquals(CommentsInfoItem.UNKNOWN_REPLY_COUNT, firstComment!!.replyCount, "Could not get the reply count of the first comment")
            assertGreater(300, firstComment.replyCount.toLong())
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testCommentsCount() {
            Assertions.assertTrue(extractor!!.commentsCount > 18800)
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=xaQJbozY_Is"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "replies"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    class ChannelOwnerTest {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            var channelOwner = false
            for (c in comments.items!!) {
                Assertions.assertFalse(isBlank(c!!.uploaderUrl))
                Assertions.assertFalse(isBlank(c.uploaderName))
                YoutubeTestsUtils.testImages(c.uploaderAvatars)
                Assertions.assertFalse(isBlank(c.commentId))
                Assertions.assertFalse(isBlank(c.name))
                Assertions.assertFalse(isBlank(c.textualUploadDate))
                Assertions.assertNotNull(c.uploadDate)
                YoutubeTestsUtils.testImages(c.thumbnails)
                Assertions.assertFalse(isBlank(c.url))
                Assertions.assertTrue(c.likeCount >= 0)
                Assertions.assertFalse(isBlank(c.commentText!!.content))
                if (c.isChannelOwner) {
                    channelOwner = true
                }
            }
            Assertions.assertTrue(channelOwner, "No comments was made by the channel owner")
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=bem4adjGKjE"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "channelOwner"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    class CreatorReply {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            var creatorReply = false
            for (c in comments.items!!) {
                Assertions.assertFalse(isBlank(c!!.uploaderUrl))
                Assertions.assertFalse(isBlank(c.uploaderName))
                YoutubeTestsUtils.testImages(c.uploaderAvatars)
                Assertions.assertFalse(isBlank(c.commentId))
                Assertions.assertFalse(isBlank(c.name))
                Assertions.assertFalse(isBlank(c.textualUploadDate))
                Assertions.assertNotNull(c.uploadDate)
                YoutubeTestsUtils.testImages(c.thumbnails)
                Assertions.assertFalse(isBlank(c.url))
                Assertions.assertTrue(c.likeCount >= 0)
                Assertions.assertFalse(isBlank(c.commentText!!.content))
                if (c.hasCreatorReply()) {
                    creatorReply = true
                }
            }
            Assertions.assertTrue(creatorReply, "No comments was replied to by creator")
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=bem4adjGKjE"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "creatorReply"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }

    class FormattingTest {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFormatting() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            DefaultTests.defaultTestListOfItems(YouTube, comments!!.items, comments.errors)
            val firstComment = comments.items!![0]
            ExtractorAsserts.assertContains("<s>", firstComment!!.commentText!!.content)
            ExtractorAsserts.assertContains("<b>", firstComment.commentText!!.content)
        }

        companion object {
            private const val url = "https://www.youtube.com/watch?v=zYpyS2HaZHM"
            private var extractor: YoutubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                YoutubeTestsUtils.ensureStateless()
                init(DownloaderFactory.getDownloader(RESOURCE_PATH + "formatting"))
                extractor = YouTube
                        .getCommentsExtractor(url)
                extractor!!.fetchPage()
            }
        }
    }
}
