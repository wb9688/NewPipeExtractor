package org.schabi.newpipe.extractor.services

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.StreamingService

abstract class DefaultExtractorTest<T : Extractor?> : BaseExtractorTest {
    @Throws(Exception::class)
    abstract fun extractor(): T?
    @Throws(Exception::class)
    abstract fun expectedService(): StreamingService
    @Throws(Exception::class)
    abstract fun expectedName(): String
    @Throws(Exception::class)
    abstract fun expectedId(): String
    @Throws(Exception::class)
    abstract fun expectedUrlContains(): String
    @Throws(Exception::class)
    abstract fun expectedOriginalUrlContains(): String
    @Test
    @Throws(Exception::class)
    override fun testServiceId() {
        Assertions.assertEquals(expectedService().serviceId, extractor()!!.serviceId)
    }

    @Test
    @Throws(Exception::class)
    override fun testName() {
        assertEquals(expectedName(), extractor()!!.name)
    }

    @Test
    @Throws(Exception::class)
    override fun testId() {
        Assertions.assertEquals(expectedId(), extractor()!!.id)
    }

    @Test
    @Throws(Exception::class)
    override fun testUrl() {
        val url = extractor()!!.url
        ExtractorAsserts.assertIsSecureUrl(url)
        ExtractorAsserts.assertContains(expectedUrlContains(), url)
    }

    @Test
    @Throws(Exception::class)
    override fun testOriginalUrl() {
        val originalUrl = extractor()!!.originalUrl
        ExtractorAsserts.assertIsSecureUrl(originalUrl)
        ExtractorAsserts.assertContains(expectedOriginalUrlContains(), originalUrl)
    }
}
