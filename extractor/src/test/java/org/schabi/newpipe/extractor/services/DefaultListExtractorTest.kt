package org.schabi.newpipe.extractor.services

import org.junit.jupiter.api.Test
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItem.InfoType
import org.schabi.newpipe.extractor.ListExtractor

abstract class DefaultListExtractorTest<T : ListExtractor<out InfoItem?>?> : DefaultExtractorTest<T>(), BaseListExtractorTest {
    open fun expectedInfoItemType(): InfoType? {
        return null
    }

    open fun expectedHasMoreItems(): Boolean {
        return true
    }

    @Test
    @Throws(Exception::class)
    override fun testRelatedItems() {
        val extractor: ListExtractor<out InfoItem>? = extractor()
        val expectedType = expectedInfoItemType()
        val items = DefaultTests.defaultTestRelatedItems(extractor)
        if (expectedType != null) {
            DefaultTests.assertOnlyContainsType(items, expectedType)
        }
    }

    @Test
    @Throws(Exception::class)
    override fun testMoreRelatedItems() {
        val extractor: ListExtractor<out InfoItem>? = extractor()
        if (expectedHasMoreItems()) {
            val expectedType = expectedInfoItemType()
            val items = DefaultTests.defaultTestMoreItems(extractor)
            if (expectedType != null) {
                DefaultTests.assertOnlyContainsType(items, expectedType)
            }
        } else {
            DefaultTests.assertNoMoreItems(extractor)
        }
    }
}
