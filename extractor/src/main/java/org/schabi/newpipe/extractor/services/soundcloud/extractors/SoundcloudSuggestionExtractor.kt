package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class SoundcloudSuggestionExtractor(service: StreamingService) : SuggestionExtractor(service) {
    @Throws(IOException::class, ExtractionException::class)
    public override fun suggestionList(query: String?): List<String> {
        val suggestions: MutableList<String> = ArrayList()
        val dl: Downloader? = NewPipe.getDownloader()
        val url: String = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "search/queries?q="
                + Utils.encodeUrlUtf8(query) + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10")
        val response: String? = dl.get(url, getExtractorLocalization()).responseBody()
        try {
            val collection: JsonArray = JsonParser.`object`().from(response).getArray("collection")
            for (suggestion: Any? in collection) {
                if (suggestion is JsonObject) {
                    suggestions.add(suggestion.getString("query"))
                }
            }
            return suggestions
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }
}
