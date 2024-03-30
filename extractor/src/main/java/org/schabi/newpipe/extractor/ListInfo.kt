package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler

abstract class ListInfo<T : InfoItem?> : Info {
    @JvmField
    var relatedItems: List<T>? = null
    @JvmField
    var nextPage: Page? = null
    val contentFilters: List<String?>?
    val sortFilter: String?

    constructor(serviceId: Int,
                id: String?,
                url: String?,
                originalUrl: String?,
                name: String?,
                contentFilter: List<String?>?,
                sortFilter: String?) : super(serviceId, id, url, originalUrl, name) {
        contentFilters = contentFilter
        this.sortFilter = sortFilter
    }

    constructor(serviceId: Int,
                listUrlIdHandler: ListLinkHandler,
                name: String?) : super(serviceId, listUrlIdHandler, name) {
        contentFilters = listUrlIdHandler.contentFilters
        sortFilter = listUrlIdHandler.sortFilter
    }

    fun hasNextPage(): Boolean {
        return Page.Companion.isValid(nextPage)
    }
}
