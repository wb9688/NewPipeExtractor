package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamSegment
import java.io.IOException
import java.util.function.Function
import java.util.function.Supplier

class BandcampRadioStreamExtractor(service: StreamingService,
                                   linkHandler: LinkHandler?) : BandcampStreamExtractor(service, linkHandler) {
    private var showInfo: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        showInfo = query(getId().toInt())
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            /* Select "subtitle" and not "audio_title", as the latter would cause a lot of
         * items to show the same title, e.g. "Bandcamp Weekly".
         */
            return showInfo!!.getString("subtitle")
        }

    @get:Throws(ContentNotSupportedException::class)
    override val uploaderUrl: String?
        get() {
            throw ContentNotSupportedException("Fan pages are not supported")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return getLinkHandler().getUrl()
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return Jsoup.parse(showInfo!!.getString("image_caption")).getElementsByTag("a").stream()
                    .map(Function({ obj: Element -> obj.text() }))
                    .findFirst()
                    .orElseThrow(Supplier({ ParsingException("Could not get uploader name") }))
        }
    override val textualUploadDate: String?
        get() {
            return showInfo!!.getString("published_date")
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageId(showInfo!!.getLong("show_image_id"), false)
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return listOf(
                    Image(BandcampExtractorHelper.BASE_URL + "/img/buttons/bandcamp-button-circle-whitecolor-512.png",
                            512, 512, ResolutionLevel.MEDIUM))
        }

    override val description: Description
        get() {
            return Description(showInfo!!.getString("desc"), Description.Companion.PLAIN_TEXT)
        }
    override val length: Long
        get() {
            return showInfo!!.getLong("audio_duration")
        }
    override val audioStreams: List<AudioStream?>
        get() {
            val audioStreams: MutableList<AudioStream?> = ArrayList()
            val streams: JsonObject = showInfo!!.getObject("audio_stream")
            if (streams.has(MP3_128)) {
                audioStreams.add(AudioStream.Builder()
                        .setId(MP3_128)
                        .setContent(streams.getString(MP3_128), true)
                        .setMediaFormat(MediaFormat.MP3)
                        .setAverageBitrate(128)
                        .build())
            }
            if (streams.has(OPUS_LO)) {
                audioStreams.add(AudioStream.Builder()
                        .setId(OPUS_LO)
                        .setContent(streams.getString(OPUS_LO), true)
                        .setMediaFormat(MediaFormat.OPUS)
                        .setAverageBitrate(100).build())
            }
            return audioStreams
        }

    @get:Throws(ParsingException::class)
    override val streamSegments: List<StreamSegment>
        get() {
            val tracks: JsonArray = showInfo!!.getArray("tracks")
            val segments: MutableList<StreamSegment> = ArrayList(tracks.size)
            for (t: Any in tracks) {
                val track: JsonObject = t as JsonObject
                val segment: StreamSegment = StreamSegment(
                        track.getString("title"), track.getInt("timecode"))
                // "track art" is the track's album cover
                segment.setPreviewUrl(BandcampExtractorHelper.getImageUrl(track.getLong("track_art_id"), true))
                segment.setChannelName(track.getString("artist"))
                segments.add(segment)
            }
            return segments
        }

    override val licence: String?
        get() {
            // Contrary to other Bandcamp streams, radio streams don't have a license
            return ""
        }

    override val category: String?
        get() {
            // Contrary to other Bandcamp streams, radio streams don't have categories
            return ""
        }

    override val tags: List<String?>?
        get() {
            // Contrary to other Bandcamp streams, radio streams don't have tags
            return emptyList<String>()
        }
    override val relatedItems: InfoItemsCollector<out InfoItem?, out InfoItemExtractor?>?
        get() {
            // Contrary to other Bandcamp streams, radio streams don't have related items
            return null
        }

    companion object {
        private val OPUS_LO: String = "opus-lo"
        private val MP3_128: String = "mp3-128"
        @Throws(ParsingException::class)
        fun query(id: Int): JsonObject {
            try {
                return JsonParser.`object`().from(NewPipe.getDownloader()
                        .get(BandcampExtractorHelper.BASE_API_URL + "/bcweekly/1/get?id=" + id).responseBody())
            } catch (e: IOException) {
                throw ParsingException("could not get show data", e)
            } catch (e: ReCaptchaException) {
                throw ParsingException("could not get show data", e)
            } catch (e: JsonParserException) {
                throw ParsingException("could not get show data", e)
            }
        }
    }
}
