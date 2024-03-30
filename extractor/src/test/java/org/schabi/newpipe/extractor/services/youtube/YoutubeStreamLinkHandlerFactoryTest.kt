package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory

/**
 * Test for [YoutubeStreamLinkHandlerFactory]
 */
class YoutubeStreamLinkHandlerFactoryTest {
    @get:Test
    val idWithNullAsUrl: Unit
        get() {
            Assertions.assertThrows(NullPointerException::class.java) { linkHandler!!.fromId(null) }
        }

    @get:Test
    val idForAd: Unit
        get() {
            Assertions.assertThrows(FoundAdException::class.java) {
                linkHandler!!.fromUrl(
                        "https://googleads.g.doubleclick.net/aclk?sa=l&ai=C-2IPgeVTWPf4GcOStgfOnIOADf78n61GvKmmobYDrgIQASDj-5MDKAJg9ZXOgeAEoAGgy_T-A8gBAakC2gkpmquIsT6oAwGqBJMBT9BgD5kVgbN0dX602bFFaDw9vsxq-We-S8VkrXVBi6W_e7brZ36GCz1WO3EPEeklYuJjXLUowwCOKsd-8xr1UlS_tusuFJv9iX35xoBHKTRvs8-0aDbfEIm6in37QDfFuZjqgEMB8-tg0Jn_Pf1RU5OzbuU40B4Gy25NUTnOxhDKthOhKBUSZEksCEerUV8GMu10iAXCxquwApIFBggDEAEYAaAGGsgGlIjthrUDgAfItIsBqAemvhvYBwHSCAUIgGEQAbgT6AE&num=1&sig=AOD64_1DybDd4qAm5O7o9UAbTNRdqXXHFQ&ctype=21&video_id=dMO_IXYPZew&client=ca-pub-6219811747049371&adurl=http://www.youtube.com/watch%3Fv%3DdMO_IXYPZew")
            }
        }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.youtube.com/watch?v=jZViOEv90d", "https://www.youtube.com/watchjZViOEv90d", "https://www.youtube.com/", "https://www.youtube.com/channel/UCBR8-60-B28hp2BmDPdntcQ", "https://invidious.fdn.fr/channel/UCBR8-60-B28hp2BmDPdntcQ"])
    fun getIdForInvalidUrls(invalidUrl: String?) {
        Assertions.assertThrows(ParsingException::class.java) { linkHandler!!.fromUrl(invalidUrl)!!.id }
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.youtube.com/watch?v=9Dpqou5cI08", "https://www.youtube.com/watch?v=9Dpqou5cI08&t=100", "https://WWW.YouTube.com/watch?v=9Dpqou5cI08&t=100", "HTTPS://www.youtube.com/watch?v=9Dpqou5cI08&t=100", "https://youtu.be/9Dpqou5cI08?t=9s", "HTTPS://Youtu.be/9Dpqou5cI08?t=9s", "https://www.youtube.com/embed/9Dpqou5cI08", "https://www.youtube-nocookie.com/embed/9Dpqou5cI08", "http://www.youtube.com/watch?v=9Dpqou5cI08", "http://youtube.com/watch?v=9Dpqou5cI08", "http://youtu.be/9Dpqou5cI08?t=9s", "http://www.youtube.com/embed/9Dpqou5cI08", "http://www.Youtube.com/embed/9Dpqou5cI08", "http://www.youtube-nocookie.com/embed/9Dpqou5cI08", "vnd.youtube://www.youtube.com/watch?v=9Dpqou5cI08", "vnd.youtube:9Dpqou5cI08"])
    @Throws(Exception::class)
    fun getId_9Dpqou5cI08_fromYt(url: String?) {
        Assertions.assertEquals("9Dpqou5cI08", linkHandler!!.fromUrl(url)!!.id)
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.youtube.com/shorts/IOS2fqxwYbA", "http://www.youtube.com/shorts/IOS2fqxwYbA", "http://www.youtube.com/v/IOS2fqxwYbA", "https://www.youtube.com/w/IOS2fqxwYbA", "https://www.youtube.com/watch/IOS2fqxwYbA"])
    @Throws(Exception::class)
    fun getId_IOS2fqxwYbA_fromYt(url: String?) {
        Assertions.assertEquals("IOS2fqxwYbA", linkHandler!!.fromUrl(url)!!.id)
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://m.youtube.com/watch?v=-cdveCh1kQk)", "https://www.youtube.com/watch?v=-cdveCh1kQk-", "https://WWW.YouTube.com/watch?v=-cdveCh1kQkwhatever", "https://youtu.be/-cdveCh1kQk)hello", "HTTPS://youtu.be/-cdveCh1kQk)"])
    @Throws(Exception::class)
    fun getId_cdveCh1kQk_fromYt(url: String?) {
        Assertions.assertEquals("-cdveCh1kQk", linkHandler!!.fromUrl(url)!!.id)
    }

    @ParameterizedTest
    @CsvSource("W-fFHeTX70Q,https://www.youtube.com/watch?v=W-fFHeTX70Q", "uEJuoEs1UxY,http://www.youtube.com/watch_popup?v=uEJuoEs1UxY", "uEJuoEs1UxY,http://www.Youtube.com/watch_popup?v=uEJuoEs1UxY", "7_WWz2DSnT8,https://youtu.be/7_WWz2DSnT8", "oy6NvWeVruY,https://m.youtube.com/watch?v=oy6NvWeVruY", "EhxJLojIE_o,http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare", "n8X9_MgEdCg,vnd.youtube://n8X9_MgEdCg", "O0EDx9WAelc,https://music.youtube.com/watch?v=O0EDx9WAelc", "O0EDx9WAelc,HTTPS://www.youtube.com/watch?v=O0EDx9WAelc]", "OGS7c0-CmRs,https://YouTu.be/OGS7c0-CmRswhatever)")
    @Throws(Exception::class)
    fun getId_diverse_fromYt(expectedId: String?, url: String?) {
        Assertions.assertEquals(expectedId, linkHandler!!.fromUrl(url)!!.id)
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://www.youtube.com/watch?v=9Dpqou5cI08", "https://www.youtube.com/watch?v=9Dpqou5cI08&t=100", "https://WWW.YouTube.com/watch?v=9Dpqou5cI08&t=100", "HTTPS://www.youtube.com/watch?v=9Dpqou5cI08&t=100", "https://youtu.be/9Dpqou5cI08?t=9s", "https://www.youtube.com/embed/9Dpqou5cI08", "https://www.youtube-nocookie.com/embed/9Dpqou5cI08", "http://www.youtube.com/watch?v=9Dpqou5cI08", "http://youtu.be/9Dpqou5cI08?t=9s", "http://www.youtube.com/embed/9Dpqou5cI08", "http://www.youtube-nocookie.com/embed/9Dpqou5cI08", "http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare", "vnd.youtube://www.youtube.com/watch?v=9Dpqou5cI08", "vnd.youtube:9Dpqou5cI08", "vnd.youtube.launch:9Dpqou5cI08", "https://music.youtube.com/watch?v=O0EDx9WAelc", "https://www.youtube.com/shorts/IOS2fqxwYbA", "http://www.youtube.com/shorts/IOS2fqxwYbA", "http://www.youtube.com/v/IOS2fqxwYbA", "https://www.youtube.com/w/IOS2fqxwYbA", "https://www.youtube.com/watch/IOS2fqxwYbA", "https://www.youtube.com/live/rUxyKA_-grg"])
    @Throws(ParsingException::class)
    fun acceptYtUrl(url: String?) {
        Assertions.assertTrue(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://hooktube.com/watch?v=TglNG-yjabU", "http://hooktube.com/watch?v=TglNG-yjabU", "https://hooktube.com/watch?v=ocH3oSnZG3c&test=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2", "hooktube.com/watch?v=3msbfr6pBNE", "hooktube.com/watch/3msbfr6pBNE", "hooktube.com/v/3msbfr6pBNE", "hooktube.com/embed/3msbfr6pBNE"])
    @Throws(ParsingException::class)
    fun acceptHookUrl(url: String?) {
        Assertions.assertTrue(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @CsvSource("TglNG-yjabU,https://hooktube.com/watch?v=TglNG-yjabU", "TglNG-yjabU,http://hooktube.com/watch?v=TglNG-yjabU", "ocH3oSnZG3c,https://hooktube.com/watch?v=ocH3oSnZG3c&test=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2", "3msbfr6pBNE,hooktube.com/watch?v=3msbfr6pBNE", "3msbfr6pBNE,hooktube.com/watch/3msbfr6pBNE", "3msbfr6pBNE,hooktube.com/v/3msbfr6pBNE", "3msbfr6pBNE,hooktube.com/w/3msbfr6pBNE", "3msbfr6pBNE,hooktube.com/embed/3msbfr6pBNE")
    @Throws(Exception::class)
    fun getHookIdfromUrl(expectedId: String?, url: String?) {
        Assertions.assertEquals(expectedId, linkHandler!!.fromUrl(url)!!.id)
    }

    @ParameterizedTest
    @ValueSource(strings = ["https://invidious.fdn.fr/watch?v=TglNG-yjabU", "http://www.invidio.us/watch?v=TglNG-yjabU", "http://invidious.fdn.fr/watch?v=TglNG-yjabU", "invidious.fdn.fr/watch?v=3msbfr6pBNE", "https://invidious.fdn.fr/watch?v=ocH3oSnZG3c&test=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2", "invidious.fdn.fr/embed/3msbfr6pBNE", "invidious.fdn.fr/watch/3msbfr6pBNE", "invidious.fdn.fr/v/3msbfr6pBNE", "invidious.fdn.fr/w/3msbfr6pBNE"])
    @Throws(ParsingException::class)
    fun acceptInvidiousUrl(url: String?) {
        Assertions.assertTrue(linkHandler!!.acceptUrl(url))
    }

    @ParameterizedTest
    @CsvSource("TglNG-yjabU,https://invidious.fdn.fr/watch?v=TglNG-yjabU", "TglNG-yjabU,http://www.invidio.us/watch?v=TglNG-yjabU", "TglNG-yjabU,http://invidious.fdn.fr/watch?v=TglNG-yjabU", "ocH3oSnZG3c,https://invidious.fdn.fr/watch?v=ocH3oSnZG3c&test=PLS2VU1j4vzuZwooPjV26XM9UEBY2CPNn2", "3msbfr6pBNE,invidious.fdn.fr/watch?v=3msbfr6pBNE", "3msbfr6pBNE,invidious.fdn.fr/embed/3msbfr6pBNE", "3msbfr6pBNE,invidious.fdn.fr/v/3msbfr6pBNE", "3msbfr6pBNE,invidious.fdn.fr/w/3msbfr6pBNE", "3msbfr6pBNE,invidious.fdn.fr/watch/3msbfr6pBNE")
    @Throws(Exception::class)
    fun getInvidiousIdfromUrl(expectedId: String?, url: String?) {
        Assertions.assertEquals(expectedId, linkHandler!!.fromUrl(url)!!.id)
    }

    companion object {
        private var linkHandler: YoutubeStreamLinkHandlerFactory? = null
        @BeforeAll
        fun setUp() {
            linkHandler = YoutubeStreamLinkHandlerFactory.getInstance()
            init(DownloaderTestImpl.Companion.getInstance())
        }
    }
}