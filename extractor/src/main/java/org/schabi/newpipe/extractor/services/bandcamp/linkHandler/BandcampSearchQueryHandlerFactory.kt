// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.io.UnsupportedEncodingException

class BandcampSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(query: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        try {
            return BandcampExtractorHelper.BASE_URL + "/search?q=" + Utils.encodeUrlUtf8(query) + "&page=1"
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("query \"" + query + "\" could not be encoded", e)
        }
    }

    companion object {
        val instance: BandcampSearchQueryHandlerFactory = BandcampSearchQueryHandlerFactory()
    }
}
