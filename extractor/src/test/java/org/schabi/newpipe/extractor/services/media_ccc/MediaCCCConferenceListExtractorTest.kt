package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache.size

/**
 * Test [MediaCCCConferenceKiosk]
 */
class MediaCCCConferenceListExtractorTest {
    @get:Throws(Exception::class)
    @get:Test
    val conferencesListTest: Unit
        get() {
            assertGreaterOrEqual(174, extractor!!.initialPage.getItems().size())
        }

    @ParameterizedTest
    @ValueSource(strings = ["FrOSCon 2016", "ChaosWest @ 35c3", "CTreffOS chaOStalks", "Datenspuren 2015", "Chaos Singularity 2017", "SIGINT10", "Vintage Computing Festival Berlin 2015", "FIfFKon 2015", "33C3: trailers", "Blinkenlights"])
    @Throws(Exception::class)
    fun conferenceTypeTest(name: String) {
        val itemList: List<InfoItem> = extractor!!.initialPage.getItems()
        Assertions.assertTrue(itemList.stream().anyMatch { item: InfoItem -> name == item.name })
    }

    companion object {
        private var extractor: KioskExtractor<*>? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUpClass() {
            init(DownloaderTestImpl.Companion.getInstance())
            extractor = MediaCCC.kioskList.getExtractorById("conferences", null)
            extractor!!.fetchPage()
        }
    }
}
