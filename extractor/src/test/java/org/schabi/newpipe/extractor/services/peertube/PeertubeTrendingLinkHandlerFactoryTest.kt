package org.schabi.newpipe.extractor.services.peertube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeTrendingLinkHandlerFactory

/**
 * Test for [PeertubeTrendingLinkHandlerFactory]
 */
class PeertubeTrendingLinkHandlerFactoryTest {
    @get:Throws(Exception::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals(LinkHandlerFactory!!.fromId("Trending").url, "https://peertube.mastodon.host/api/v1/videos?sort=-trending")
            Assertions.assertEquals(LinkHandlerFactory!!.fromId("Most liked").url, "https://peertube.mastodon.host/api/v1/videos?sort=-likes")
            Assertions.assertEquals(LinkHandlerFactory!!.fromId("Recently added").url, "https://peertube.mastodon.host/api/v1/videos?sort=-publishedAt")
            Assertions.assertEquals(LinkHandlerFactory!!.fromId("Local").url, "https://peertube.mastodon.host/api/v1/videos?sort=-publishedAt&isLocal=true")
        }

    @get:Throws(Exception::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals(LinkHandlerFactory!!.fromUrl("https://peertube.mastodon.host/videos/trending")!!.id, "Trending")
            Assertions.assertEquals(LinkHandlerFactory!!.fromUrl("https://peertube.mastodon.host/videos/most-liked")!!.id, "Most liked")
            Assertions.assertEquals(LinkHandlerFactory!!.fromUrl("https://peertube.mastodon.host/videos/recently-added")!!.id, "Recently added")
            Assertions.assertEquals(LinkHandlerFactory!!.fromUrl("https://peertube.mastodon.host/videos/local")!!.id, "Local")
        }

    @Test
    @Throws(ParsingException::class)
    fun acceptUrl() {
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/trending"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/trending?adsf=fjaj#fhe"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/most-liked"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/most-liked?adsf=fjaj#fhe"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/recently-added"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/recently-added?adsf=fjaj#fhe"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/local"))
        Assertions.assertTrue(LinkHandlerFactory!!.acceptUrl("https://peertube.mastodon.host/videos/local?adsf=fjaj#fhe"))
        PeertubeLinkHandlerFactoryTestHelper.assertDoNotAcceptNonURLs(LinkHandlerFactory)
    }

    companion object {
        private var LinkHandlerFactory: LinkHandlerFactory? = null
        @BeforeAll
        @Throws(Exception::class)
        fun setUp() {
            // setting instance might break test when running in parallel
            PeerTube.instance = PeertubeInstance("https://peertube.mastodon.host", "PeerTube on Mastodon.host")
            LinkHandlerFactory = PeertubeTrendingLinkHandlerFactory.getInstance()
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}
