package org.schabi.newpipe.extractor.suggestion

import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.io.IOException

abstract class SuggestionExtractor(val service: StreamingService) {
    private var forcedLocalization: Localization? = null
    private var forcedContentCountry: ContentCountry? = null
    @Throws(IOException::class, ExtractionException::class)
    abstract fun suggestionList(query: String?): List<String>
    val serviceId: Int
        get() {
            return service.getServiceId()
        }

    // TODO: Create a more general Extractor class
    fun forceLocalization(localization: Localization?) {
        forcedLocalization = localization
    }

    fun forceContentCountry(contentCountry: ContentCountry?) {
        forcedContentCountry = contentCountry
    }

    @get:Nonnull
    val extractorLocalization: Localization?
        get() {
            return if (forcedLocalization == null) service.getLocalization() else forcedLocalization
        }

    @get:Nonnull
    val extractorContentCountry: ContentCountry?
        get() {
            return if (forcedContentCountry == null) service.getContentCountry() else forcedContentCountry
        }
}
