package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.kiosk.KioskInfo
import org.schabi.newpipe.extractor.kiosk.KioskInfo.Companion.getInfo
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory

/*
* Created by Christian Schabesberger on 12.08.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* YoutubeTrendingKioskInfoTest.java is part of NewPipe Extractor.
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
 * Test for [KioskInfo]
 */
class YoutubeTrendingKioskInfoTest {
    @get:Test
    val streams: Unit
        get() {
            Assertions.assertFalse(kioskInfo!!.relatedItems!!.isEmpty())
        }

    @get:Test
    val id: Unit
        get() {
            Assertions.assertTrue(kioskInfo!!.id == "Trending" || kioskInfo!!.id == "Trends")
        }

    @get:Test
    val name: Unit
        get() {
            Assertions.assertFalse(kioskInfo!!.name!!.isEmpty())
        }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "kiosk"
        var kioskInfo: KioskInfo? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            YoutubeTestsUtils.ensureStateless()
            init(DownloaderFactory.getDownloader(RESOURCE_PATH))
            val LinkHandlerFactory: LinkHandlerFactory = (YouTube as StreamingService).kioskList.getListLinkHandlerFactoryByType("Trending")
            kioskInfo = getInfo(YouTube, LinkHandlerFactory.fromId("Trending").url!!)
        }
    }
}
