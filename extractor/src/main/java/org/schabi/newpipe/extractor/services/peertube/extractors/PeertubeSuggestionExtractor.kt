package org.schabi.newpipe.extractor.services.peertube.extractors

import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor

class PeertubeSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {
    public override fun suggestionList(query: String?): List<String> {
        return emptyList()
    }
}
