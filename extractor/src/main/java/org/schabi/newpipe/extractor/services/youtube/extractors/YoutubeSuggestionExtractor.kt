/*
 * Created by Christian Schabesberger on 28.09.16.
 *
 * Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeSuggestionExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class YoutubeSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {
    @Throws(IOException::class, ExtractionException::class)
    public override fun suggestionList(query: String?): List<String> {
        val url: String = ("https://suggestqueries-clients6.youtube.com/complete/search"
                + "?client=" + "youtube"
                + "&ds=" + "yt"
                + "&gl=" + Utils.encodeUrlUtf8(getExtractorContentCountry().getCountryCode())
                + "&q=" + Utils.encodeUrlUtf8(query)
                + "&xhr=t")
        val headers: MutableMap<String?, List<String?>?> = HashMap()
        headers.put("Origin", listOf("https://www.youtube.com"))
        headers.put("Referer", listOf("https://www.youtube.com"))
        val response: Response? = NewPipe.getDownloader()
                .get(url, headers, getExtractorLocalization())
        val contentTypeHeader: String? = response!!.getHeader("Content-Type")
        if (Utils.isNullOrEmpty(contentTypeHeader) || !contentTypeHeader!!.contains("application/json")) {
            throw ExtractionException(("Invalid response type (got \"" + contentTypeHeader
                    + "\", excepted a JSON response) (response code "
                    + response.responseCode() + ")"))
        }
        val responseBody: String? = response.responseBody()
        if (responseBody!!.isEmpty()) {
            throw ExtractionException("Empty response received")
        }
        try {
            val suggestions: JsonArray = JsonParser.array()
                    .from(responseBody)
                    .getArray(1) // 0: search query, 1: search suggestions, 2: tracking data?
            return suggestions.stream()
                    .filter(Predicate({ o: Any? -> JsonArray::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonArray::class.java.cast(obj) }))
                    .map(Function({ suggestion: JsonArray -> suggestion.getString(0) })) // 0 is the search suggestion
                    .filter(Predicate({ suggestion: String? -> !Utils.isBlank(suggestion) })) // Filter blank suggestions
                    .collect(Collectors.toUnmodifiableList())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse JSON response", e)
        }
    }
}
