package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskList.getListLinkHandlerFactoryByType
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.BandcampService.kioskList
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.kioskList
import org.schabi.newpipe.extractor.services.peertube.PeertubeService.kioskList
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService.kioskList
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.kioskList

/*
* Created by Christian Schabesberger on 12.08.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* YoutubeTrendingLinkHandlerFactoryTest.java is part of NewPipe Extractor.
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
 * Test for [YoutubeTrendingLinkHandlerFactory]
 */
class YoutubeTrendingLinkHandlerFactoryTest {
    @get:Throws(Exception::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals(LinkHandlerFactory!!.fromId("").url, "https://www.youtube.com/feed/trending")
        }

    @get:Throws(Exception::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals(LinkHandlerFactory!!.fromUrl("https://www.youtube.com/feed/trending")!!.id, "Trending")
        }

    @Test
    @Throws(ParsingException::class)
    fun acceptUrl() {
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://www.youtube.com/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://www.youtube.com/feed/trending?adsf=fjaj#fhe"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("http://www.youtube.com/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("www.youtube.com/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("youtube.com/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("youtube.com/feed/trending?akdsakjf=dfije&kfj=dkjak"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://youtube.com/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("m.youtube.com/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://www.invidio.us/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://invidio.us/feed/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("invidio.us/feed/trending"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("https://youtu.be/feed/trending"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("kdskjfiiejfia"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("https://www.youtube.com/bullshit/feed/trending"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("https://www.youtube.com/feed/trending/bullshit"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("https://www.youtube.com/feed/bullshit/trending"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("peter klaut aepferl youtube.com/feed/trending"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("youtube.com/feed/trending askjkf"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("askdjfi youtube.com/feed/trending askjkf"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("    youtube.com/feed/trending"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl("https://www.youtube.com/feed/trending.html"))
        Assertions.assertFalse(LinkHandlerFactory!!.acceptUrl(""))
    }

    companion object {
        private var LinkHandlerFactory: LinkHandlerFactory? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            LinkHandlerFactory = YouTube.kioskList.getListLinkHandlerFactoryByType("Trending")
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}
