package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.kiosk.KioskList.getExtractorById
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.kioskList
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.kioskList
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.kioskList
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty
import java.util.stream.Stream

class MediaCCCRecentListExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testStreamList() {
        val items: List<StreamInfoItem> = extractor!!.initialPage.getItems()
        Assertions.assertFalse(items.isEmpty(), "No items returned")
        Assertions.assertAll(items.stream().flatMap { item: StreamInfoItem -> getAllConditionsForItem(item) })
    }

    private fun getAllConditionsForItem(item: StreamInfoItem): Stream<Executable?> {
        return Stream.of(
                Executable {
                    Assertions.assertFalse(
                            isNullOrEmpty(item.name),
                            "Name=[" + item.name + "] of " + item + " is empty or null"
                    )
                },
                Executable {
                    ExtractorAsserts.assertGreater(0,
                            item.duration,
                            "Duration[=" + item.duration + "] of " + item + " is <= 0"
                    )
                }
        )
    }

    companion object {
        private var extractor: KioskExtractor<*>? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = MediaCCC.kioskList.getExtractorById("recent", null)
            extractor!!.fetchPage()
        }
    }
}
