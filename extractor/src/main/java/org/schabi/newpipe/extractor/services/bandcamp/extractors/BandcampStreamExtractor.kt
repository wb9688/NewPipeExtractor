// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.PaidContentException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

open class BandcampStreamExtractor(service: StreamingService, linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {
    private var albumJson: JsonObject? = null
    private var current: JsonObject? = null
    private var document: Document? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val html: String? = downloader!!.get(getLinkHandler().getUrl()).responseBody()
        document = Jsoup.parse((html)!!)
        albumJson = getAlbumInfoJson(html)
        current = albumJson!!.getObject("current")
        if (albumJson!!.getArray("trackinfo").size > 1) {
            // In this case, we are actually viewing an album page!
            throw ExtractionException("Page is actually an album, not a track")
        }
        if (albumJson!!.getArray("trackinfo").getObject(0).isNull("file")) {
            throw PaidContentException("This track is not available without being purchased")
        }
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return current!!.getString("title")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            val parts: Array<String> = url!!.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            // https: (/) (/) * .bandcamp.com (/) and leave out the rest
            return Utils.HTTPS + parts.get(2) + "/"
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return Utils.replaceHttpWithHttps(albumJson!!.getString("url"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return albumJson!!.getString("artist")
        }
    override val textualUploadDate: String?
        get() {
            return current!!.getString("publish_date")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return BandcampExtractorHelper.parseDate(textualUploadDate)
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            if (albumJson!!.isNull("art_id")) {
                return listOf<Image>()
            }
            return BandcampExtractorHelper.getImagesFromImageId(albumJson!!.getLong("art_id"), true)
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageUrl(document!!.getElementsByClass("band-photo")
                    .stream()
                    .map(Function({ element: Element -> element.attr("src") }))
                    .findFirst()
                    .orElse(""))
        }

    override val description: Description
        get() {
            val s: String? = Utils.nonEmptyAndNullJoin("\n\n", current!!.getString("about"),
                    current!!.getString("lyrics"), current!!.getString("credits"))
            return Description(s, Description.Companion.PLAIN_TEXT)
        }
    override val audioStreams: List<AudioStream?>
        get() {
            return listOf<AudioStream?>(AudioStream.Builder()
                    .setId("mp3-128")
                    .setContent(albumJson!!.getArray("trackinfo")
                            .getObject(0)
                            .getObject("file")
                            .getString("mp3-128"), true)
                    .setMediaFormat(MediaFormat.MP3)
                    .setAverageBitrate(128)
                    .build())
        }

    @get:Throws(ParsingException::class)
    override val length: Long
        get() {
            return albumJson!!.getArray("trackinfo").getObject(0)
                    .getDouble("duration").toLong()
        }
    override val videoStreams: List<VideoStream?>
        get() {
            return emptyList<VideoStream>()
        }
    override val videoOnlyStreams: List<VideoStream?>
        get() {
            return emptyList<VideoStream>()
        }
    override val streamType: StreamType?
        get() {
            return StreamType.AUDIO_STREAM
        }
    override val relatedItems: InfoItemsCollector<out InfoItem?, out InfoItemExtractor?>?
        get() {
            val collector: PlaylistInfoItemsCollector = PlaylistInfoItemsCollector(getServiceId())
            document!!.getElementsByClass("recommended-album")
                    .stream()
                    .map(Function({ relatedAlbum: Element -> BandcampRelatedPlaylistInfoItemExtractor(relatedAlbum) }))
                    .forEach(Consumer({ extractor: BandcampRelatedPlaylistInfoItemExtractor -> collector.commit(extractor) }))
            return collector
        }

    override val category: String?
        get() {
            // Get first tag from html, which is the artist's Genre
            return document!!.getElementsByClass("tralbum-tags").stream()
                    .flatMap(Function<Element, Stream<out Element>>({ element: Element -> element.getElementsByClass("tag").stream() }))
                    .map(Function({ obj: Element -> obj.text() }))
                    .findFirst()
                    .orElse("")
        }

    override val licence: String?
        get() {
            /*
        Tests resulted in this mapping of ints to licence:
        https://cloud.disroot.org/s/ZTWBxbQ9fKRmRWJ/preview (screenshot from a Bandcamp artist's
        account)
        */
            when (current!!.getInt("license_type")) {
                1 -> return "All rights reserved ©"
                2 -> return "CC BY-NC-ND 3.0"
                3 -> return "CC BY-NC-SA 3.0"
                4 -> return "CC BY-NC 3.0"
                5 -> return "CC BY-ND 3.0"
                6 -> return "CC BY 3.0"
                8 -> return "CC BY-SA 3.0"
                else -> return "Unknown"
            }
        }

    override val tags: List<String?>?
        get() {
            return document!!.getElementsByAttributeValue("itemprop", "keywords")
                    .stream()
                    .map(Function({ obj: Element -> obj.text() }))
                    .collect(Collectors.toList())
        }

    companion object {
        /**
         * Get the JSON that contains album's metadata from page
         *
         * @param html Website
         * @return Album metadata JSON
         * @throws ParsingException In case of a faulty website
         */
        @Throws(ParsingException::class)
        fun getAlbumInfoJson(html: String?): JsonObject? {
            try {
                return JsonUtils.getJsonData(html, "data-tralbum")
            } catch (e: JsonParserException) {
                throw ParsingException("Faulty JSON; page likely does not contain album data", e)
            } catch (e: ArrayIndexOutOfBoundsException) {
                throw ParsingException("JSON does not exist", e)
            }
        }
    }
}
