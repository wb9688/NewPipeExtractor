package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeCommentsLinkHandlerFactory

class YoutubeCommentsLinkHandlerFactoryTest {
    @get:Test
    val idWithNullAsUrl: Unit
        get() {
            Assertions.assertThrows(NullPointerException::class.java) { linkHandler!!.fromId(null) }
        }

    @get:Throws(ParsingException::class)
    @get:Test
    val idFromYt: Unit
        get() {
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://www.youtube.com/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://m.youtube.com/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://youtube.com/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://youtu.be/VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://youtu.be/VM_6n762j6M&t=20")!!.id)
        }

    @Test
    @Throws(ParsingException::class)
    fun testAcceptUrl() {
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://www.youtube.com/watch?v=VM_6n762j6M&t=20"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://WWW.youtube.com/watch?v=VM_6n762j6M&t=20"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://youtube.com/watch?v=VM_6n762j6M&t=20"))
        Assertions.assertTrue(linkHandler!!.acceptUrl("https://youtu.be/VM_6n762j6M&t=20"))
    }

    @Test
    @Throws(ParsingException::class)
    fun testDeniesUrl() {
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://www.you com/watch?v=VM_6n762j6M"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("https://com/watch?v=VM_6n762j6M"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("htt ://com/watch?v=VM_6n762j6M"))
        Assertions.assertFalse(linkHandler!!.acceptUrl("ftp://www.youtube.com/watch?v=VM_6n762j6M"))
    }

    @get:Throws(ParsingException::class)
    @get:Test
    val idFromInvidious: Unit
        get() {
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://www.invidio.us/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://invidio.us/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://INVIDIO.US/watch?v=VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://invidio.us/VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://invidio.us/VM_6n762j6M&t=20")!!.id)
        }

    @get:Throws(ParsingException::class)
    @get:Test
    val idFromY2ube: Unit
        get() {
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://y2u.be/VM_6n762j6M")!!.id)
            Assertions.assertEquals("VM_6n762j6M", linkHandler!!.fromUrl("https://Y2U.Be/VM_6n762j6M")!!.id)
        }

    companion object {
        private var linkHandler: YoutubeCommentsLinkHandlerFactory? = null
        @BeforeAll
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
            linkHandler = YoutubeCommentsLinkHandlerFactory.getInstance()
        }
    }
}
