package org.schabi.newpipe.extractor.linkhandler

import java.util.Collections

open class ListLinkHandler(originalUrl: String?,
                           url: String?,
                           id: String?,
                           contentFilters: List<String?>?,
                           val sortFilter: String?) : LinkHandler(originalUrl, url, id) {
    @JvmField
    val contentFilters: List<String?>

    init {
        this.contentFilters = Collections.unmodifiableList(contentFilters)
    }

    constructor(handler: ListLinkHandler) : this(handler.originalUrl,
            handler.url,
            handler.id,
            handler.contentFilters,
            handler.sortFilter)

    constructor(handler: LinkHandler?) : this(handler!!.originalUrl,
            handler.url,
            handler.id, emptyList<String>(),
            "")
}
