package org.schabi.newpipe.extractor.search

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler

abstract class SearchExtractor(service: StreamingService, linkHandler: SearchQueryHandler?) : ListExtractor<InfoItem?>(service, linkHandler) {
    class NothingFoundException(message: String?) : ExtractionException(message)

    val searchString: String?
        get() {
            return this.linkHandler.getSearchString()
        }

    @JvmField
    @get:Throws(ParsingException::class)
    @get:Nonnull
    abstract val searchSuggestion: String?

    @get:Nonnull
    override val linkHandler: LinkHandler?
        get() {
            return super.getLinkHandler() as SearchQueryHandler?
        }

    @get:Nonnull
    override val name: String?
        get() {
            return this.linkHandler.getSearchString()
        }

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val isCorrectedSearch: Boolean

    @JvmField
    @get:Throws(ParsingException::class)
    @get:Nonnull
    abstract val metaInfo: List<MetaInfo?>?
}
