package org.schabi.newpipe.extractor.services.media_ccc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory

class MediaCCCConferenceLinkHandlerFactoryTest {
    @get:Throws(ParsingException::class)
    @get:Test
    val id: Unit
        get() {
            Assertions.assertEquals("jh20",
                    linkHandler!!.fromUrl("https://media.ccc.de/c/jh20#278")!!.id)
            Assertions.assertEquals("jh20",
                    linkHandler!!.fromUrl("https://media.ccc.de/b/jh20?a=b")!!.id)
            Assertions.assertEquals("jh20",
                    linkHandler!!.fromUrl("https://api.media.ccc.de/public/conferences/jh20&a=b&b=c")!!.id)
        }

    @get:Throws(ParsingException::class)
    @get:Test
    val url: Unit
        get() {
            Assertions.assertEquals("https://media.ccc.de/c/jh20",
                    linkHandler!!.fromUrl("https://media.ccc.de/c/jh20#278")!!.url)
            Assertions.assertEquals("https://media.ccc.de/c/jh20",
                    linkHandler!!.fromUrl("https://media.ccc.de/b/jh20?a=b")!!.url)
            Assertions.assertEquals("https://media.ccc.de/c/jh20",
                    linkHandler!!.fromUrl("https://api.media.ccc.de/public/conferences/jh20&a=b&b=c")!!.url)
            Assertions.assertEquals("https://media.ccc.de/c/jh20",
                    linkHandler!!.fromId("jh20").url)
        }

    companion object {
        private var linkHandler: MediaCCCConferenceLinkHandlerFactory? = null
        @BeforeAll
        fun setUp() {
            linkHandler = MediaCCCConferenceLinkHandlerFactory.getInstance()
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}
