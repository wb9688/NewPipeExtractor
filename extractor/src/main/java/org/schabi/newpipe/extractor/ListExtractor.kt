package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import java.io.IOException

/**
 * Base class to extractors that have a list (e.g. playlists, users).
 * @param <R> the info item type this list extractor provides
</R> */
abstract class ListExtractor<R : InfoItem?>(service: StreamingService, linkHandler: ListLinkHandler?) : Extractor(service, linkHandler) {
    @JvmField
    @get:Throws(IOException::class, ExtractionException::class)
    abstract val initialPage: InfoItemsPage<R?>?

    /**
     * Get a list of items corresponding to the specific requested page.
     *
     * @param page any page got from the exclusive implementation of the list extractor
     * @return a [InfoItemsPage] corresponding to the requested page
     * @see InfoItemsPage.getNextPage
     */
    @Throws(IOException::class, ExtractionException::class)
    abstract fun getPage(page: Page?): InfoItemsPage<R?>?

    override val linkHandler: LinkHandler?
        get() {
            return super.getLinkHandler() as ListLinkHandler?
        }
    /*//////////////////////////////////////////////////////////////////////////
    // Inner
    ////////////////////////////////////////////////////////////////////////// */
    /**
     * A class that is used to wrap a list of gathered items and eventual errors, it
     * also contains a field that points to the next available page ([.nextPage]).
     * @param <T> the info item type that this page is supposed to store and provide
    </T> */
    class InfoItemsPage<T : InfoItem?>(itemsList: List<T?>?,
                                       nextPage: Page?,
                                       errors: List<Throwable?>?) {
        /**
         * The current list of items of this page
         */
        val items: List<T>?

        /**
         * Url pointing to the next page relative to this one
         *
         * @see ListExtractor.getPage
         * @see Page
         */
        @JvmField
        val nextPage: Page?

        /**
         * Errors that happened during the extraction
         */
        @JvmField
        val errors: List<Throwable?>?

        constructor(collector: InfoItemsCollector<T, *>, nextPage: Page?) : this(collector.getItems(), nextPage, collector.getErrors())

        init {
            items = itemsList
            this.nextPage = nextPage
            this.errors = errors
        }

        fun hasNextPage(): Boolean {
            return Page.Companion.isValid(nextPage)
        }

        companion object {
            private val EMPTY: InfoItemsPage<InfoItem> = InfoItemsPage(emptyList<InfoItem>(), null, emptyList<Throwable>())

            /**
             * A convenient method that returns a representation of an empty page.
             *
             * @return a type-safe page with the list of items and errors empty and the nextPage set to
             * `null`.
             */
            fun <T : InfoItem?> emptyPage(): InfoItemsPage<T> {
                return EMPTY as InfoItemsPage<T>
            }
        }
    }

    companion object {
        /**
         * Constant that should be returned whenever
         * a list has an unknown number of items.
         */
        val ITEM_COUNT_UNKNOWN: Long = -1

        /**
         * Constant that should be returned whenever a list has an
         * infinite number of items. For example a YouTube mix.
         */
        @JvmField
        val ITEM_COUNT_INFINITE: Long = -2

        /**
         * Constant that should be returned whenever a list
         * has an unknown number of items bigger than 100.
         */
        val ITEM_COUNT_MORE_THAN_100: Long = -3
    }
}
