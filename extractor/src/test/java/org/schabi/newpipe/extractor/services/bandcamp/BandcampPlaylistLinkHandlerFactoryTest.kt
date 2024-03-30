// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampPlaylistLinkHandlerFactory

/**
 * Test for [BandcampPlaylistLinkHandlerFactory]
 */
class BandcampPlaylistLinkHandlerFactoryTest {
    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        Assertions.assertFalse(linkHandler!!.acceptUrl("http://interovgm.com/releases/"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://interovgm.com/releases"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("http://zachbenson.bandcamp.com"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://bandcamp.com"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/track/kitchen"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://interovgm.com/track/title"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://example.com/album/samplealbum"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://powertothequeerkids.bandcamp.com/album/power-to-the-queer-kids"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://zachbenson.bandcamp.com/album/prom"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://MACBENSON.BANDCAMP.COM/ALBUM/COMING-OF-AGE"))
    }

    companion object {
        private var linkHandler: BandcampPlaylistLinkHandlerFactory? = null
        @BeforeAll
        fun setUp() {
            linkHandler = BandcampPlaylistLinkHandlerFactory.getInstance()
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}
