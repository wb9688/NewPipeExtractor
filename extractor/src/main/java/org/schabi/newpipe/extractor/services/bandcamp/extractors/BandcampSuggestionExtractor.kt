// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class BandcampSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {
    @Throws(IOException::class, ExtractionException::class)
    public override fun suggestionList(query: String?): List<String> {
        val downloader: Downloader? = NewPipe.getDownloader()
        try {
            val fuzzyResults: JsonObject = JsonParser.`object`().from(downloader
                    .get(AUTOCOMPLETE_URL + Utils.encodeUrlUtf8(query)).responseBody())
            return fuzzyResults.getObject("auto").getArray("results").stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ jsonObject: JsonObject -> jsonObject.getString("name") }))
                    .distinct()
                    .collect(Collectors.toList())
        } catch (e: JsonParserException) {
            return emptyList()
        }
    }

    companion object {
        private val AUTOCOMPLETE_URL: String = BandcampExtractorHelper.BASE_API_URL + "/fuzzysearch/1/autocomplete?q="
    }
}
