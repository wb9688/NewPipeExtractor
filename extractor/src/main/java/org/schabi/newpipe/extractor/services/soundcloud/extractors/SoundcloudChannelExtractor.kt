package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.services.soundcloud.linkHandler.SoundcloudChannelTabLinkHandlerFactory
import java.io.IOException

class SoundcloudChannelExtractor(service: StreamingService,
                                 linkHandler: ListLinkHandler?) : ChannelExtractor(service, linkHandler) {
    @get:Nonnull
    override var id: String? = null
        private set
    private var user: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        id = getLinkHandler().getId()
        val apiUrl: String = (USERS_ENDPOINT + id + "?client_id="
                + SoundcloudParsingHelper.clientId())
        val response: String? = downloader.get(apiUrl, getExtractorLocalization()).responseBody()
        try {
            user = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }

    @get:Nonnull
    override val name: String?
        get() {
            return user!!.getString("username")
        }

    @get:Nonnull
    override val avatars: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(user!!.getString("avatar_url"))
        }

    @get:Nonnull
    override val banners: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromVisualUrl(user!!.getObject("visuals")
                    .getArray("visuals")
                    .getObject(0)
                    .getString("visual_url"))
        }
    override val feedUrl: String?
        get() {
            return null
        }
    override val subscriberCount: Long
        get() {
            return user!!.getLong("followers_count", 0)
        }
    override val description: String?
        get() {
            return user!!.getString("description", "")
        }
    override val parentChannelName: String?
        get() {
            return ""
        }
    override val parentChannelUrl: String?
        get() {
            return ""
        }

    @get:Nonnull
    override val parentChannelAvatars: List<Image?>?
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return user!!.getBoolean("verified")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val tabs: List<ListLinkHandler>
        get() {
            val url: String? = getUrl()
            val urlTracks: String = (url
                    + SoundcloudChannelTabLinkHandlerFactory.Companion.getUrlSuffix(ChannelTabs.TRACKS))
            val urlPlaylists: String = (url
                    + SoundcloudChannelTabLinkHandlerFactory.Companion.getUrlSuffix(ChannelTabs.PLAYLISTS))
            val urlAlbums: String = (url
                    + SoundcloudChannelTabLinkHandlerFactory.Companion.getUrlSuffix(ChannelTabs.ALBUMS))
            val id: String? = id
            return java.util.List.of(
                    ListLinkHandler(urlTracks, urlTracks, id,
                            java.util.List.of(ChannelTabs.TRACKS), ""),
                    ListLinkHandler(urlPlaylists, urlPlaylists, id,
                            java.util.List.of(ChannelTabs.PLAYLISTS), ""),
                    ListLinkHandler(urlAlbums, urlAlbums, id,
                            java.util.List.of(ChannelTabs.ALBUMS), ""))
        }

    companion object {
        private val USERS_ENDPOINT: String = SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "users/"
    }
}
