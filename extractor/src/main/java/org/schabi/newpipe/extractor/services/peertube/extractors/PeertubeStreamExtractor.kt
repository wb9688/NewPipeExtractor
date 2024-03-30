package org.schabi.newpipe.extractor.services.peertube.extractors

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
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.Frameset
import org.schabi.newpipe.extractor.stream.Stream
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import org.schabi.newpipe.extractor.stream.StreamSegment
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.SubtitlesStream
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.Locale
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

class PeertubeStreamExtractor(service: StreamingService, linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {
    private override val baseUrl: String?
    private var json: JsonObject? = null
    private val subtitles: MutableList<SubtitlesStream?> = ArrayList()
    private override val audioStreams: MutableList<AudioStream?> = ArrayList()
    private override val videoStreams: MutableList<VideoStream?> = ArrayList()
    private var subtitlesException: ParsingException? = null

    init {
        baseUrl = getBaseUrl()
    }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            return JsonUtils.getString(json, "publishedAt")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualUploadDate: String? = textualUploadDate
            if (textualUploadDate == null) {
                return null
            }
            return DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate))
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem(baseUrl, json)
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val description: Description
        get() {
            var text: String?
            try {
                text = JsonUtils.getString(json, "description")
            } catch (e: ParsingException) {
                return Description.Companion.EMPTY_DESCRIPTION
            }
            if (text.length == 250 && (text.substring(247) == "...")) {
                // If description is shortened, get full description
                val dl: Downloader? = NewPipe.getDownloader()
                try {
                    val response: Response? = dl!!.get((baseUrl
                            + PeertubeStreamLinkHandlerFactory.Companion.VIDEO_API_ENDPOINT
                            + getId() + "/description"))
                    val jsonObject: JsonObject = JsonParser.`object`().from(response!!.responseBody())
                    text = JsonUtils.getString(jsonObject, "description")
                } catch (ignored: IOException) {
                    // Something went wrong when getting the full description, use the shortened one
                } catch (ignored: ReCaptchaException) {
                } catch (ignored: JsonParserException) {
                }
            }
            return Description(text, Description.Companion.MARKDOWN)
        }

    @get:Throws(ParsingException::class)
    val ageLimit: Int
        get() {
            val isNSFW: Boolean = JsonUtils.getBoolean(json, "nsfw")
            if (isNSFW) {
                return 18
            } else {
                return StreamExtractor.Companion.NO_AGE_LIMIT
            }
        }
    override val length: Long
        get() {
            return json!!.getLong("duration")
        }

    @get:Throws(ParsingException::class)
    override val timeStamp: Long
        get() {
            val timestamp: Long = getTimestampSeconds(
                    "((#|&|\\?)start=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)")
            if (timestamp == -2L) {
                // regex for timestamp was not found
                return 0
            } else {
                return timestamp
            }
        }
    override val viewCount: Long
        get() {
            return json!!.getLong("views")
        }
    override val likeCount: Long
        get() {
            return json!!.getLong("likes")
        }
    override val dislikeCount: Long
        get() {
            return json!!.getLong("dislikes")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploaderUrl: String?
        get() {
            val name: String? = JsonUtils.getString(json, ACCOUNT_NAME)
            val host: String? = JsonUtils.getString(json, ACCOUNT_HOST)
            return getService().getChannelLHFactory().fromId("accounts/" + name + "@" + host, baseUrl)
                    .getUrl()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploaderName: String?
        get() {
            return JsonUtils.getString(json, "account.displayName")
        }

    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json!!.getObject("account"))
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val subChannelUrl: String?
        get() {
            return JsonUtils.getString(json, "channel.url")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val subChannelName: String?
        get() {
            return JsonUtils.getString(json, "channel.displayName")
        }

    @get:Nonnull
    override val subChannelAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json!!.getObject("channel"))
        }

    @get:Nonnull
    override val hlsUrl: String
        get() {
            assertPageFetched()
            if ((streamType == StreamType.VIDEO_STREAM
                            && !Utils.isNullOrEmpty(json!!.getObject(FILES)))) {
                return json!!.getObject(FILES).getString(PLAYLIST_URL, "")
            }
            return json!!.getArray(STREAMING_PLAYLISTS).getObject(0).getString(PLAYLIST_URL, "")
        }

    @Throws(ParsingException::class)
    public override fun getAudioStreams(): List<AudioStream?> {
        assertPageFetched()

        /*
        Some videos have audio streams; others don't.
        So an audio stream may be available if a video stream is available.
        Audio streams are also not returned as separated streams for livestreams.
        That's why the extraction of audio streams is only run when there are video streams
        extracted and when the content is not a livestream.
         */if ((audioStreams.isEmpty() && videoStreams.isEmpty()
                        && (streamType == StreamType.VIDEO_STREAM))) {
            streams
        }
        return audioStreams
    }

    @Throws(ExtractionException::class)
    public override fun getVideoStreams(): List<VideoStream?> {
        assertPageFetched()
        if (videoStreams.isEmpty()) {
            if (streamType == StreamType.VIDEO_STREAM) {
                streams
            } else {
                extractLiveVideoStreams()
            }
        }
        return videoStreams
    }

    override val videoOnlyStreams: List<VideoStream?>
        get() {
            return emptyList<VideoStream>()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val subtitlesDefault: List<SubtitlesStream?>
        get() {
            if (subtitlesException != null) {
                throw subtitlesException
            }
            return subtitles
        }

    @Nonnull
    @Throws(ParsingException::class)
    public override fun getSubtitles(format: MediaFormat): List<SubtitlesStream?> {
        if (subtitlesException != null) {
            throw subtitlesException
        }
        return subtitles.stream()
                .filter(Predicate({ sub: SubtitlesStream? -> sub.getFormat() == format }))
                .collect(Collectors.toList())
    }

    override val streamType: StreamType?
        get() {
            return if (json!!.getBoolean("isLive")) StreamType.LIVE_STREAM else StreamType.VIDEO_STREAM
        }

    @get:Throws(IOException::class, ExtractionException::class)
    override val relatedItems: InfoItemsCollector<out InfoItem?, out InfoItemExtractor?>?
        get() {
            val tags: List<String?>? = tags
            val apiUrl: String
            if (tags!!.isEmpty()) {
                apiUrl = (baseUrl + "/api/v1/accounts/" + JsonUtils.getString(json, ACCOUNT_NAME)
                        + "@" + JsonUtils.getString(json, ACCOUNT_HOST)
                        + "/videos?start=0&count=8")
            } else {
                apiUrl = getRelatedItemsUrl(tags)
            }
            if (Utils.isBlank(apiUrl)) {
                return null
            } else {
                val collector: StreamInfoItemsCollector = StreamInfoItemsCollector(
                        getServiceId())
                getStreamsFromApi(collector, apiUrl)
                return collector
            }
        }

    @get:Nonnull
    override val tags: List<String?>?
        get() {
            return JsonUtils.getStringListFromJsonArray(json!!.getArray("tags"))
        }

    @get:Nonnull
    override val supportInfo: String?
        get() {
            try {
                return JsonUtils.getString(json, "support")
            } catch (e: ParsingException) {
                return ""
            }
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val streamSegments: List<StreamSegment>
        get() {
            val segments: MutableList<StreamSegment> = ArrayList()
            val segmentsJson: JsonObject?
            try {
                segmentsJson = fetchSubApiContent("chapters")
            } catch (e: IOException) {
                throw ParsingException("Could not get stream segments", e)
            } catch (e: ReCaptchaException) {
                throw ParsingException("Could not get stream segments", e)
            }
            if (segmentsJson != null && segmentsJson.has("chapters")) {
                val segmentsArray: JsonArray = segmentsJson.getArray("chapters")
                for (i in segmentsArray.indices) {
                    val segmentObject: JsonObject = segmentsArray.getObject(i)
                    segments.add(StreamSegment(
                            segmentObject.getString("title"),
                            segmentObject.getInt("timecode")))
                }
            }
            return segments
        }

    @get:Throws(ExtractionException::class)
    @get:Nonnull
    override val frames: List<Frameset>
        get() {
            val framesets: MutableList<Frameset> = ArrayList()
            val storyboards: JsonObject?
            try {
                storyboards = fetchSubApiContent("storyboards")
            } catch (e: IOException) {
                throw ExtractionException("Could not get frames", e)
            } catch (e: ReCaptchaException) {
                throw ExtractionException("Could not get frames", e)
            }
            if (storyboards != null && storyboards.has("storyboards")) {
                val storyboardsArray: JsonArray = storyboards.getArray("storyboards")
                for (storyboard: Any? in storyboardsArray) {
                    if (storyboard is JsonObject) {
                        val storyboardObject: JsonObject = storyboard
                        val url: String = storyboardObject.getString("storyboardPath")
                        val width: Int = storyboardObject.getInt("spriteWidth")
                        val height: Int = storyboardObject.getInt("spriteHeight")
                        val totalWidth: Int = storyboardObject.getInt("totalWidth")
                        val totalHeight: Int = storyboardObject.getInt("totalHeight")
                        val framesPerPageX: Int = totalWidth / width
                        val framesPerPageY: Int = totalHeight / height
                        val count: Int = framesPerPageX * framesPerPageY
                        val durationPerFrame: Int = storyboardObject.getInt("spriteDuration") * 1000
                        framesets.add(Frameset( // there is only one composite image per video containing all frames
                                java.util.List.of(baseUrl + url),
                                width, height, count,
                                durationPerFrame, framesPerPageX, framesPerPageY))
                    }
                }
            }
            return framesets
        }

    @Nonnull
    @Throws(UnsupportedEncodingException::class)
    private fun getRelatedItemsUrl(@Nonnull tags: List<String?>?): String {
        val url: String = baseUrl + PeertubeSearchQueryHandlerFactory.Companion.SEARCH_ENDPOINT_VIDEOS
        val params: StringBuilder = StringBuilder()
        params.append("start=0&count=8&sort=-createdAt")
        for (tag: String? in tags!!) {
            params.append("&tagsOneOf=").append(Utils.encodeUrlUtf8(tag))
        }
        return url + "?" + params
    }

    @Throws(IOException::class, ReCaptchaException::class, ParsingException::class)
    private fun getStreamsFromApi(collector: StreamInfoItemsCollector, apiUrl: String) {
        val response: Response? = getDownloader().get(apiUrl)
        var relatedVideosJson: JsonObject? = null
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                relatedVideosJson = JsonParser.`object`().from(response.responseBody())
            } catch (e: JsonParserException) {
                throw ParsingException("Could not parse json data for related videos", e)
            }
        }
        if (relatedVideosJson != null) {
            collectStreamsFrom(collector, relatedVideosJson)
        }
    }

    @Throws(ParsingException::class)
    private fun collectStreamsFrom(collector: StreamInfoItemsCollector,
                                   jsonObject: JsonObject) {
        val contents: JsonArray?
        try {
            contents = JsonUtils.getValue(jsonObject, "data") as JsonArray?
        } catch (e: Exception) {
            throw ParsingException("Could not extract related videos", e)
        }
        for (c: Any in contents!!) {
            if (c is JsonObject) {
                val extractor: PeertubeStreamInfoItemExtractor = PeertubeStreamInfoItemExtractor(c, baseUrl)
                // Do not add the same stream in related streams
                if (!(extractor.getUrl() == getUrl())) {
                    collector.commit(extractor)
                }
            }
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val response: Response? = downloader!!.get(
                baseUrl + PeertubeStreamLinkHandlerFactory.Companion.VIDEO_API_ENDPOINT + getId())
        if (response != null) {
            setInitialData(response.responseBody())
        } else {
            throw ExtractionException("Could not extract PeerTube channel data")
        }
        loadSubtitles()
    }

    @Throws(ExtractionException::class)
    private fun setInitialData(responseBody: String?) {
        try {
            json = JsonParser.`object`().from(responseBody)
        } catch (e: JsonParserException) {
            throw ExtractionException("Could not extract PeerTube stream data", e)
        }
        if (json == null) {
            throw ExtractionException("Could not extract PeerTube stream data")
        }
        PeertubeParsingHelper.validate(json)
    }

    private fun loadSubtitles() {
        if (subtitles.isEmpty()) {
            try {
                val response: Response? = getDownloader().get((baseUrl
                        + PeertubeStreamLinkHandlerFactory.Companion.VIDEO_API_ENDPOINT
                        + getId() + "/captions"))
                val captionsJson: JsonObject = JsonParser.`object`().from(response!!.responseBody())
                val captions: JsonArray? = JsonUtils.getArray(captionsJson, "data")
                for (c: Any in captions!!) {
                    if (c is JsonObject) {
                        val caption: JsonObject = c
                        val url: String = baseUrl + JsonUtils.getString(caption, "captionPath")
                        val languageCode: String? = JsonUtils.getString(caption, "language.id")
                        val ext: String = url.substring(url.lastIndexOf(".") + 1)
                        val fmt: MediaFormat? = MediaFormat.Companion.getFromSuffix(ext)
                        if (fmt != null && !Utils.isNullOrEmpty(languageCode)) {
                            subtitles.add(SubtitlesStream.Builder()
                                    .setContent(url, true)
                                    .setMediaFormat(fmt)
                                    .setLanguageCode(languageCode)
                                    .setAutoGenerated(false)
                                    .build())
                        }
                    }
                }
            } catch (e: Exception) {
                subtitlesException = ParsingException("Could not get subtitles", e)
            }
        }
    }

    @Throws(ParsingException::class)
    private fun extractLiveVideoStreams() {
        try {
            val streamingPlaylists: JsonArray = json!!.getArray(STREAMING_PLAYLISTS)
            streamingPlaylists.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ stream: JsonObject ->
                        VideoStream.Builder()
                                .setId(stream.getInt("id", -1).toString())
                                .setContent(stream.getString(PLAYLIST_URL, ""), true)
                                .setIsVideoOnly(false)
                                .setResolution("")
                                .setMediaFormat(MediaFormat.MPEG_4)
                                .setDeliveryMethod(DeliveryMethod.HLS)
                                .build()
                    })) // Don't use the containsSimilarStream method because it will always return
                    // false so if there are multiples HLS URLs returned, only the first will be
                    // extracted in this case.
                    .forEachOrdered(Consumer({ e: VideoStream? -> videoStreams.add(e) }))
        } catch (e: Exception) {
            throw ParsingException("Could not get video streams", e)
        }
    }

    @get:Throws(ParsingException::class)
    private val streams: Unit
        private get() {
            // Progressive streams
            getStreamsFromArray(json!!.getArray(FILES), "")

            // HLS streams
            try {
                for (playlist: JsonObject in json!!.getArray(STREAMING_PLAYLISTS).stream()
                        .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                        .collect(Collectors.toList())) {
                    getStreamsFromArray(playlist.getArray(FILES), playlist.getString(PLAYLIST_URL))
                }
            } catch (e: Exception) {
                throw ParsingException("Could not get streams", e)
            }
        }

    @Throws(ParsingException::class)
    private fun getStreamsFromArray(@Nonnull streams: JsonArray,
                                    playlistUrl: String) {
        try {
            /*
            Starting with version 3.4.0 of PeerTube, the HLS playlist of stream resolutions
            contains the UUID of the streams, so we can't use the same method to get the URL of
            the HLS playlist without fetching the master playlist.
            These UUIDs are the same as the ones returned into the fileUrl and fileDownloadUrl
            strings.
            */
            val isInstanceUsingRandomUuidsForHlsStreams: Boolean = (!Utils.isNullOrEmpty(playlistUrl)
                    && playlistUrl.endsWith("-master.m3u8"))
            for (stream: JsonObject in streams.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .collect(Collectors.toList())) {

                // Extract stream version of streams first
                val url: String? = JsonUtils.getString(stream,
                        if (stream.has(FILE_URL)) FILE_URL else FILE_DOWNLOAD_URL)
                if (Utils.isNullOrEmpty(url)) {
                    // Not a valid stream URL
                    return
                }
                val resolution: String? = JsonUtils.getString(stream, "resolution.label")
                val idSuffix: String = if (stream.has(FILE_URL)) FILE_URL else FILE_DOWNLOAD_URL
                if (resolution!!.lowercase(Locale.getDefault()).contains("audio")) {
                    // An audio stream
                    addNewAudioStream(stream, isInstanceUsingRandomUuidsForHlsStreams, resolution,
                            idSuffix, url, playlistUrl)
                } else {
                    // A video stream
                    addNewVideoStream(stream, isInstanceUsingRandomUuidsForHlsStreams, resolution,
                            idSuffix, url, playlistUrl)
                }
            }
        } catch (e: Exception) {
            throw ParsingException("Could not get streams from array", e)
        }
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getHlsPlaylistUrlFromFragmentedFileUrl(
            @Nonnull streamJsonObject: JsonObject,
            @Nonnull idSuffix: String,
            @Nonnull format: String,
            @Nonnull url: String?): String {
        val streamUrl: String? = if ((FILE_DOWNLOAD_URL == idSuffix)) JsonUtils.getString(streamJsonObject, FILE_URL) else url
        return streamUrl!!.replace("-fragmented." + format, ".m3u8")
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getHlsPlaylistUrlFromMasterPlaylist(@Nonnull streamJsonObject: JsonObject,
                                                    @Nonnull playlistUrl: String?): String {
        return playlistUrl!!.replace("master", JsonUtils.getNumber(streamJsonObject,
                RESOLUTION_ID).toString())
    }

    @Throws(ParsingException::class)
    private fun addNewAudioStream(@Nonnull streamJsonObject: JsonObject,
                                  isInstanceUsingRandomUuidsForHlsStreams: Boolean,
                                  @Nonnull resolution: String?,
                                  @Nonnull idSuffix: String,
                                  @Nonnull url: String?,
                                  playlistUrl: String?) {
        val extension: String = url!!.substring(url.lastIndexOf(".") + 1)
        val format: MediaFormat? = MediaFormat.Companion.getFromSuffix(extension)
        val id: String = resolution + "-" + extension

        // Add progressive HTTP streams first
        audioStreams.add(AudioStream.Builder()
                .setId(id + "-" + idSuffix + "-" + DeliveryMethod.PROGRESSIVE_HTTP)
                .setContent(url, true)
                .setMediaFormat(format)
                .setAverageBitrate(AudioStream.Companion.UNKNOWN_BITRATE)
                .build())

        // Then add HLS streams
        if (!Utils.isNullOrEmpty(playlistUrl)) {
            val hlsStreamUrl: String
            if (isInstanceUsingRandomUuidsForHlsStreams) {
                hlsStreamUrl = getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject, idSuffix,
                        extension, url)
            } else {
                hlsStreamUrl = getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject, playlistUrl)
            }
            val audioStream: AudioStream? = AudioStream.Builder()
                    .setId(id + "-" + DeliveryMethod.HLS)
                    .setContent(hlsStreamUrl, true)
                    .setDeliveryMethod(DeliveryMethod.HLS)
                    .setMediaFormat(format)
                    .setAverageBitrate(AudioStream.Companion.UNKNOWN_BITRATE)
                    .setManifestUrl(playlistUrl)
                    .build()
            if (!Stream.Companion.containSimilarStream(audioStream, audioStreams)) {
                audioStreams.add(audioStream)
            }
        }

        // Finally, add torrent URLs
        val torrentUrl: String? = JsonUtils.getString(streamJsonObject, "torrentUrl")
        if (!Utils.isNullOrEmpty(torrentUrl)) {
            audioStreams.add(AudioStream.Builder()
                    .setId(id + "-" + idSuffix + "-" + DeliveryMethod.TORRENT)
                    .setContent(torrentUrl, true)
                    .setDeliveryMethod(DeliveryMethod.TORRENT)
                    .setMediaFormat(format)
                    .setAverageBitrate(AudioStream.Companion.UNKNOWN_BITRATE)
                    .build())
        }
    }

    @Throws(ParsingException::class)
    private fun addNewVideoStream(@Nonnull streamJsonObject: JsonObject,
                                  isInstanceUsingRandomUuidsForHlsStreams: Boolean,
                                  @Nonnull resolution: String?,
                                  @Nonnull idSuffix: String,
                                  @Nonnull url: String?,
                                  playlistUrl: String?) {
        val extension: String = url!!.substring(url.lastIndexOf(".") + 1)
        val format: MediaFormat? = MediaFormat.Companion.getFromSuffix(extension)
        val id: String = resolution + "-" + extension

        // Add progressive HTTP streams first
        videoStreams.add(VideoStream.Builder()
                .setId(id + "-" + idSuffix + "-" + DeliveryMethod.PROGRESSIVE_HTTP)
                .setContent(url, true)
                .setIsVideoOnly(false)
                .setResolution(resolution)
                .setMediaFormat(format)
                .build())

        // Then add HLS streams
        if (!Utils.isNullOrEmpty(playlistUrl)) {
            val hlsStreamUrl: String = if (isInstanceUsingRandomUuidsForHlsStreams) getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject, idSuffix, extension,
                    url) else getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject, playlistUrl)
            val videoStream: VideoStream? = VideoStream.Builder()
                    .setId(id + "-" + DeliveryMethod.HLS)
                    .setContent(hlsStreamUrl, true)
                    .setIsVideoOnly(false)
                    .setDeliveryMethod(DeliveryMethod.HLS)
                    .setResolution(resolution)
                    .setMediaFormat(format)
                    .setManifestUrl(playlistUrl)
                    .build()
            if (!Stream.Companion.containSimilarStream(videoStream, videoStreams)) {
                videoStreams.add(videoStream)
            }
        }

        // Add finally torrent URLs
        val torrentUrl: String? = JsonUtils.getString(streamJsonObject, "torrentUrl")
        if (!Utils.isNullOrEmpty(torrentUrl)) {
            videoStreams.add(VideoStream.Builder()
                    .setId(id + "-" + idSuffix + "-" + DeliveryMethod.TORRENT)
                    .setContent(torrentUrl, true)
                    .setIsVideoOnly(false)
                    .setDeliveryMethod(DeliveryMethod.TORRENT)
                    .setResolution(resolution)
                    .setMediaFormat(format)
                    .build())
        }
    }

    /**
     * Fetch content from a sub-API of the video.
     * @param subPath the API subpath after the video id,
     * e.g. "storyboards" for "/api/v1/videos/{id}/storyboards"
     * @return the [JsonObject] of the sub-API or null if the API does not exist
     * which is the case if the instance has an outdated PeerTube version.
     * @throws ParsingException if the API response could not be parsed to a [JsonObject]
     * @throws IOException if the API response could not be fetched
     * @throws ReCaptchaException if the API response is a reCaptcha
     */
    @Throws(ParsingException::class, IOException::class, ReCaptchaException::class)
    private fun fetchSubApiContent(@Nonnull subPath: String): JsonObject? {
        val apiUrl: String = (baseUrl + PeertubeStreamLinkHandlerFactory.Companion.VIDEO_API_ENDPOINT
                + getId() + "/" + subPath)
        val response: Response? = getDownloader().get(apiUrl)
        if (response == null) {
            throw ParsingException("Could not get segments from API.")
        }
        if (response.responseCode() == 400) {
            // Chapter or segments support was added with PeerTube v6.0.0
            // This instance does not support it yet.
            return null
        }
        if (response.responseCode() != 200) {
            throw ParsingException(("Could not get segments from API. Response code: "
                    + response.responseCode()))
        }
        try {
            return JsonParser.`object`().from(response.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json data for segments", e)
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return JsonUtils.getString(json, "name")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val host: String?
        get() {
            return JsonUtils.getString(json, ACCOUNT_HOST)
        }

    @get:Nonnull
    override val privacy: Privacy?
        get() {
            when (json!!.getObject("privacy").getInt("id")) {
                1 -> return Privacy.PUBLIC
                2 -> return Privacy.UNLISTED
                3 -> return Privacy.PRIVATE
                4 -> return Privacy.INTERNAL
                else -> return Privacy.OTHER
            }
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val category: String?
        get() {
            return JsonUtils.getString(json, "category.label")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val licence: String?
        get() {
            return JsonUtils.getString(json, "licence.label")
        }
    override val languageInfo: Locale?
        get() {
            try {
                return Locale(JsonUtils.getString(json, "language.id"))
            } catch (e: ParsingException) {
                return null
            }
        }

    companion object {
        private val ACCOUNT_HOST: String = "account.host"
        private val ACCOUNT_NAME: String = "account.name"
        private val FILES: String = "files"
        private val FILE_DOWNLOAD_URL: String = "fileDownloadUrl"
        private val FILE_URL: String = "fileUrl"
        private val PLAYLIST_URL: String = "playlistUrl"
        private val RESOLUTION_ID: String = "resolution.id"
        private val STREAMING_PLAYLISTS: String = "streamingPlaylists"
    }
}
