package org.schabi.newpipe.extractor.services.youtube.stream

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.Extractor.serviceId
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getStreamExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import java.util.Objects
import java.util.function.Consumer
import java.util.stream.Collectors

class YoutubeStreamExtractorRelatedMixTest : DefaultStreamExtractorTest() {
    // @formatter:off
    override fun extractor(): StreamExtractor? {
        return extractor
    }
    override fun expectedService(): StreamingService {
        return YouTube
    }
    override fun expectedName(): String {
        return TITLE
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
        return "NoCopyrightSounds"
    }
    override fun expectedUploaderUrl(): String? {
        return "https://www.youtube.com/channel/UC_aEa8K-EOJ3D6gOs7HcyNg"
    }
    override fun expectedDescriptionContains(): List<String> {
        return mutableListOf("https://www.youtube.com/user/danielleviband/", "©")
    }
    override fun expectedUploaderVerified(): Boolean {
        return true
    }
    override fun expectedUploaderSubscriberCountAtLeast(): Long {
        return 32000000
    }
    override fun expectedLength(): Long {
        return 208
    }
    override fun expectedViewCountAtLeast(): Long {
        return 449000000
    }
    override fun expectedUploadDate(): String? {
        return "2015-07-09 16:34:35.000"
    }
    override fun expectedTextualUploadDate(): String? {
        return "2015-07-09T09:34:35-07:00"
    }
    override fun expectedLikeCountAtLeast(): Long {
        return 6400000
    }
    override fun expectedDislikeCountAtLeast(): Long {
        return -1
    }
    override fun expectedStreamSegmentsCount(): Int {
        return 0
    }
    override fun expectedLicence(): String? {
        return YoutubeStreamExtractorDefaultTest.YOUTUBE_LICENCE
    }
    override fun expectedCategory(): String {
        return "Music"
    }
    override fun expectedTags(): List<String> {
        return mutableListOf("Cartoon On & On (feat. Daniel Levi)", "Cartoon - On & On", "Cartoon", 
        "On & On", "NCS", "nocopyrightsounds", "no copyright sounds", "NCS release Cartoon", 
        "NCS Release Daniel Levi", "Daniel Levi", "NCS Release", "NCS Cartoon On & On", 
        "NCS On and On", "NCS On & On", "NCS Best Songs", "NCS Cartoon Daniel Levi", 
        "music", "songs", "ncs", "edm", "best music", "top music", "free music", 
        "club music", "dance music", "no copyright music", "electronic music", 
        "royalty free music", "copyright free music", "gaming music", "electronic pop")
    }
     // @formatter:on
     @Test
     @Throws(Exception::class)
     override fun testRelatedItems() {
         super.testRelatedItems()
         val playlists = Objects.requireNonNull(extractor!!.relatedItems)
                 .items
                 .stream()
                 .filter { o: Any? -> PlaylistInfoItem::class.java.isInstance(o) }
                 .map { obj: Any? -> PlaylistInfoItem::class.java.cast(obj) }
                 .collect(Collectors.toList())
         playlists.forEach(Consumer { item: PlaylistInfoItem ->
             Assertions.assertNotEquals(PlaylistType.NORMAL, item.playlistType,
                     "Unexpected normal playlist in related items")
         })
         val streamMixes = playlists.stream()
                 .filter { item: PlaylistInfoItem -> item.playlistType == PlaylistType.MIX_STREAM }
                 .collect(Collectors.toList())
         ExtractorAsserts.assertGreaterOrEqual(1, streamMixes.size.toLong(), "Not found one or more stream mix in related items")
         val streamMix = streamMixes[0]
         Assertions.assertSame(InfoType.PLAYLIST, streamMix.infoType)
         Assertions.assertEquals(YouTube.serviceId, streamMix.serviceId)
         ExtractorAsserts.assertContains(URL, streamMix.url)
         ExtractorAsserts.assertContains("list=RD" + ID, streamMix.url)
         Assertions.assertEquals("Mix – " + TITLE, streamMix.name)
         YoutubeTestsUtils.testImages(streamMix.thumbnails)
     }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/"
        private const val ID = "K4DyBUG242c"
        private const val URL = YoutubeStreamExtractorDefaultTest.BASE_URL + ID
        private const val TITLE = "Cartoon - On & On (feat. Daniel Levi) | Electronic Pop | NCS - Copyright Free Music"
        private var extractor: StreamExtractor? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeTestsUtils.ensureStateless()
            isConsentAccepted = true
            init(DownloaderFactory.getDownloader(RESOURCE_PATH + "relatedMix"))
            extractor = YouTube.getStreamExtractor(URL)
            extractor!!.fetchPage()
        }
    }
}
