package org.schabi.newpipe.extractor.linkhandler

import org.schabi.newpipe.extractor.exceptions.ParsingException

abstract class SearchQueryHandlerFactory() : ListLinkHandlerFactory() {
    ///////////////////////////////////
    // To Override
    ///////////////////////////////////
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    abstract override fun getUrl(query: String?, contentFilter: List<String?>?, sortFilter: String?): String?
    @Suppress("unused")
    fun getSearchString(url: String?): String {
        return ""
    }

    ///////////////////////////////////
    // Logic
    ///////////////////////////////////
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return getSearchString(url)
    }

    @Throws(ParsingException::class)
    public override fun fromQuery(query: String?,
                                  contentFilter: List<String?>?,
                                  sortFilter: String?): SearchQueryHandler {
        return SearchQueryHandler(super.fromQuery(query, contentFilter, sortFilter))
    }

    @Throws(ParsingException::class)
    fun fromQuery(query: String?): SearchQueryHandler {
        return fromQuery(query, emptyList<String>(), "")
    }

    /**
     * It's not mandatory for NewPipe to handle the Url
     */
    public override fun onAcceptUrl(url: String?): Boolean {
        return false
    }
}
