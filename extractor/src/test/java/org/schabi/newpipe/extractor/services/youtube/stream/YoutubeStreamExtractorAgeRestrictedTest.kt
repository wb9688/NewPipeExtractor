package org.schabi.newpipe.extractor.services.youtube.stream

import org.junit.jupiter.api.BeforeAll
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getStreamExtractor
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType

class YoutubeStreamExtractorAgeRestrictedTest : DefaultStreamExtractorTest() {
    override fun extractor(): StreamExtractor? {
        return extractor
    }

    override fun expectedService(): StreamingService {
        return YouTube
    }

    override fun expectedName(): String {
        return "Russian   Daft Punk"
    }

    override fun expectedId(): String {
        return ID
    }

    override fun expectedUrlContains(): String {
        return YoutubeStreamExtractorDefaultTest.BASE_URL + ID
    }

    override fun expectedOriginalUrlContains(): String {
        return URL
    }

    override fun expectedStreamType(): StreamType? {
        return StreamType.VIDEO_STREAM
    }

    override fun expectedUploaderName(): String {
        return "DAN TV"
    }

    override fun expectedUploaderUrl(): String? {
        return "https://www.youtube.com/channel/UCcQHIVL83g5BEQe2IJFb-6w"
    }

    override fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 50
    }

    override fun expectedUploaderVerified(): Boolean {
        return false
    }

    override fun expectedDescriptionIsEmpty(): Boolean {
        return true
    }

    override fun expectedDescriptionContains(): List<String> {
        return emptyList()
    }

    override fun expectedLength(): Long {
        return 10
    }

    override fun expectedTimestamp(): Long {
        return TIMESTAMP.toLong()
    }

    override fun expectedViewCountAtLeast(): Long {
        return 232000
    }

    override fun expectedUploadDate(): String? {
        return "2018-03-11 19:22:08.000"
    }

    override fun expectedTextualUploadDate(): String? {
        return "2018-03-11T12:22:08-07:00"
    }

    override fun expectedLikeCountAtLeast(): Long {
        return 3700
    }

    override fun expectedDislikeCountAtLeast(): Long {
        return -1
    }

    override fun expectedHasRelatedItems(): Boolean {
        return false
    } // no related videos (!)

    override fun expectedAgeLimit(): Int {
        return 18
    }

    override fun expectedHasSubtitles(): Boolean {
        return false
    }

    override fun expectedHasFrames(): Boolean {
        return false
    }

    override fun expectedCategory(): String {
        return "People & Blogs"
    }

    override fun expectedLicence(): String? {
        return "YouTube licence"
    }

    override fun expectedTags(): List<String> {
        return emptyList()
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
        private const val ID = "rwcfPqbAx-0"
        private const val TIMESTAMP = 196
        private const val URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID + "&t=" + TIMESTAMP
        private var extractor: StreamExtractor? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeTestsUtils.ensureStateless()
            init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ageRestricted"))
            extractor = YouTube.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
