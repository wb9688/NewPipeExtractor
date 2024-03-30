// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.linkHandler

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.Locale

/**
 * Artist do have IDs that are useful
 */
class BandcampChannelLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        try {
            val response: String? = NewPipe.getDownloader().get(url).responseBody()

            // Use band data embedded in website to extract ID
            val bandData: JsonObject? = JsonUtils.getJsonData(response, "data-band")
            return bandData!!.getLong("id").toString()
        } catch (e: IOException) {
            throw ParsingException("Download failed", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Download failed", e)
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw ParsingException("Download failed", e)
        } catch (e: JsonParserException) {
            throw ParsingException("Download failed", e)
        }
    }

    /**
     * Uses the mobile endpoint as a "translator" from id to url
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?, contentFilter: List<String?>?, sortFilter: String?): String? {
        val artistDetails: JsonObject? = BandcampExtractorHelper.getArtistDetails(id)
        if (artistDetails!!.getBoolean("error")) {
            throw ParsingException(
                    "JSON does not contain a channel URL (invalid id?) or is otherwise invalid")
        }
        return Utils.replaceHttpWithHttps(artistDetails.getString("bandcamp_url"))
    }

    /**
     * Accepts only pages that lead to the root of an artist profile. Supports external pages.
     */
    @Throws(ParsingException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        val lowercaseUrl: String = url!!.lowercase(Locale.getDefault())

        // https: | | artist.bandcamp.com | releases - music - album - track ( | name)
        //  0      1           2                           3                    (4)
        val splitUrl: Array<String> = lowercaseUrl.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        // URL is too short
        if (splitUrl.size != 3 && splitUrl.size != 4) {
            return false
        }

        // Must have "releases", "music", "album" or "track" as segment after URL or none at all
        if (splitUrl.size == 4 && !(((splitUrl.get(3) == "releases") || (splitUrl.get(3) == "music") || (splitUrl.get(3) == "album") || (splitUrl.get(3) == "track")))) {
            return false
        } else {
            if ((splitUrl.get(2) == "daily.bandcamp.com")) {
                // Refuse links to daily.bandcamp.com as that is not an artist
                return false
            }

            // Test whether domain is supported
            return BandcampExtractorHelper.isSupportedDomain(lowercaseUrl)
        }
    }

    companion object {
        val instance: BandcampChannelLinkHandlerFactory = BandcampChannelLinkHandlerFactory()
    }
}
