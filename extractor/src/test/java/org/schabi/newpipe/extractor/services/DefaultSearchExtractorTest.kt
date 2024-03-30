package org.schabi.newpipe.extractor.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty
import java.net.MalformedURLException
import java.util.stream.Collectors

abstract class DefaultSearchExtractorTest : DefaultListExtractorTest<SearchExtractor?>(), BaseSearchExtractorTest {
    abstract fun expectedSearchString(): String
    abstract fun expectedSearchSuggestion(): String?
    open val isCorrectedSearch: Boolean
        get() = false

    @Throws(MalformedURLException::class)
    open fun expectedMetaInfo(): List<MetaInfo> {
        return emptyList()
    }

    @Test
    @Throws(Exception::class)
    override fun testSearchString() {
        Assertions.assertEquals(expectedSearchString(), extractor()!!.searchString)
    }

    @Test
    @Throws(Exception::class)
    override fun testSearchSuggestion() {
        val expectedSearchSuggestion = expectedSearchSuggestion()
        if (isNullOrEmpty(expectedSearchSuggestion)) {
            ExtractorAsserts.assertEmpty("Suggestion was expected to be empty", extractor()!!.searchSuggestion)
        } else {
            assertEquals(expectedSearchSuggestion, extractor()!!.searchSuggestion)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testSearchCorrected() {
        assertEquals(isCorrectedSearch, extractor()!!.isCorrectedSearch)
    }

    /**
     * @see DefaultStreamExtractorTest.testMetaInfo
     */
    @Test
    @Throws(Exception::class)
    open fun testMetaInfo() {
        val metaInfoList = extractor()!!.metaInfo
        val expectedMetaInfoList = expectedMetaInfo()
        for (expectedMetaInfo in expectedMetaInfoList) {
            val texts = metaInfoList!!.stream()
                    .map { metaInfo: MetaInfo? -> metaInfo!!.content!!.content }
                    .collect(Collectors.toList())
            val titles = metaInfoList.stream().map<Any?>(MetaInfo::getTitle).collect(Collectors.toList<Any?>())
            val urls = metaInfoList.stream().flatMap { info: MetaInfo? -> info!!.getUrls().stream() }
                    .collect(Collectors.toList())
            val urlTexts = metaInfoList.stream().flatMap { info: MetaInfo? -> info!!.getUrlTexts().stream() }
                    .collect(Collectors.toList())
            Assertions.assertTrue(texts.contains(expectedMetaInfo.content!!.content))
            Assertions.assertTrue(titles.contains(expectedMetaInfo.title))
            for (expectedUrlText in expectedMetaInfo.getUrlTexts()) {
                Assertions.assertTrue(urlTexts.contains(expectedUrlText))
            }
            for (expectedUrl in expectedMetaInfo.getUrls()) {
                Assertions.assertTrue(urls.contains(expectedUrl))
            }
        }
    }
}
