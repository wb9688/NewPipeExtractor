package org.schabi.newpipe.extractor.services

import org.junit.jupiter.api.Test

interface BaseListExtractorTest : BaseExtractorTest {
    @Test
    @Throws(Exception::class)
    fun testRelatedItems()

    @Test
    @Throws(Exception::class)
    fun testMoreRelatedItems()
}
