package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.kiosk.KioskList.getExtractorById
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.kioskList
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.kioskList
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.kioskList
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList

class MediaCCCLiveStreamListExtractorTest {
    @get:Throws(Exception::class)
    @get:Test
    val conferencesListTest: Unit
        get() {
            val items: List<InfoItem> = extractor!!.initialPage.getItems()
            // just test if there is an exception thrown
        }

    companion object {
        private var extractor: KioskExtractor<*>? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = MediaCCC.kioskList.getExtractorById("live", null)
            extractor!!.fetchPage()
        }
    }
}
