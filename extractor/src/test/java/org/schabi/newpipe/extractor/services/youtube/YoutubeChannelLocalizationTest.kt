package org.schabi.newpipe.extractor.services.youtube

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.schabi.newpipe.downloader.DownloaderFactory
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.NewPipe.init
import org.schabi.newpipe.extractor.StreamingService.getChannelExtractor
import org.schabi.newpipe.extractor.StreamingService.supportedLocalizations
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.getChannelTabExtractor
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService.getChannelTabExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService.getChannelTabExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

/**
 * A class that tests multiple channels and ranges of "time ago".
 */
class YoutubeChannelLocalizationTest {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    @Test
    @Throws(Exception::class)
    fun testAllSupportedLocalizations() {
        YoutubeTestsUtils.ensureStateless()
        init(DownloaderFactory.getDownloader(RESOURCE_PATH + "localization"))
        testLocalizationsFor("https://www.youtube.com/user/NBCNews")
        testLocalizationsFor("https://www.youtube.com/channel/UCcmpeVbSSQlZRvHfdC-CRwg/videos")
        testLocalizationsFor("https://www.youtube.com/channel/UC65afEgL62PGFWXY7n6CUbA")
        testLocalizationsFor("https://www.youtube.com/channel/UCEOXxzW2vU0P-0THehuIIeg")
    }

    @Throws(Exception::class)
    private fun testLocalizationsFor(channelUrl: String) {
        val supportedLocalizations: List<Localization> = YouTube.supportedLocalizations
        // final List<Localization> supportedLocalizations = Arrays.asList(Localization.DEFAULT, new Localization("sr"));
        val results: MutableMap<Localization, List<StreamInfoItem>> = LinkedHashMap()
        for (currentLocalization in supportedLocalizations) {
            if (DEBUG) println("Testing localization = $currentLocalization")
            var itemsPage: InfoItemsPage<InfoItem?>?
            itemsPage = try {
                val extractor: ChannelExtractor = YouTube.getChannelExtractor(channelUrl)
                extractor.forceLocalization(currentLocalization)
                extractor.fetchPage()

                // Use Videos tab only
                val tabExtractor: ChannelTabExtractor = YouTube.getChannelTabExtractor(
                        extractor.tabs[0])
                tabExtractor.fetchPage()
                DefaultTests.defaultTestRelatedItems<InfoItem>(tabExtractor)
            } catch (e: Throwable) {
                println("[!] $currentLocalization → failed")
                throw e
            }
            val items = itemsPage!!.items
                    .stream()
                    .filter { o: InfoItem? -> StreamInfoItem::class.java.isInstance(o) }
                    .map { obj: InfoItem? -> StreamInfoItem::class.java.cast(obj) }
                    .collect(Collectors.toUnmodifiableList())
            for (i in items.indices) {
                val item = items[i]
                var debugMessage = """[${String.format("%02d", i)}] ${currentLocalization.localizationCode} → ${item.name}
:::: ${item.streamType}, views = ${item.viewCount}"""
                val uploadDate = item.uploadDate
                if (uploadDate != null) {
                    val dateAsText = dateTimeFormatter.format(uploadDate.offsetDateTime())
                    debugMessage += """
                        
                        :::: ${item.textualUploadDate}
                        :::: $dateAsText
                        """.trimIndent()
                }
                if (DEBUG) println(debugMessage + "\n")
            }
            results[currentLocalization] = items
            if (DEBUG) println("\n===============================\n")
        }


        // Check results
        val referenceList = results[Localization.DEFAULT]!!
        var someFail = false
        for ((key, currentList) in results) {
            if (key.equals(Localization.DEFAULT)) {
                continue
            }
            val currentLocalizationCode = key.localizationCode
            val referenceLocalizationCode = Localization.DEFAULT.localizationCode
            if (DEBUG) {
                println("Comparing " + referenceLocalizationCode + " with " +
                        currentLocalizationCode)
            }
            if (referenceList.size != currentList.size) {
                if (DEBUG) println("[!] $currentLocalizationCode → Lists are not equal")
                someFail = true
                continue
            }
            for (i in 0 until referenceList.size - 1) {
                val referenceItem = referenceList[i]
                val currentItem = currentList[i]
                val referenceUploadDate = referenceItem.uploadDate
                val currentUploadDate = currentItem.uploadDate
                val referenceDateString = if (referenceUploadDate == null) "null" else dateTimeFormatter.format(referenceUploadDate.offsetDateTime())
                val currentDateString = if (currentUploadDate == null) "null" else dateTimeFormatter.format(currentUploadDate.offsetDateTime())
                var difference: Long = -1
                if (referenceUploadDate != null && currentUploadDate != null) {
                    difference = ChronoUnit.MILLIS.between(referenceUploadDate.offsetDateTime(), currentUploadDate.offsetDateTime())
                }
                val areTimeEquals = difference < 5 * 60 * 1000L
                if (!areTimeEquals) {
                    println("""      [!] $currentLocalizationCode → [$i] dates are not equal
          $referenceLocalizationCode: $referenceDateString → ${referenceItem.textualUploadDate}
          $currentLocalizationCode: $currentDateString → ${currentItem.textualUploadDate}""")
                }
            }
        }
        if (someFail) {
            Assertions.fail<Any>("Some localization failed")
        } else {
            if (DEBUG) print("""
    All tests passed
    
    ===============================
    
    
    """.trimIndent())
        }
    }

    companion object {
        private const val RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/channel/"
        private const val DEBUG = false
    }
}
