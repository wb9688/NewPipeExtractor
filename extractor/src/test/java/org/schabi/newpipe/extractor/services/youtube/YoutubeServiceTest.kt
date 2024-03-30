package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.StreamingService.getPlaylistExtractor
import org.schabi.newpipe.extractor.kiosk.KioskList
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor

/*
* Created by Christian Schabesberger on 29.12.15.
*
* Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* YoutubeSearchExtractorStreamTest.java is part of NewPipe Extractor.
*
* NewPipe Extractor is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* NewPipe Extractor is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
*/ /**
 * Test for [YoutubeService]
 */
class YoutubeServiceTest {
    @Test
    fun testGetKioskAvailableKiosks() {
        Assertions.assertFalse(kioskList!!.availableKiosks.isEmpty(), "No kiosk got returned")
    }

    @Test
    @Throws(Exception::class)
    fun testGetDefaultKiosk() {
        Assertions.assertEquals(kioskList!!.getDefaultKioskExtractor(null)!!.getId(), "Trending")
    }

    @get:Throws(Exception::class)
    @get:Test
    val playListExtractorIsNormalPlaylist: Unit
        get() {
            val extractor = service!!.getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=JhqtYOnNrTs&list=PL-EkZZikQIQVqk9rBWzEo5b-2GeozElS")
            Assertions.assertTrue(extractor is YoutubePlaylistExtractor)
        }

    @get:Throws(Exception::class)
    @get:Test
    val playlistExtractorIsMix: Unit
        get() {
            val videoId = "_AzeUSL9lZc"
            var extractor: PlaylistExtractor = YouTube.getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=$videoId&list=RD$videoId")
            Assertions.assertTrue(extractor is YoutubeMixPlaylistExtractor)
            extractor = YouTube.getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=$videoId&list=RDMM$videoId")
            Assertions.assertTrue(extractor is YoutubeMixPlaylistExtractor)
            val mixVideoId = "qHtzO49SDmk"
            extractor = YouTube.getPlaylistExtractor(
                    "https://www.youtube.com/watch?v=$mixVideoId&list=RD$videoId")
            Assertions.assertTrue(extractor is YoutubeMixPlaylistExtractor)
        }

    companion object {
        var service: StreamingService? = null
        var kioskList: KioskList? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            service = YouTube
            kioskList = service!!.kioskList
        }
    }
}
