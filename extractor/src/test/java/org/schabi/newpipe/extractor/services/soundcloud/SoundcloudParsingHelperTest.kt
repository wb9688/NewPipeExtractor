package org.schabi.newpipe.extractor.services.soundcloud

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.resolveIdWithWidgetApi
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.resolveUrlWithEmbedPlayer

internal class SoundcloudParsingHelperTest {
    @Test
    @Throws(Exception::class)
    fun resolveUrlWithEmbedPlayerTest() {
        Assertions.assertEquals("https://soundcloud.com/trapcity", resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/26057743"))
        Assertions.assertEquals("https://soundcloud.com/nocopyrightsounds", resolveUrlWithEmbedPlayer("https://api.soundcloud.com/users/16069159"))
        Assertions.assertEquals("https://soundcloud.com/trapcity", resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/26057743"))
        Assertions.assertEquals("https://soundcloud.com/nocopyrightsounds", resolveUrlWithEmbedPlayer("https://api-v2.soundcloud.com/users/16069159"))
    }

    @Test
    @Throws(Exception::class)
    fun resolveIdWithWidgetApiTest() {
        Assertions.assertEquals("26057743", resolveIdWithWidgetApi("https://soundcloud.com/trapcity"))
        Assertions.assertEquals("16069159", resolveIdWithWidgetApi("https://soundcloud.com/nocopyrightsounds"))
    }

    companion object {
        @BeforeAll
        fun setUp() {
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}