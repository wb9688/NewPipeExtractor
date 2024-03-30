package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderTestImpl
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptPlayerManager.getUrlWithThrottlingParameterDeobfuscated
import java.io.IOException

internal class YoutubeThrottlingParameterDeobfuscationTest {
    @BeforeEach
    @Throws(IOException::class)
    fun setup() {
        init(DownloaderTestImpl.Companion.getInstance())
        YoutubeTestsUtils.ensureStateless()
    }

    @Test
    fun testExtractFunction__success() {
        val videoIds = arrayOf("jE1USQrs1rw", "CqxjzfudGAc", "goH-9MfQI7w", "KYIdr_7H5Yw", "J1WeqmGbYeI")
        val obfuscatedUrl = "https://r6---sn-4g5ednek.googlevideo.com/videoplayback?expire=1626562120&ei=6AnzYO_YBpql1gLGkb_IBQ&ip=127.0.0.1&id=o-ANhBEf36Z5h-8U9DDddtPDqtS0ZNwf0XJAAigudKI2uI&itag=278&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278&source=youtube&requiressl=yes&vprv=1&mime=video%2Fwebm&ns=TvecOReN0vPuXb3j_zq157IG&gir=yes&clen=2915100&dur=270.203&lmt=1608157174907785&keepalive=yes&fexp=24001373,24007246&c=WEB&txp=5535432&n=N9BWSTFT7vvBJrvQ&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&alr=yes&sig=AOq0QJ8wRQIgW6XnUDKPDSxiT0_KE_tDDMpcaCJl2Un5p0Fu9qZNQGkCIQDWxsDHi_s2BEmRqIbd1C5g_gzfihB7RZLsScKWNMwzzA%3D%3D&cpn=9r2yt3BqcYmeb2Yu&cver=2.20210716.00.00&redirect_counter=1&cm2rm=sn-4g5ezy7s&cms_redirect=yes&mh=Y5&mm=34&mn=sn-4g5ednek&ms=ltu&mt=1626540524&mv=m&mvi=6&pl=43&lsparams=mh,mm,mn,ms,mv,mvi,pl&lsig=AG3C_xAwRQIhAIUzxTn9Vw1-vm-_7OQ5-0h1M6AZsY9Bx1FlCCTeMICzAiADtGggbn4Znsrh2EnvyOsGnYdRGcbxn4mW9JMOQiInDQ%3D%3D&range=259165-480735&rn=11&rbuf=20190"
        for (videoId in videoIds) {
            try {
                val deobfuscatedUrl = getUrlWithThrottlingParameterDeobfuscated(
                        videoId, obfuscatedUrl)
                Assertions.assertNotEquals(obfuscatedUrl, deobfuscatedUrl)
            } catch (e: Exception) {
                Assertions.fail<Any>("Failed to extract throttling parameter deobfuscation function or run its code for video "
                        + videoId, e)
            }
        }
    }

    @Test
    @Throws(ParsingException::class)
    fun testDecode__success() {
        // URL extracted from browser with the developer tools
        val obfuscatedUrl = "https://r6---sn-4g5ednek.googlevideo.com/videoplayback?expire=1626562120&ei=6AnzYO_YBpql1gLGkb_IBQ&ip=127.0.0.1&id=o-ANhBEf36Z5h-8U9DDddtPDqtS0ZNwf0XJAAigudKI2uI&itag=278&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C278&source=youtube&requiressl=yes&vprv=1&mime=video%2Fwebm&ns=TvecOReN0vPuXb3j_zq157IG&gir=yes&clen=2915100&dur=270.203&lmt=1608157174907785&keepalive=yes&fexp=24001373,24007246&c=WEB&txp=5535432&n=N9BWSTFT7vvBJrvQ&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&alr=yes&sig=AOq0QJ8wRQIgW6XnUDKPDSxiT0_KE_tDDMpcaCJl2Un5p0Fu9qZNQGkCIQDWxsDHi_s2BEmRqIbd1C5g_gzfihB7RZLsScKWNMwzzA%3D%3D&cpn=9r2yt3BqcYmeb2Yu&cver=2.20210716.00.00&redirect_counter=1&cm2rm=sn-4g5ezy7s&cms_redirect=yes&mh=Y5&mm=34&mn=sn-4g5ednek&ms=ltu&mt=1626540524&mv=m&mvi=6&pl=43&lsparams=mh,mm,mn,ms,mv,mvi,pl&lsig=AG3C_xAwRQIhAIUzxTn9Vw1-vm-_7OQ5-0h1M6AZsY9Bx1FlCCTeMICzAiADtGggbn4Znsrh2EnvyOsGnYdRGcbxn4mW9JMOQiInDQ%3D%3D&range=259165-480735&rn=11&rbuf=20190"
        val deobfuscatedUrl = getUrlWithThrottlingParameterDeobfuscated(
                "jE1USQrs1rw", obfuscatedUrl)
        // The deobfuscation function changes over time, so we just check if the corresponding
        // parameter changed
        Assertions.assertNotEquals(obfuscatedUrl, deobfuscatedUrl)
    }

    @Test
    @Throws(ParsingException::class)
    fun testDecode__noThrottlingParam__success() {
        val noNParamUrl = "https://r5---sn-4g5ednsz.googlevideo.com/videoplayback?expire=1626553257&ei=SefyYPmIFoKT1wLtqbjgCQ&ip=127.0.0.1&id=o-AIT5xGifsaEAdEOAb5vd06J9VNtm-KHHolnaZRGPjHZi&itag=140&source=youtube&requiressl=yes&mh=xO&mm=31%2C29&mn=sn-4g5ednsz%2Csn-4g5e6nsr&ms=au%2Crdu&mv=m&mvi=5&pl=24&initcwndbps=1322500&vprv=1&mime=audio%2Fmp4&ns=cA2SS5atEe0mH8tMwGTO4RIG&gir=yes&clen=3009275&dur=185.898&lmt=1626356984653961&mt=1626531173&fvip=5&keepalive=yes&fexp=24001373%2C24007246&beids=23886212&c=WEB&txp=6411222&sparams=expire%2Cei%2Cip%2Cid%2Citag%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&lsparams=mh%2Cmm%2Cmn%2Cms%2Cmv%2Cmvi%2Cpl%2Cinitcwndbps&lsig=AG3C_xAwRgIhAPueRlTutSlzPafxrqBmgZz5m7-Zfbw3QweDp3j4XO9SAiEA5tF7_ZCJFKmS-D6I1jlUURjpjoiTbsYyKuarV4u6E8Y%3D&sig=AOq0QJ8wRQIgRD_4WwkPeTEKGVSQqPsznMJGqq4nVJ8o1ChGBCgi4Y0CIQCZT3tI40YLKBWJCh2Q7AlvuUIpN0ficzdSElLeQpJdrw=="
        val deobfuscatedUrl = getUrlWithThrottlingParameterDeobfuscated(
                "jE1USQrs1rw", noNParamUrl)
        Assertions.assertEquals(noNParamUrl, deobfuscatedUrl)
    }
}
