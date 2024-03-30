package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

class SoundcloudPlaylistExtractor(service: StreamingService,
                                  linkHandler: ListLinkHandler?) : PlaylistExtractor(service, linkHandler) {
    override var id: String? = null
        private set
    private var playlist: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        id = getLinkHandler().getId()
        val apiUrl: String = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "playlists/" + id + "?client_id="
                + SoundcloudParsingHelper.clientId() + "&representation=compact")
        val response: String? = downloader.get(apiUrl, getExtractorLocalization()).responseBody()
        try {
            playlist = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
    }

    override val name: String?
        get() {
            return playlist!!.getString("title")
        }

    override val thumbnails: List<Image?>?
        get() {
            val artworkUrl: String = playlist!!.getString("artwork_url")
            if (!Utils.isNullOrEmpty(artworkUrl)) {
                return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(artworkUrl)
            }

            // If the thumbnail is null or empty, traverse the items list and get a valid one
            // If it also fails, return an empty list
            try {
                val infoItems: InfoItemsPage<StreamInfoItem?>? = initialPage
                for (item: StreamInfoItem? in infoItems.getItems()) {
                    val thumbnails: List<Image?>? = item.getThumbnails()
                    if (!Utils.isNullOrEmpty(thumbnails)) {
                        return thumbnails
                    }
                }
            } catch (ignored: Exception) {
            }
            return listOf<Image>()
        }
    override val uploaderUrl: String?
        get() {
            return SoundcloudParsingHelper.getUploaderUrl(playlist)
        }
    override val uploaderName: String?
        get() {
            return SoundcloudParsingHelper.getUploaderName(playlist)
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(SoundcloudParsingHelper.getAvatarUrl(playlist))
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return playlist!!.getObject("user").getBoolean("verified")
        }
    override val streamCount: Long
        get() {
            return playlist!!.getLong("track_count")
        }

    @get:Throws(ParsingException::class)
    override val description: Description
        get() {
            val description: String = playlist!!.getString("description")
            if (Utils.isNullOrEmpty(description)) {
                return Description.Companion.EMPTY_DESCRIPTION
            }
            return Description(description, Description.Companion.PLAIN_TEXT)
        }

    override val initialPage: InfoItemsPage<R?>?
        get() {
            val streamInfoItemsCollector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            val ids: MutableList<String?> = ArrayList()
            playlist!!.getArray("tracks")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .forEachOrdered(Consumer({ track: JsonObject ->
                        // i.e. if full info is available
                        if (track.has("title")) {
                            streamInfoItemsCollector.commit(
                                    SoundcloudStreamInfoItemExtractor(track))
                        } else {
                            // %09d would be enough, but a 0 before the number does not create
                            // problems, so let's be sure
                            ids.add(String.format("%010d", track.getInt("id")))
                        }
                    }))
            return InfoItemsPage(streamInfoItemsCollector, Page(ids))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getIds())) {
            throw IllegalArgumentException("Page doesn't contain IDs")
        }
        val currentIds: List<String?>?
        val nextIds: List<String?>?
        if (page.getIds().size <= STREAMS_PER_REQUESTED_PAGE) {
            // Fetch every remaining stream, there are less than the max
            currentIds = page.getIds()
            nextIds = null
        } else {
            currentIds = page.getIds().subList(0, STREAMS_PER_REQUESTED_PAGE)
            nextIds = page.getIds().subList(STREAMS_PER_REQUESTED_PAGE, page.getIds().size)
        }
        val currentPageUrl: String = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "tracks?client_id="
                + SoundcloudParsingHelper.clientId() + "&ids=" + java.lang.String.join(",", currentIds))
        val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
        val response: String? = NewPipe.getDownloader().get(currentPageUrl,
                getExtractorLocalization()).responseBody()
        try {
            val tracks: JsonArray = JsonParser.array().from(response)
            // Response may not contain tracks in the same order as currentIds.
            // The streams are displayed in the order which is used in currentIds on SoundCloud.
            val idToTrack: HashMap<Int, JsonObject> = HashMap()
            for (track: Any? in tracks) {
                if (track is JsonObject) {
                    val o: JsonObject = track
                    idToTrack.put(o.getInt("id"), o)
                }
            }
            for (strId: String? in currentIds!!) {
                val id: Int = strId!!.toInt()
                try {
                    collector.commit(SoundcloudStreamInfoItemExtractor(
                            Objects.requireNonNull(
                                    idToTrack.get(id),
                                    "no track with id " + id + " in response"
                            )
                    ))
                } catch (e: NullPointerException) {
                    throw ParsingException("Could not parse json response", e)
                }
            }
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json response", e)
        }
        return InfoItemsPage(collector, Page(nextIds))
    }

    companion object {
        private val STREAMS_PER_REQUESTED_PAGE: Int = 15
    }
}
