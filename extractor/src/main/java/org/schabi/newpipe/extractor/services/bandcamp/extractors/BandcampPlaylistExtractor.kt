package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.PaidContentException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampPlaylistStreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.Objects
import java.util.function.Function

class BandcampPlaylistExtractor(service: StreamingService,
                                linkHandler: ListLinkHandler?) : PlaylistExtractor(service, linkHandler) {
    private var document: Document? = null
    private var albumJson: JsonObject? = null
    private var trackInfo: JsonArray? = null

    @get:Throws(ParsingException::class)
    override var name: String? = null
        private set

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val html: String? = downloader!!.get(getLinkHandler().getUrl()).responseBody()
        document = Jsoup.parse((html)!!)
        albumJson = BandcampStreamExtractor.Companion.getAlbumInfoJson(html)
        trackInfo = albumJson!!.getArray("trackinfo")
        try {
            name = JsonUtils.getJsonData(html, "data-embed").getString("album_title")
        } catch (e: JsonParserException) {
            throw ParsingException("Faulty JSON; page likely does not contain album data", e)
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw ParsingException("JSON does not exist", e)
        }
        if (trackInfo.isEmpty()) {
            // Albums without trackInfo need to be purchased before they can be played
            throw PaidContentException("Album needs to be purchased")
        }
    }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            if (albumJson!!.isNull("art_id")) {
                return listOf<Image>()
            } else {
                return BandcampExtractorHelper.getImagesFromImageId(albumJson!!.getLong("art_id"), true)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            val parts: Array<String> = getUrl().split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            // https: (/) (/) * .bandcamp.com (/) and leave out the rest
            return Utils.HTTPS + parts.get(2) + "/"
        }
    override val uploaderName: String?
        get() {
            return albumJson!!.getString("artist")
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageUrl(document!!.getElementsByClass("band-photo")
                    .stream()
                    .map(Function({ element: Element -> element.attr("src") }))
                    .findFirst()
                    .orElse(""))
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val streamCount: Long
        get() {
            return trackInfo!!.size.toLong()
        }

    @get:Throws(ParsingException::class)
    override val description: Description
        get() {
            val tInfo: Element? = document!!.getElementById("trackInfo")
            if (tInfo == null) {
                throw ParsingException("Could not find trackInfo in document")
            }
            val about: Elements = tInfo.getElementsByClass("tralbum-about")
            val credits: Elements = tInfo.getElementsByClass("tralbum-credits")
            val license: Element? = document!!.getElementById("license")
            if (about.isEmpty() && credits.isEmpty() && (license == null)) {
                return Description.Companion.EMPTY_DESCRIPTION
            }
            val sb: StringBuilder = StringBuilder()
            if (!about.isEmpty()) {
                sb.append(Objects.requireNonNull(about.first()).html())
            }
            if (!credits.isEmpty()) {
                sb.append(Objects.requireNonNull(credits.first()).html())
            }
            if (license != null) {
                sb.append(license.html())
            }
            return Description(sb.toString(), Description.Companion.HTML)
        }

    @get:Throws(ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            for (i in trackInfo!!.indices) {
                val track: JsonObject = trackInfo!!.getObject(i)
                if (trackInfo!!.size < MAXIMUM_INDIVIDUAL_COVER_ARTS) {
                    // Load cover art of every track individually
                    collector.commit(BandcampPlaylistStreamInfoItemExtractor(
                            track, uploaderUrl, getService()))
                } else {
                    // Pretend every track has the same cover art as the album
                    collector.commit(BandcampPlaylistStreamInfoItemExtractor(
                            track, uploaderUrl, thumbnails))
                }
            }
            return InfoItemsPage(collector, null)
        }

    public override fun getPage(page: Page?): InfoItemsPage<StreamInfoItem?>? {
        return null
    }

    companion object {
        /**
         * An arbitrarily chosen number above which cover arts won't be fetched individually for each
         * track; instead, it will be assumed that every track has the same cover art as the album,
         * which is not always the case.
         */
        private val MAXIMUM_INDIVIDUAL_COVER_ARTS: Int = 10
    }
}
