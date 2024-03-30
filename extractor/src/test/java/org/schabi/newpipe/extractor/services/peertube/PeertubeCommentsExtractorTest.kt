package org.schabi.newpipe.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.Extractor.url
import org.schabi.newpipe.extractor.InfoItemsCollector.items
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage.items
import org.schabi.newpipe.extractor.ListExtractor.getPage
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getCommentsExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor.name
import org.schabi.newpipe.extractor.comments.CommentsInfo
import org.schabi.newpipe.extractor.comments.CommentsInfo.Companion.getMoreItems
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.downloader.Request.url
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor.thumbnails
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.getPage
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.name
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioExtractor.name
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor.name
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeAccountExtractor.name
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelExtractor.name
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeCommentsExtractor.getPage
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor.name
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor.thumbnails
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor.uploaderName
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistExtractor.uploaderUrl
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeTrendingExtractor.name
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor.name
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudChartsExtractor.name
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.getPage
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.name
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.thumbnails
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.uploaderName
import org.schabi.newpipe.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor.uploaderUrl
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor.name
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelExtractor.url
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor.getPage
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor.name
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedExtractor.url
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor.getPage
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor.name
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor.thumbnails
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.getPage
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.name
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.thumbnails
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.uploaderName
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor.uploaderUrl
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeTrendingExtractor.name
import org.schabi.newpipe.extractor.stream.StreamExtractor.likeCount
import org.schabi.newpipe.extractor.stream.StreamExtractor.textualUploadDate
import org.schabi.newpipe.extractor.stream.StreamExtractor.uploaderAvatars
import org.schabi.newpipe.extractor.utils.Utils.isBlank
import java.io.IOException
import java.util.Optional
import java.util.function.BinaryOperator
import java.util.function.Consumer
import java.util.function.Function

object PeertubeCommentsExtractorTest {
    private fun findCommentWithId(
            id: String, comments: List<CommentsInfoItem?>?): Optional<CommentsInfoItem?> {
        return comments
                .stream()
                .filter { c: CommentsInfoItem? -> c!!.commentId == id }
                .findFirst()
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun findNestedCommentWithId(id: String, comment: CommentsInfoItem): Boolean {
        return if (comment.commentId == id) {
            true
        } else PeerTube
                .getCommentsExtractor(comment.url)
                .getPage(comment.replies)
                .items
                .stream()
                .map<Boolean>(Function<CommentsInfoItem, Boolean> { c: CommentsInfoItem ->
                    try {
                        return@map findNestedCommentWithId(id, c)
                    } catch (ignored: Exception) {
                        return@map false
                    }
                })
                .reduce(BinaryOperator<Boolean> { a: Boolean, b: Boolean -> a || b })
                .orElse(false)
    }

    class Default {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            val comment = "I love this"
            var comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            var result = findInComments(comments, comment)
            while (comments!!.hasNextPage() && !result) {
                comments = extractor!!.getPage(comments.nextPage)
                result = findInComments(comments, comment)
            }
            Assertions.assertTrue(result)
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFromCommentsInfo() {
            val comment = "Thanks for this nice video explanation of Peertube!"
            val commentsInfo = CommentsInfo.getInfo("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv")
            Assertions.assertEquals("Comments", commentsInfo!!.name)
            var result: Boolean = findInComments(commentsInfo.relatedItems, comment)
            var nextPage = commentsInfo.nextPage
            var moreItems: InfoItemsPage<CommentsInfoItem?>? = InfoItemsPage(null, nextPage, null)
            while (moreItems!!.hasNextPage() && !result) {
                moreItems = getMoreItems(PeerTube, commentsInfo, nextPage)
                result = findInComments(moreItems!!.items, comment)
                nextPage = moreItems!!.nextPage
            }
            Assertions.assertTrue(result)
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsAllData() {
            extractor!!.initialPage
                    .items
                    .forEach(Consumer<error.NonExistentClass> { commentsInfoItem: error.NonExistentClass ->
                        Assertions.assertFalse(isBlank(commentsInfoItem.uploaderUrl))
                        Assertions.assertFalse(isBlank(commentsInfoItem.uploaderName))
                        DefaultTests.defaultTestImageCollection(commentsInfoItem.uploaderAvatars)
                        Assertions.assertFalse(isBlank(commentsInfoItem.commentId))
                        Assertions.assertFalse(isBlank(commentsInfoItem.commentText.content))
                        Assertions.assertFalse(isBlank(commentsInfoItem.name))
                        Assertions.assertFalse(isBlank(commentsInfoItem.textualUploadDate))
                        DefaultTests.defaultTestImageCollection(commentsInfoItem.thumbnails)
                        Assertions.assertFalse(isBlank(commentsInfoItem.url))
                        assertEquals(-1, commentsInfoItem.likeCount)
                        Assertions.assertTrue(isBlank(commentsInfoItem.textualLikeCount))
                    })
        }

        private fun findInComments(comments: InfoItemsPage<CommentsInfoItem?>?,
                                   comment: String): Boolean {
            return findInComments(comments!!.items, comment)
        }

        private fun findInComments(comments: List<CommentsInfoItem>,
                                   comment: String): Boolean {
            return comments.stream()
                    .anyMatch { commentsInfoItem: CommentsInfoItem -> commentsInfoItem.commentText!!.content!!.contains(comment) }
        }

        companion object {
            private var extractor: PeertubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = PeerTube
                        .getCommentsExtractor("https://framatube.org/w/kkGMgK9ZtnKfYAgnEtQxbv")
            }
        }
    }

    class DeletedComments {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            val comments: InfoItemsPage<CommentsInfoItem?>? = extractor!!.initialPage
            Assertions.assertTrue(comments!!.errors!!.isEmpty())
        }

        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetCommentsFromCommentsInfo() {
            val commentsInfo = CommentsInfo.getInfo("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3")
            Assertions.assertTrue(commentsInfo!!.getErrors().isEmpty())
        }

        companion object {
            private var extractor: PeertubeCommentsExtractor? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = PeerTube
                        .getCommentsExtractor("https://framatube.org/videos/watch/217eefeb-883d-45be-b7fc-a788ad8507d3")
            }
        }
    }

    /**
     * Test a video that has comments with nested replies.
     */
    class NestedComments {
        @Test
        @Throws(IOException::class, ExtractionException::class)
        fun testGetComments() {
            Assertions.assertFalse(comments!!.items!!.isEmpty())
            val nestedCommentHeadOpt = findCommentWithId("9770", comments!!.items)
            Assertions.assertTrue(nestedCommentHeadOpt.isPresent)
            Assertions.assertTrue(findNestedCommentWithId("9773", nestedCommentHeadOpt.get()), "The nested comment replies were not found")
        }

        @Test
        fun testHasCreatorReply() {
            assertCreatorReply("9770", true)
            assertCreatorReply("9852", false)
            assertCreatorReply("11239", false)
        }

        companion object {
            private var extractor: PeertubeCommentsExtractor? = null
            private var comments: InfoItemsPage<CommentsInfoItem?>? = null
            @BeforeAll
            @Throws(Exception::class)
            fun setUp() {
                init(DownloaderTestImpl.Companion.getInstance())
                extractor = PeerTube
                        .getCommentsExtractor("https://share.tube/w/vxu4uTstUBAUromWwXGHrq")
                comments = extractor!!.initialPage
            }

            private fun assertCreatorReply(id: String, expected: Boolean) {
                val comment = findCommentWithId(id, comments!!.items)
                Assertions.assertTrue(comment.isPresent)
                Assertions.assertEquals(expected, comment.get().hasCreatorReply())
            }
        }
    }
}
