package org.schabi.newpipe.extractor.services.youtube.stream

import org.junit.jupiter.api.BeforeAll
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getStreamExtractor
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamExtractor.Privacy
import org.schabi.newpipe.extractor.stream.StreamType

class YoutubeStreamExtractorUnlistedTest : DefaultStreamExtractorTest() {
    override fun extractor(): StreamExtractor? {
        return extractor
    }

    override fun expectedService(): StreamingService {
        return YouTube
    }

    override fun expectedName(): String {
        return "Praise the Casual: Ein Neuling trifft Dark Souls - Folge 5"
    }

    override fun expectedId(): String {
        return ID
    }

    override fun expectedUrlContains(): String {
        return URL
    }

    override fun expectedOriginalUrlContains(): String {
        return URL
    }

    override fun expectedStreamType(): StreamType? {
        return StreamType.VIDEO_STREAM
    }

    override fun expectedUploaderName(): String {
        return "Hooked"
    }

    override fun expectedUploaderUrl(): String? {
        return "https://www.youtube.com/channel/UCPysfiuOv4VKBeXFFPhKXyw"
    }

    override fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 24300
    }

    override fun expectedDescriptionContains(): List<String> {
        return mutableListOf("https://www.youtube.com/user/Roccowschiptune",
                "https://www.facebook.com/HookedMagazinDE")
    }

    override fun expectedLength(): Long {
        return 2488
    }

    override fun expectedViewCountAtLeast(): Long {
        return 1500
    }

    override fun expectedUploadDate(): String? {
        return "2017-09-22 12:15:21.000"
    }

    override fun expectedTextualUploadDate(): String? {
        return "2017-09-22T05:15:21-07:00"
    }

    override fun expectedLikeCountAtLeast(): Long {
        return 110
    }

    override fun expectedDislikeCountAtLeast(): Long {
        return -1
    }

    override fun expectedPrivacy(): Privacy? {
        return Privacy.UNLISTED
    }

    override fun expectedLicence(): String? {
        return "YouTube licence"
    }

    override fun expectedCategory(): String {
        return "Gaming"
    }

    override fun expectedTags(): List<String> {
        return mutableListOf("dark souls", "hooked", "praise the casual")
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
        const val ID = "udsB8KnIJTg"
        const val URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID
        private var extractor: StreamExtractor? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeTestsUtils.ensureStateless()
            init(DownloaderFactory.getDownloader(RESOURCE_PATH + "unlisted"))
            extractor = YouTube.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
