package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.Stream
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream
import java.io.IOException
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class MediaCCCLiveStreamExtractor(service: StreamingService,
                                  linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {
    private var conference: JsonObject? = null
    private var group: String = ""
    private var room: JsonObject? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val doc: JsonArray? = MediaCCCParsingHelper.getLiveStreams(downloader,
                getExtractorLocalization())
        // Find the correct room
        for (c in doc!!.indices) {
            val conferenceObject: JsonObject = doc.getObject(c)
            val groups: JsonArray = conferenceObject.getArray("groups")
            for (g in groups.indices) {
                val groupObject: String = groups.getObject(g).getString("group")
                val rooms: JsonArray = groups.getObject(g).getArray("rooms")
                for (r in rooms.indices) {
                    val roomObject: JsonObject = rooms.getObject(r)
                    if ((getId() == (conferenceObject.getString("slug") + "/"
                                    + roomObject.getString("slug")))) {
                        conference = conferenceObject
                        group = groupObject
                        room = roomObject
                        return
                    }
                }
            }
        }
        throw ExtractionException("Could not find room matching id: '" + getId() + "'")
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return room!!.getString("display")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getThumbnailsFromLiveStreamItem(room)
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val description: Description
        get() {
            return Description((conference!!.getString("description")
                    + " - " + group), Description.Companion.PLAIN_TEXT)
        }
    override val viewCount: Long
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploaderUrl: String?
        get() {
            return "https://streaming.media.ccc.de/" + conference!!.getString("slug")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploaderName: String?
        get() {
            return conference!!.getString("conference")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val dashMpdUrl: String
        /**
         * Get the URL of the first DASH stream found.
         *
         *
         *
         * There can be several DASH streams, so the URL of the first one found is returned by this
         * method.
         *
         *
         *
         *
         * You can find the other DASH video streams by using [.getVideoStreams]
         *
         */
        get() {
            return getManifestOfDeliveryMethodWanted("dash")
        }

    @get:Nonnull
    override val hlsUrl: String
        /**
         * Get the URL of the first HLS stream found.
         *
         *
         *
         * There can be several HLS streams, so the URL of the first one found is returned by this
         * method.
         *
         *
         *
         *
         * You can find the other HLS video streams by using [.getVideoStreams]
         *
         */
        get() {
            return getManifestOfDeliveryMethodWanted("hls")
        }

    @Nonnull
    private fun getManifestOfDeliveryMethodWanted(deliveryMethod: String): String {
        return room!!.getArray(STREAMS).stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map(Function({ streamObject: JsonObject -> streamObject.getObject(URLS) }))
                .filter(Predicate({ urls: JsonObject -> urls.has(deliveryMethod) }))
                .map(Function({ urls: JsonObject -> urls.getObject(deliveryMethod).getString(URL, "") }))
                .findFirst()
                .orElse("")
    }

    @get:Throws(IOException::class, ExtractionException::class)
    override val audioStreams: List<AudioStream?>
        get() {
            return getStreams<AudioStream?>("audio",
                    Function<MediaCCCLiveStreamMapperDTO, AudioStream?>({ dto: MediaCCCLiveStreamMapperDTO ->
                        val builder: AudioStream.Builder? = AudioStream.Builder()
                                .setId(dto.urlValue.getString("tech", Stream.Companion.ID_UNKNOWN))
                                .setContent(dto.urlValue.getString(URL), true)
                                .setAverageBitrate(AudioStream.Companion.UNKNOWN_BITRATE)
                        if (("hls" == dto.urlKey)) {
                            // We don't know with the type string what media format will
                            // have HLS streams.
                            // However, the tech string may contain some information
                            // about the media format used.
                            return@getStreams builder!!.setDeliveryMethod(DeliveryMethod.HLS)
                                    .build()
                        }
                        builder!!.setMediaFormat(MediaFormat.Companion.getFromSuffix(dto.urlKey))
                                .build()
                    }))
        }

    @get:Throws(IOException::class, ExtractionException::class)
    override val videoStreams: List<VideoStream?>
        get() {
            return getStreams<VideoStream?>("video",
                    Function<MediaCCCLiveStreamMapperDTO, VideoStream?>({ dto: MediaCCCLiveStreamMapperDTO ->
                        val videoSize: JsonArray = dto.streamJsonObj.getArray("videoSize")
                        val builder: VideoStream.Builder? = VideoStream.Builder()
                                .setId(dto.urlValue.getString("tech", Stream.Companion.ID_UNKNOWN))
                                .setContent(dto.urlValue.getString(URL), true)
                                .setIsVideoOnly(false)
                                .setResolution(videoSize.getInt(0).toString() + "x" + videoSize.getInt(1))
                        if (("hls" == dto.urlKey)) {
                            // We don't know with the type string what media format will
                            // have HLS streams.
                            // However, the tech string may contain some information
                            // about the media format used.
                            return@getStreams builder!!.setDeliveryMethod(DeliveryMethod.HLS)
                                    .build()
                        }
                        builder!!.setMediaFormat(MediaFormat.Companion.getFromSuffix(dto.urlKey))
                                .build()
                    }))
        }

    /**
     * This is just an internal class used in [.getStreams] to tie together
     * the stream json object, its URL key and its URL value. An object of this class would be
     * temporary and the three values it holds would be **convert**ed to a proper [Stream]
     * object based on the wanted stream type.
     */
    private class MediaCCCLiveStreamMapperDTO internal constructor(val streamJsonObj: JsonObject,
                                                                   val urlKey: String,
                                                                   val urlValue: JsonObject)

    private fun <T : Stream?> getStreams(
            streamType: String,
            converter: Function<MediaCCCLiveStreamMapperDTO, T>): List<T> {
        return room!!.getArray(STREAMS).stream() // Ensure that we use only process JsonObjects
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) })) // Only process streams of requested type
                .filter(Predicate({ streamJsonObj: JsonObject -> (streamType == streamJsonObj.getString("type")) })) // Flatmap Urls and ensure that we use only process JsonObjects
                .flatMap(Function<JsonObject, java.util.stream.Stream<out MediaCCCLiveStreamMapperDTO>>({ streamJsonObj: JsonObject ->
                    streamJsonObj.getObject(URLS).entries.stream()
                            .filter(Predicate<Map.Entry<String?, Any?>>({ e: Map.Entry<String?, Any?> -> e.value is JsonObject }))
                            .map(Function<Map.Entry<String, Any>, MediaCCCLiveStreamMapperDTO>({ e: Map.Entry<String, Any> ->
                                MediaCCCLiveStreamMapperDTO(
                                        streamJsonObj,
                                        e.key,
                                        e.value as JsonObject)
                            }))
                })) // The DASH manifest will be extracted with getDashMpdUrl
                .filter(Predicate({ dto: MediaCCCLiveStreamMapperDTO -> !("dash" == dto.urlKey) })) // Convert
                .map(converter)
                .collect(Collectors.toList())
    }

    override val videoOnlyStreams: List<VideoStream?>
        get() {
            return emptyList<VideoStream>()
        }

    @get:Throws(ParsingException::class)
    override val streamType: StreamType?
        get() {
            return StreamType.LIVE_STREAM
        }

    @get:Nonnull
    override val category: String?
        get() {
            return group
        }

    companion object {
        private val STREAMS: String = "streams"
        private val URLS: String = "urls"
        private val URL: String = "url"
    }
}
