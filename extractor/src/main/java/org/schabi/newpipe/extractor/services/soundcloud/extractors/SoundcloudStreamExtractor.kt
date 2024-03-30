package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.SoundCloudGoPlusContentException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.Stream
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

class SoundcloudStreamExtractor(service: StreamingService,
                                linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {
    private var track: JsonObject? = null
    private var isAvailable: Boolean = true
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        track = SoundcloudParsingHelper.resolveFor(downloader, getUrl())
        val policy: String = track!!.getString("policy", "")
        if (!(policy == "ALLOW") && !(policy == "MONETIZE")) {
            isAvailable = false
            if ((policy == "SNIP")) {
                throw SoundCloudGoPlusContentException()
            }
            if ((policy == "BLOCK")) {
                throw GeographicRestrictionException(
                        "This track is not available in user's country")
            }
            throw ContentNotAvailableException("Content not available: policy " + policy)
        }
    }

    override val id: String?
        get() {
            return track!!.getInt("id").toString()
        }

    override val name: String?
        get() {
            return track!!.getString("title")
        }

    override val textualUploadDate: String?
        get() {
            return track!!.getString("created_at")
                    .replace("T", " ")
                    .replace("Z", "")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            return DateWrapper(SoundcloudParsingHelper.parseDateFrom(track!!.getString("created_at")))
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromTrackObject(track)
        }

    override val description: Description
        get() {
            return Description(track!!.getString("description"), Description.Companion.PLAIN_TEXT)
        }
    override val length: Long
        get() {
            return track!!.getLong("duration") / 1000L
        }

    @get:Throws(ParsingException::class)
    override val timeStamp: Long
        get() {
            return getTimestampSeconds("(#t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)")
        }
    override val viewCount: Long
        get() {
            return track!!.getLong("playback_count")
        }
    override val likeCount: Long
        get() {
            return track!!.getLong("likes_count", -1)
        }

    override val uploaderUrl: String?
        get() {
            return SoundcloudParsingHelper.getUploaderUrl(track)
        }

    override val uploaderName: String?
        get() {
            return SoundcloudParsingHelper.getUploaderName(track)
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return track!!.getObject("user").getBoolean("verified")
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl(SoundcloudParsingHelper.getAvatarUrl(track))
        }

    @get:Throws(ExtractionException::class)
    override val audioStreams: List<AudioStream?>
        get() {
            val audioStreams: MutableList<AudioStream?> = ArrayList()

            // Streams can be streamable and downloadable - or explicitly not.
            // For playing the track, it is only necessary to have a streamable track.
            // If this is not the case, this track might not be published yet.
            if (!track!!.getBoolean("streamable") || !isAvailable) {
                return audioStreams
            }
            try {
                val transcodings: JsonArray = track!!.getObject("media").getArray("transcodings")
                if (!Utils.isNullOrEmpty(transcodings)) {
                    // Get information about what stream formats are available
                    extractAudioStreams(transcodings, checkMp3ProgressivePresence(transcodings),
                            audioStreams)
                }
                extractDownloadableFileIfAvailable(audioStreams)
            } catch (e: NullPointerException) {
                throw ExtractionException("Could not get audio streams", e)
            }
            return audioStreams
        }

    @Nonnull
    @Throws(IOException::class, ExtractionException::class)
    private fun getTranscodingUrl(endpointUrl: String): String {
        val apiStreamUrl: String = endpointUrl + "?client_id=" + SoundcloudParsingHelper.clientId()
        val response: String? = NewPipe.getDownloader().get(apiStreamUrl).responseBody()
        val urlObject: JsonObject
        try {
            urlObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse streamable URL", e)
        }
        return urlObject.getString("url")
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getDownloadUrl(trackId: String?): String? {
        val response: String? = NewPipe.getDownloader().get((SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "tracks/"
                + trackId + "/download" + "?client_id=" + SoundcloudParsingHelper.clientId())).responseBody()
        val downloadJsonObject: JsonObject
        try {
            downloadJsonObject = JsonParser.`object`().from(response)
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse download URL", e)
        }
        val redirectUri: String = downloadJsonObject.getString("redirectUri")
        if (!Utils.isNullOrEmpty(redirectUri)) {
            return redirectUri
        }
        return null
    }

    private fun extractAudioStreams(transcodings: JsonArray,
                                    mp3ProgressiveInStreams: Boolean,
                                    audioStreams: MutableList<AudioStream?>) {
        transcodings.stream()
                .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .forEachOrdered(Consumer<JsonObject>({ transcoding: JsonObject ->
                    val url: String = transcoding.getString("url")
                    if (Utils.isNullOrEmpty(url)) {
                        return@forEachOrdered
                    }
                    try {
                        val preset: String = transcoding.getString("preset", Stream.Companion.ID_UNKNOWN)
                        val protocol: String = transcoding.getObject("format")
                                .getString("protocol")
                        val builder: AudioStream.Builder? = AudioStream.Builder()
                                .setId(preset)
                        val isHls: Boolean = (protocol == "hls")
                        if (isHls) {
                            builder!!.setDeliveryMethod(DeliveryMethod.HLS)
                        }
                        builder!!.setContent(getTranscodingUrl(url), true)
                        if (preset.contains("mp3")) {
                            // Don't add the MP3 HLS stream if there is a progressive stream
                            // present because both have the same bitrate
                            if (mp3ProgressiveInStreams && isHls) {
                                return@forEachOrdered
                            }
                            builder.setMediaFormat(MediaFormat.MP3)
                            builder.setAverageBitrate(128)
                        } else if (preset.contains("opus")) {
                            builder.setMediaFormat(MediaFormat.OPUS)
                            builder.setAverageBitrate(64)
                            builder.setDeliveryMethod(DeliveryMethod.HLS)
                        } else {
                            // Unknown format, skip to the next audio stream
                            return@forEachOrdered
                        }
                        val audioStream: AudioStream? = builder.build()
                        if (!Stream.Companion.containSimilarStream(audioStream, audioStreams)) {
                            audioStreams.add(audioStream)
                        }
                    } catch (ignored: ExtractionException) {
                        // Something went wrong when trying to get and add this audio stream,
                        // skip to the next one
                    } catch (ignored: IOException) {
                    }
                }))
    }

    /**
     * Add the downloadable format if it is available.
     *
     *
     *
     * A track can have the `downloadable` boolean set to `true`, but it doesn't mean
     * we can download it.
     *
     *
     *
     *
     * If the value of the `has_download_left` boolean is `true`, the track can be
     * downloaded, and not otherwise.
     *
     *
     * @param audioStreams the audio streams to which the downloadable file is added
     */
    fun extractDownloadableFileIfAvailable(audioStreams: MutableList<AudioStream?>) {
        if (track!!.getBoolean("downloadable") && track!!.getBoolean("has_downloads_left")) {
            try {
                val downloadUrl: String? = getDownloadUrl(id)
                if (!Utils.isNullOrEmpty(downloadUrl)) {
                    audioStreams.add(AudioStream.Builder()
                            .setId("original-format")
                            .setContent(downloadUrl, true)
                            .setAverageBitrate(AudioStream.Companion.UNKNOWN_BITRATE)
                            .build())
                }
            } catch (ignored: Exception) {
                // If something went wrong when trying to get the download URL, ignore the
                // exception throw because this "stream" is not necessary to play the track
            }
        }
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

    @get:Throws(IOException::class, ExtractionException::class)
    override val relatedItems: InfoItemsCollector<out InfoItem?, out InfoItemExtractor?>?
        get() {
            val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(getServiceId())
            val apiUrl: String = (SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL + "tracks/" + urlEncode(id)
                    + "/related?client_id=" + urlEncode(SoundcloudParsingHelper.clientId()))
            SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl)
            return collector
        }
    override val privacy: Privacy?
        get() {
            return if ((track!!.getString("sharing") == "public")) Privacy.PUBLIC else Privacy.PRIVATE
        }

    override val category: String?
        get() {
            return track!!.getString("genre")
        }

    override val licence: String?
        get() {
            return track!!.getString("license")
        }

    override val tags: List<String?>?
        get() {
            // Tags are separated by spaces, but they can be multiple words escaped by quotes "
            val tagList: Array<String> = track!!.getString("tag_list").split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val tags: MutableList<String?> = ArrayList()
            val escapedTag: StringBuilder = StringBuilder()
            var isEscaped: Boolean = false
            for (tag: String in tagList) {
                if (tag.startsWith("\"")) {
                    escapedTag.append(tag.replace("\"", ""))
                    isEscaped = true
                } else if (isEscaped) {
                    if (tag.endsWith("\"")) {
                        escapedTag.append(" ").append(tag.replace("\"", ""))
                        isEscaped = false
                        tags.add(escapedTag.toString())
                    } else {
                        escapedTag.append(" ").append(tag)
                    }
                } else if (!tag.isEmpty()) {
                    tags.add(tag)
                }
            }
            return tags
        }

    companion object {
        private fun checkMp3ProgressivePresence(transcodings: JsonArray): Boolean {
            return transcodings.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .anyMatch(Predicate({ transcodingJsonObject: JsonObject ->
                        transcodingJsonObject.getString("preset")
                                .contains("mp3") && (transcodingJsonObject.getObject("format")
                                .getString("protocol") == "progressive")
                    }))
        }

        private fun urlEncode(value: String?): String? {
            try {
                return Utils.encodeUrlUtf8(value)
            } catch (e: UnsupportedEncodingException) {
                throw IllegalStateException(e)
            }
        }
    }
}
