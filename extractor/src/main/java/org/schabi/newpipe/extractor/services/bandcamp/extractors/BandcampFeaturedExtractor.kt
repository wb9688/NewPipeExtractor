// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.kiosk.KioskExtractor
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector
import java.io.IOException
import java.nio.charset.StandardCharsets

class BandcampFeaturedExtractor(streamingService: StreamingService,
                                listLinkHandler: ListLinkHandler?,
                                kioskId: String) : KioskExtractor<PlaylistInfoItem?>(streamingService, listLinkHandler, kioskId) {
    private var json: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        try {
            json = JsonParser.`object`().from(getDownloader().postWithContentTypeJson(
                    FEATURED_API_URL, emptyMap(),
                    "{\"platform\":\"\",\"version\":0}".toByteArray(StandardCharsets.UTF_8))
                    .responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse Bandcamp featured API response", e)
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return KIOSK_FEATURED
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val featuredStories: JsonArray = json!!.getObject("feed_content")
                    .getObject("stories")
                    .getArray("featured")
            return extractItems(featuredStories)
        }

    private fun extractItems(featuredStories: JsonArray): InfoItemsPage<PlaylistInfoItem?> {
        val c: PlaylistInfoItemsCollector = PlaylistInfoItemsCollector(getServiceId())
        for (i in featuredStories.indices) {
            val featuredStory: JsonObject = featuredStories.getObject(i)
            if (featuredStory.isNull("album_title")) {
                // Is not an album, ignore
                continue
            }
            c.commit(BandcampPlaylistInfoItemFeaturedExtractor(featuredStory))
        }
        val lastFeaturedStory: JsonObject = featuredStories.getObject(featuredStories.size - 1)
        return InfoItemsPage(c, getNextPageFrom(lastFeaturedStory))
    }

    /**
     * Next Page can be generated from metadata of last featured story
     */
    private fun getNextPageFrom(lastFeaturedStory: JsonObject): Page {
        val lastStoryDate: Long = lastFeaturedStory.getLong("story_date")
        val lastStoryId: Long = lastFeaturedStory.getLong("ntid")
        val lastStoryType: String = lastFeaturedStory.getString("story_type")
        return Page(
                (MORE_FEATURED_API_URL + "?story_groups=featured"
                        + ':' + lastStoryDate + ':' + lastStoryType + ':' + lastStoryId)
        )
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<PlaylistInfoItem?>? {
        val response: JsonObject
        try {
            response = JsonParser.`object`().from(
                    getDownloader().get(page.getUrl()).responseBody()
            )
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse Bandcamp featured API response", e)
        }
        return extractItems(response.getObject("stories").getArray("featured"))
    }

    companion object {
        val KIOSK_FEATURED: String = "Featured"
        val FEATURED_API_URL: String = BandcampExtractorHelper.BASE_API_URL + "/mobile/24/bootstrap_data"
        val MORE_FEATURED_API_URL: String = BandcampExtractorHelper.BASE_API_URL + "/mobile/24/feed_older_logged_out"
    }
}
