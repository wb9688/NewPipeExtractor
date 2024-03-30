package org.schabi.newpipe.extractor.services.soundcloud.linkHandler

import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.io.UnsupportedEncodingException

class SoundcloudSearchQueryHandlerFactory private constructor() : SearchQueryHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        try {
            var url: String = SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "search"
            if (!contentFilter!!.isEmpty()) {
                when (contentFilter.get(0)) {
                    TRACKS -> url += "/tracks"
                    USERS -> url += "/users"
                    PLAYLISTS -> url += "/playlists"
                    ALL -> {}
                    else -> {}
                }
            }
            return (url + "?q=" + Utils.encodeUrlUtf8(id)
                    + "&client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=" + ITEMS_PER_PAGE + "&offset=0")
        } catch (e: UnsupportedEncodingException) {
            throw ParsingException("Could not encode query", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("ReCaptcha required", e)
        } catch (e: IOException) {
            throw ParsingException("Could not get client id", e)
        } catch (e: ExtractionException) {
            throw ParsingException("Could not get client id", e)
        }
    }

    override val availableContentFilter: Array<String?>
        get() {
            return arrayOf(
                    ALL,
                    TRACKS,
                    USERS,
                    PLAYLISTS)
        }

    companion object {
        val instance: SoundcloudSearchQueryHandlerFactory = SoundcloudSearchQueryHandlerFactory()
        @JvmField
        val TRACKS: String = "tracks"
        @JvmField
        val USERS: String = "users"
        @JvmField
        val PLAYLISTS: String = "playlists"
        val ALL: String = "all"
        val ITEMS_PER_PAGE: Int = 10
    }
}
