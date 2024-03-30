package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getStreamExtractor
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache.size
import java.io.IOException
import java.util.Calendar
import java.util.TimeZone
import java.util.function.Consumer

class BandcampRadioStreamExtractorTest : DefaultStreamExtractorTest() {
    @Test
    @Throws(ExtractionException::class)
    fun testGettingCorrectStreamExtractor() {
        Assertions.assertTrue(Bandcamp.getStreamExtractor("https://bandcamp.com/?show=3") is BandcampRadioStreamExtractor)
        Assertions.assertFalse(Bandcamp.getStreamExtractor("https://zachbenson.bandcamp.com/track/deflated") is BandcampRadioStreamExtractor)
    }

    override fun extractor(): StreamExtractor? {
        return extractor
    }

    @Throws(Exception::class)
    override fun expectedName(): String {
        return "Sound Movements"
    }

    @Throws(Exception::class)
    override fun expectedId(): String {
        return "230"
    }

    @Throws(Exception::class)
    override fun expectedUrlContains(): String {
        return URL
    }

    @Throws(Exception::class)
    override fun expectedOriginalUrlContains(): String {
        return URL
    }

    override fun expectedHasVideoStreams(): Boolean {
        return false
    }

    override fun expectedHasSubtitles(): Boolean {
        return false
    }

    override fun expectedHasFrames(): Boolean {
        return false
    }

    override fun expectedHasRelatedItems(): Boolean {
        return false
    }

    override fun expectedStreamType(): StreamType? {
        return StreamType.AUDIO_STREAM
    }

    override fun expectedService(): StreamingService {
        return Bandcamp
    }

    override fun expectedUploaderName(): String {
        return "Andrew Jervis"
    }

    override fun expectedStreamSegmentsCount(): Int {
        return 30
    }

    @Test
    fun testGetUploaderUrl() {
        Assertions.assertThrows<ContentNotSupportedException>(ContentNotSupportedException::class.java, extractor::getUploaderUrl)
    }

    @Test
    override fun testUploaderUrl() {
        Assertions.assertThrows(ContentNotSupportedException::class.java) { super.testUploaderUrl() }
    }

    override fun expectedUploaderUrl(): String? {
        return null
    }

    override fun expectedDescriptionContains(): List<String> {
        return listOf("Featuring special guests Nick Hakim and Elbows, plus fresh cuts from Eddie Palmieri, KRS One, Ladi6, and Moonchild.")
    }

    override fun expectedLength(): Long {
        return 5619
    }

    override fun expectedViewCountAtLeast(): Long {
        return -1
    }

    override fun expectedLikeCountAtLeast(): Long {
        return -1
    }

    override fun expectedDislikeCountAtLeast(): Long {
        return -1
    }

    override fun expectedUploadDate(): String? {
        return "16 May 2017 00:00:00 GMT"
    }

    override fun expectedTextualUploadDate(): String? {
        return "16 May 2017 00:00:00 GMT"
    }

    @Test
    @Throws(ParsingException::class)
    override fun testUploadDate() {
        val expectedCalendar = Calendar.getInstance()

        // 16 May 2017 00:00:00 GMT
        expectedCalendar.timeZone = TimeZone.getTimeZone("GMT")
        expectedCalendar.timeInMillis = 0
        expectedCalendar[2017, Calendar.MAY] = 16
        Assertions.assertEquals(expectedCalendar.timeInMillis, extractor!!.uploadDate!!.offsetDateTime().toInstant().toEpochMilli())
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetThumbnails() {
        BandcampTestUtils.testImages(extractor!!.thumbnails)
    }

    @Test
    @Throws(ParsingException::class)
    fun testGetUploaderAvatars() {
        DefaultTests.defaultTestImageCollection(extractor!!.uploaderAvatars)
        extractor!!.uploaderAvatars!!.forEach(Consumer { image: Image -> ExtractorAsserts.assertContains("bandcamp-button", image.url) })
    }

    @Test
    @Throws(ExtractionException::class, IOException::class)
    fun testGetAudioStreams() {
        assertEquals(1, extractor!!.audioStreams.size())
    }

    companion object {
        private var extractor: StreamExtractor? = null
        private const val URL = "https://bandcamp.com/?show=230"
        @BeforeAll
        @Throws(IOException::class, ExtractionException::class)
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = Bandcamp.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
