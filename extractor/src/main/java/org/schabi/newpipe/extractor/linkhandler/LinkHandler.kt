package org.schabi.newpipe.extractor.linkhandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.Utils
import java.io.Serializable

open class LinkHandler(val originalUrl: String?, @JvmField val url: String?, @JvmField val id: String?) : Serializable {

    constructor(handler: LinkHandler) : this(handler.originalUrl, handler.url, handler.id)

    @get:Throws(ParsingException::class)
    val baseUrl: String?
        get() {
            return Utils.getBaseUrl(url)
        }
}
