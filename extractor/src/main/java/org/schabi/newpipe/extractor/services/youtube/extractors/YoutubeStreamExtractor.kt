/*
 * Created by Christian Schabesberger on 06.08.15.
 *
 * Copyright (C) 2019 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeStreamExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.MultiInfoItemsCollector
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.AgeRestrictedContentException
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException
import org.schabi.newpipe.extractor.exceptions.PaidContentException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.PrivateContentException
import org.schabi.newpipe.extractor.exceptions.YoutubeMusicPremiumContentException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.localization.TimeAgoPatternsManager
import org.schabi.newpipe.extractor.services.youtube.ItagItem
import org.schabi.newpipe.extractor.services.youtube.ItagItem.ItagType
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptPlayerManager
import org.schabi.newpipe.extractor.services.youtube.YoutubeMetaInfoHelper
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.Frameset
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamSegment
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.SubtitlesStream
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.LocaleCompat
import org.schabi.newpipe.extractor.utils.Pair
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Locale
import java.util.Objects
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.ceil

class YoutubeStreamExtractor(service: StreamingService, linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {
    private var playerResponse: JsonObject? = null
    private var nextResponse: JsonObject? = null
    private var html5StreamingData: JsonObject? = null
    private var androidStreamingData: JsonObject? = null
    private var iosStreamingData: JsonObject? = null

    private var videoPrimaryInfoRenderer: JsonObject? = null
        /*//////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////// */ private get() {
            if (field != null) {
                return field
            }
            field = getVideoInfoRenderer("videoPrimaryInfoRenderer")
            return field
        }
    private var videoSecondaryInfoRenderer: JsonObject? = null
    private var playerMicroFormatRenderer: JsonObject? = null
    private var ageLimit: Int = -1
    private override var streamType: StreamType? = null

    // We need to store the contentPlaybackNonces because we need to append them to videoplayback
    // URLs (with the cpn parameter).
    // Also because a nonce should be unique, it should be different between clients used, so
    // three different strings are used.
    private var html5Cpn: String? = null
    private var androidCpn: String? = null
    private var iosCpn: String? = null

    @get:Throws(ParsingException::class)
    override val name: String?
        /*//////////////////////////////////////////////////////////////////////////
    // Impl
    ////////////////////////////////////////////////////////////////////////// */   get() {
            assertPageFetched()
            var title: String?

            // Try to get the video's original title, which is untranslated
            title = playerResponse!!.getObject("videoDetails").getString("title")
            if (Utils.isNullOrEmpty(title)) {
                title = YoutubeParsingHelper.getTextFromObject(videoPrimaryInfoRenderer!!.getObject("title"))
                if (Utils.isNullOrEmpty(title)) {
                    throw ParsingException("Could not get name")
                }
            }
            return title
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            if (!playerMicroFormatRenderer!!.getString("uploadDate", "").isEmpty()) {
                return playerMicroFormatRenderer!!.getString("uploadDate")
            } else if (!playerMicroFormatRenderer!!.getString("publishDate", "").isEmpty()) {
                return playerMicroFormatRenderer!!.getString("publishDate")
            }
            val liveDetails: JsonObject = playerMicroFormatRenderer!!.getObject(
                    "liveBroadcastDetails")
            if (!liveDetails.getString("endTimestamp", "").isEmpty()) {
                // an ended live stream
                return liveDetails.getString("endTimestamp")
            } else if (!liveDetails.getString("startTimestamp", "").isEmpty()) {
                // a running live stream
                return liveDetails.getString("startTimestamp")
            } else if (getStreamType() == StreamType.LIVE_STREAM) {
                // this should never be reached, but a live stream without upload date is valid
                return null
            }
            val videoPrimaryInfoRendererDateText: String? = YoutubeParsingHelper.getTextFromObject(videoPrimaryInfoRenderer!!.getObject("dateText"))
            if (videoPrimaryInfoRendererDateText != null) {
                if (videoPrimaryInfoRendererDateText.startsWith("Premiered")) {
                    val time: String = videoPrimaryInfoRendererDateText.substring(13)
                    try { // Premiered 20 hours ago
                        val timeAgoParser: TimeAgoParser? = TimeAgoPatternsManager.getTimeAgoParserFor(
                                Localization("en"))
                        val parsedTime: OffsetDateTime? = timeAgoParser!!.parse(time).offsetDateTime()
                        return DateTimeFormatter.ISO_LOCAL_DATE.format(parsedTime)
                    } catch (ignored: Exception) {
                    }
                    try { // Premiered Feb 21, 2020
                        val localDate: LocalDate = LocalDate.parse(time,
                                DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
                        return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
                    } catch (ignored: Exception) {
                    }
                    try { // Premiered on 21 Feb 2020
                        val localDate: LocalDate = LocalDate.parse(time,
                                DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
                        return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
                    } catch (ignored: Exception) {
                    }
                }
                try {
                    // TODO: this parses English formatted dates only, we need a better approach to
                    //  parse the textual date
                    val localDate: LocalDate = LocalDate.parse(videoPrimaryInfoRendererDateText,
                            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH))
                    return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)
                } catch (e: Exception) {
                    throw ParsingException("Could not get upload date", e)
                }
            }
            throw ParsingException("Could not get upload date")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualUploadDate: String? = textualUploadDate
            if (Utils.isNullOrEmpty(textualUploadDate)) {
                return null
            }
            return DateWrapper(YoutubeParsingHelper.parseDateFrom(textualUploadDate), true)
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            assertPageFetched()
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(playerResponse!!.getObject("videoDetails")
                        .getObject("thumbnail")
                        .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails")
            }
        }

    @get:Throws(ParsingException::class)
    override val description: Description
        get() {
            assertPageFetched()
            // Description with more info on links
            val videoSecondaryInfoRendererDescription: String? = YoutubeParsingHelper.getTextFromObject(
                    getVideoSecondaryInfoRenderer()!!.getObject("description"),
                    true)
            if (!Utils.isNullOrEmpty(videoSecondaryInfoRendererDescription)) {
                return Description(videoSecondaryInfoRendererDescription, Description.Companion.HTML)
            }
            val attributedDescription: String? = YoutubeParsingHelper.getAttributedDescription(
                    getVideoSecondaryInfoRenderer()!!.getObject("attributedDescription"))
            if (!Utils.isNullOrEmpty(attributedDescription)) {
                return Description(attributedDescription, Description.Companion.HTML)
            }
            var description: String? = playerResponse!!.getObject("videoDetails")
                    .getString("shortDescription")
            if (description == null) {
                val descriptionObject: JsonObject = playerMicroFormatRenderer!!.getObject("description")
                description = YoutubeParsingHelper.getTextFromObject(descriptionObject)
            }

            // Raw non-html description
            return Description(description, Description.Companion.PLAIN_TEXT)
        }

    @Throws(ParsingException::class)
    public override fun getAgeLimit(): Int {
        if (ageLimit != -1) {
            return ageLimit
        }
        val ageRestricted: Boolean = getVideoSecondaryInfoRenderer()
                .getObject("metadataRowContainer")
                .getObject("metadataRowContainerRenderer")
                .getArray("rows")
                .stream() // Only JsonObjects allowed
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .flatMap(Function<JsonObject, Stream<out JsonObject>>({ metadataRow: JsonObject ->
                    metadataRow
                            .getObject("metadataRowRenderer")
                            .getArray("contents")
                            .stream() // Only JsonObjects allowed
                            .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                            .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                }))
                .flatMap(Function<JsonObject, Stream<out JsonObject>>({ content: JsonObject ->
                    content
                            .getArray("runs")
                            .stream() // Only JsonObjects allowed
                            .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                            .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                }))
                .map(Function({ run: JsonObject -> run.getString("text", "") }))
                .anyMatch(Predicate({ rowText: String -> rowText.contains("Age-restricted") }))
        ageLimit = if (ageRestricted) 18 else StreamExtractor.Companion.NO_AGE_LIMIT
        return ageLimit
    }

    @get:Throws(ParsingException::class)
    override val length: Long
        get() {
            assertPageFetched()
            try {
                val duration: String = playerResponse
                        .getObject("videoDetails")
                        .getString("lengthSeconds")
                return duration.toLong()
            } catch (e: Exception) {
                return getDurationFromFirstAdaptiveFormat(Arrays.asList(
                        html5StreamingData, androidStreamingData, iosStreamingData)).toLong()
            }
        }

    @Throws(ParsingException::class)
    private fun getDurationFromFirstAdaptiveFormat(streamingDatas: List<JsonObject?>): Int {
        for (streamingData: JsonObject? in streamingDatas) {
            val adaptiveFormats: JsonArray = streamingData!!.getArray(ADAPTIVE_FORMATS)
            if (adaptiveFormats.isEmpty()) {
                continue
            }
            val durationMs: String = adaptiveFormats.getObject(0)
                    .getString("approxDurationMs")
            try {
                return Math.round(durationMs.toLong() / 1000f)
            } catch (ignored: NumberFormatException) {
            }
        }
        throw ParsingException("Could not get duration")
    }

    @get:Throws(ParsingException::class)
    override val timeStamp: Long
        /**
         * Attempts to parse (and return) the offset to start playing the video from.
         *
         * @return the offset (in seconds), or 0 if no timestamp is found.
         */
        get() {
            val timestamp: Long = getTimestampSeconds("((#|&|\\?)t=\\d*h?\\d*m?\\d+s?)")
            if (timestamp == -2L) {
                // Regex for timestamp was not found
                return 0
            }
            return timestamp
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            var views: String? = YoutubeParsingHelper.getTextFromObject(videoPrimaryInfoRenderer!!.getObject("viewCount")
                    .getObject("videoViewCountRenderer").getObject("viewCount"))
            if (Utils.isNullOrEmpty(views)) {
                views = playerResponse!!.getObject("videoDetails").getString("viewCount")
                if (Utils.isNullOrEmpty(views)) {
                    throw ParsingException("Could not get view count")
                }
            }
            if (views!!.lowercase(Locale.getDefault()).contains("no views")) {
                return 0
            }
            return Utils.removeNonDigitCharacters(views).toLong()
        }

    @get:Throws(ParsingException::class)
    override val likeCount: Long
        get() {
            assertPageFetched()

            // If ratings are not allowed, there is no like count available
            if (!playerResponse!!.getObject("videoDetails").getBoolean("allowRatings")) {
                return -1L
            }
            val topLevelButtons: JsonArray = videoPrimaryInfoRenderer
                    .getObject("videoActions")
                    .getObject("menuRenderer")
                    .getArray("topLevelButtons")
            try {
                return parseLikeCountFromLikeButtonViewModel(topLevelButtons)
            } catch (ignored: ParsingException) {
                // A segmentedLikeDislikeButtonRenderer could be returned instead of a
                // segmentedLikeDislikeButtonViewModel, so ignore extraction errors relative to
                // segmentedLikeDislikeButtonViewModel object
            }
            try {
                return parseLikeCountFromLikeButtonRenderer(topLevelButtons)
            } catch (e: ParsingException) {
                throw ParsingException("Could not get like count", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            assertPageFetched()

            // Don't use the id in the videoSecondaryRenderer object to get real id of the uploader
            // The difference between the real id of the channel and the displayed id is especially
            // visible for music channels and autogenerated channels.
            val uploaderId: String = playerResponse!!.getObject("videoDetails").getString("channelId")
            if (!Utils.isNullOrEmpty(uploaderId)) {
                return YoutubeChannelLinkHandlerFactory.Companion.getInstance().getUrl("channel/" + uploaderId)
            }
            throw ParsingException("Could not get uploader url")
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            assertPageFetched()

            // Don't use the name in the videoSecondaryRenderer object to get real name of the uploader
            // The difference between the real name of the channel and the displayed name is especially
            // visible for music channels and autogenerated channels.
            val uploaderName: String = playerResponse!!.getObject("videoDetails").getString("author")
            if (Utils.isNullOrEmpty(uploaderName)) {
                throw ParsingException("Could not get uploader name")
            }
            return uploaderName
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return YoutubeParsingHelper.isVerified(
                    getVideoSecondaryInfoRenderer()
                            .getObject("owner")
                            .getObject("videoOwnerRenderer")
                            .getArray("badges"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderAvatars: List<Image?>?
        get() {
            assertPageFetched()
            val imageList: List<Image?>? = YoutubeParsingHelper.getImagesFromThumbnailsArray(
                    getVideoSecondaryInfoRenderer()!!.getObject("owner")
                            .getObject("videoOwnerRenderer")
                            .getObject("thumbnail")
                            .getArray("thumbnails"))
            if (imageList!!.isEmpty() && ageLimit == StreamExtractor.Companion.NO_AGE_LIMIT) {
                throw ParsingException("Could not get uploader avatars")
            }
            return imageList
        }

    @get:Throws(ParsingException::class)
    val uploaderSubscriberCount: Long
        get() {
            val videoOwnerRenderer: JsonObject? = JsonUtils.getObject(videoSecondaryInfoRenderer,
                    "owner.videoOwnerRenderer")
            if (!videoOwnerRenderer!!.has("subscriberCountText")) {
                return StreamExtractor.Companion.UNKNOWN_SUBSCRIBER_COUNT
            }
            try {
                return Utils.mixedNumberWordToLong(YoutubeParsingHelper.getTextFromObject(videoOwnerRenderer
                        .getObject("subscriberCountText")))
            } catch (e: NumberFormatException) {
                throw ParsingException("Could not get uploader subscriber count", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val dashMpdUrl: String
        get() {
            assertPageFetched()

            // There is no DASH manifest available in the iOS clients and the DASH manifest of the
            // Android client doesn't contain all available streams (mainly the WEBM ones)
            return getManifestUrl(
                    "dash",
                    Arrays.asList(html5StreamingData, androidStreamingData))
        }

    @get:Throws(ParsingException::class)
    override val hlsUrl: String
        get() {
            assertPageFetched()

            // Return HLS manifest of the iOS client first because on livestreams, the HLS manifest
            // returned has separated audio and video streams
            // Also, on videos, non-iOS clients don't have an HLS manifest URL in their player response
            return getManifestUrl(
                    "hls",
                    Arrays.asList(iosStreamingData, html5StreamingData, androidStreamingData))
        }

    @get:Throws(ExtractionException::class)
    override val audioStreams: List<AudioStream?>
        get() {
            assertPageFetched()
            return getItags(ADAPTIVE_FORMATS, ItagType.AUDIO,
                    audioStreamBuilderHelper, "audio")
        }

    @get:Throws(ExtractionException::class)
    override val videoStreams: List<VideoStream?>
        get() {
            assertPageFetched()
            return getItags(FORMATS, ItagType.VIDEO,
                    getVideoStreamBuilderHelper(false), "video")
        }

    @get:Throws(ExtractionException::class)
    override val videoOnlyStreams: List<VideoStream?>
        get() {
            assertPageFetched()
            return getItags(ADAPTIVE_FORMATS, ItagType.VIDEO_ONLY,
                    getVideoStreamBuilderHelper(true), "video-only")
        }

    /**
     * Try to deobfuscate a streaming URL and fall back to the given URL, because decryption may
     * fail if YouTube changes break something.
     *
     *
     *
     * This way a breaking change from YouTube does not result in a broken extractor.
     *
     *
     * @param streamingUrl the streaming URL to which deobfuscating its throttling parameter if
     * there is one
     * @param videoId      the video ID to use when extracting JavaScript player code, if needed
     */
    private fun tryDeobfuscateThrottlingParameterOfUrl(streamingUrl: String?,
                                                       videoId: String?): String? {
        try {
            return YoutubeJavaScriptPlayerManager.getUrlWithThrottlingParameterDeobfuscated(
                    videoId, streamingUrl)
        } catch (e: ParsingException) {
            return streamingUrl
        }
    }

    @get:Throws(ParsingException::class)
    override val subtitlesDefault: List<SubtitlesStream?>
        get() {
            return getSubtitles(MediaFormat.TTML)
        }

    @Throws(ParsingException::class)
    public override fun getSubtitles(format: MediaFormat): List<SubtitlesStream?> {
        assertPageFetched()

        // We cannot store the subtitles list because the media format may change
        val subtitlesToReturn: MutableList<SubtitlesStream?> = ArrayList()
        val renderer: JsonObject = playerResponse!!.getObject("captions")
                .getObject("playerCaptionsTracklistRenderer")
        val captionsArray: JsonArray = renderer.getArray("captionTracks")
        // TODO: use this to apply auto translation to different language from a source language
        // final JsonArray autoCaptionsArray = renderer.getArray("translationLanguages");
        for (i in captionsArray.indices) {
            val languageCode: String? = captionsArray.getObject(i).getString("languageCode")
            val baseUrl: String? = captionsArray.getObject(i).getString("baseUrl")
            val vssId: String? = captionsArray.getObject(i).getString("vssId")
            if ((languageCode != null) && (baseUrl != null) && (vssId != null)) {
                val isAutoGenerated: Boolean = vssId.startsWith("a.")
                val cleanUrl: String = baseUrl // Remove preexisting format if exists
                        .replace("&fmt=[^&]*".toRegex(), "") // Remove translation language
                        .replace("&tlang=[^&]*".toRegex(), "")
                subtitlesToReturn.add(SubtitlesStream.Builder()
                        .setContent(cleanUrl + "&fmt=" + format.getSuffix(), true)
                        .setMediaFormat(format)
                        .setLanguageCode(languageCode)
                        .setAutoGenerated(isAutoGenerated)
                        .build())
            }
        }
        return subtitlesToReturn
    }

    public override fun getStreamType(): StreamType? {
        assertPageFetched()
        return streamType
    }

    private fun setStreamType() {
        if (playerResponse!!.getObject("playabilityStatus").has("liveStreamability")) {
            streamType = StreamType.LIVE_STREAM
        } else if (playerResponse!!.getObject("videoDetails").getBoolean("isPostLiveDvr", false)) {
            streamType = StreamType.POST_LIVE_STREAM
        } else {
            streamType = StreamType.VIDEO_STREAM
        }
    }

    @get:Throws(ExtractionException::class)
    override val relatedItems: InfoItemsCollector<out InfoItem?, out InfoItemExtractor?>?
        get() {
            assertPageFetched()
            if (getAgeLimit() != StreamExtractor.Companion.NO_AGE_LIMIT) {
                return null
            }
            try {
                val collector: MultiInfoItemsCollector = MultiInfoItemsCollector(getServiceId())
                val results: JsonArray = nextResponse
                        .getObject("contents")
                        .getObject("twoColumnWatchNextResults")
                        .getObject("secondaryResults")
                        .getObject("secondaryResults")
                        .getArray("results")
                val timeAgoParser: TimeAgoParser? = getTimeAgoParser()
                results.stream()
                        .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                        .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                        .map<InfoItemExtractor?>(Function<JsonObject, InfoItemExtractor?>({ result: JsonObject ->
                            if (result.has("compactVideoRenderer")) {
                                return@map YoutubeStreamInfoItemExtractor(
                                        result.getObject("compactVideoRenderer"), timeAgoParser)
                            } else if (result.has("compactRadioRenderer")) {
                                return@map YoutubeMixOrPlaylistInfoItemExtractor(
                                        result.getObject("compactRadioRenderer"))
                            } else if (result.has("compactPlaylistRenderer")) {
                                return@map YoutubeMixOrPlaylistInfoItemExtractor(
                                        result.getObject("compactPlaylistRenderer"))
                            }
                            null
                        }))
                        .filter(Predicate<InfoItemExtractor?>({ obj: InfoItemExtractor? -> Objects.nonNull(obj) }))
                        .forEach(Consumer<InfoItemExtractor?>({ extractor: InfoItemExtractor -> collector.commit(extractor) }))
                return collector
            } catch (e: Exception) {
                throw ParsingException("Could not get related videos", e)
            }
        }
    override val errorMessage: String?
        /**
         * {@inheritDoc}
         */
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(playerResponse!!.getObject("playabilityStatus")
                        .getObject("errorScreen").getObject("playerErrorMessageRenderer")
                        .getObject("reason"))
            } catch (e: NullPointerException) {
                return null // No error message
            }
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val videoId: String? = getId()
        val localization: Localization? = getExtractorLocalization()
        val contentCountry: ContentCountry? = getExtractorContentCountry()
        html5Cpn = YoutubeParsingHelper.generateContentPlaybackNonce()
        playerResponse = YoutubeParsingHelper.getJsonPostResponse(PLAYER,
                YoutubeParsingHelper.createDesktopPlayerBody(
                        localization,
                        contentCountry,
                        videoId,
                        YoutubeJavaScriptPlayerManager.getSignatureTimestamp(videoId),
                        false,
                        html5Cpn),
                localization)

        // Save the playerResponse from the player endpoint of the desktop internal API because
        // there can be restrictions on the embedded player.
        // E.g. if a video is age-restricted, the embedded player's playabilityStatus says that
        // the video cannot be played outside of YouTube, but does not show the original message.
        val youtubePlayerResponse: JsonObject? = playerResponse
        if (playerResponse == null) {
            throw ExtractionException("Could not get playerResponse")
        }
        val playabilityStatus: JsonObject = playerResponse!!.getObject("playabilityStatus")
        val isAgeRestricted: Boolean = playabilityStatus.getString("reason", "")
                .contains("age")
        setStreamType()
        if (!playerResponse!!.has(STREAMING_DATA)) {
            try {
                fetchTvHtml5EmbedJsonPlayer(contentCountry, localization, videoId)
            } catch (ignored: Exception) {
            }
        }

        // Refresh the stream type because the stream type may be not properly known for
        // age-restricted videos
        setStreamType()
        if (html5StreamingData == null && playerResponse!!.has(STREAMING_DATA)) {
            html5StreamingData = playerResponse!!.getObject(STREAMING_DATA)
        }
        if (html5StreamingData == null) {
            checkPlayabilityStatus(youtubePlayerResponse, playabilityStatus)
        }

        // The microformat JSON object of the content is not returned on the client we use to
        // try to get streams of unavailable contents but is still returned on the WEB client,
        // so we need to store it instead of getting it directly from the playerResponse
        playerMicroFormatRenderer = youtubePlayerResponse!!.getObject("microformat")
                .getObject("playerMicroformatRenderer")
        if (isPlayerResponseNotValid(playerResponse, videoId)) {
            throw ExtractionException("Initial player response is not valid")
        }
        val body: ByteArray = JsonWriter.string(
                YoutubeParsingHelper.prepareDesktopJsonBuilder(localization, contentCountry)
                        .value(YoutubeParsingHelper.VIDEO_ID, videoId)
                        .value(YoutubeParsingHelper.CONTENT_CHECK_OK, true)
                        .value(YoutubeParsingHelper.RACY_CHECK_OK, true)
                        .done())
                .toByteArray(StandardCharsets.UTF_8)
        nextResponse = YoutubeParsingHelper.getJsonPostResponse(NEXT, body, localization)

        // streamType can only have LIVE_STREAM, POST_LIVE_STREAM and VIDEO_STREAM values (see
        // setStreamType()), so this block will be run only for POST_LIVE_STREAM and VIDEO_STREAM
        // values if fetching of the ANDROID client is not forced
        if (((!isAgeRestricted && streamType != StreamType.LIVE_STREAM)
                        || isAndroidClientFetchForced)) {
            try {
                fetchAndroidMobileJsonPlayer(contentCountry, localization, videoId)
            } catch (ignored: Exception) {
                // Ignore exceptions related to ANDROID client fetch or parsing, as it is not
                // compulsory to play contents
            }
        }
        if (((!isAgeRestricted && streamType == StreamType.LIVE_STREAM)
                        || isIosClientFetchForced)) {
            try {
                fetchIosMobileJsonPlayer(contentCountry, localization, videoId)
            } catch (ignored: Exception) {
                // Ignore exceptions related to IOS client fetch or parsing, as it is not
                // compulsory to play contents
            }
        }
    }

    @Throws(ParsingException::class)
    private fun checkPlayabilityStatus(youtubePlayerResponse: JsonObject?,
                                       playabilityStatus: JsonObject) {
        var status: String? = playabilityStatus.getString("status")
        if (status == null || status.equals("ok", ignoreCase = true)) {
            return
        }

        // If status exist, and is not "OK", throw the specific exception based on error message
        // or a ContentNotAvailableException with the reason text if it's an unknown reason.
        val newPlayabilityStatus: JsonObject = youtubePlayerResponse!!.getObject("playabilityStatus")
        status = newPlayabilityStatus.getString("status")
        val reason: String? = newPlayabilityStatus.getString("reason")
        if (status.equals("login_required", ignoreCase = true)) {
            if (reason == null) {
                val message: String? = newPlayabilityStatus.getArray("messages").getString(0)
                if (message != null && message.contains("private")) {
                    throw PrivateContentException("This video is private.")
                }
            } else if (reason.contains("age")) {
                // No streams can be fetched, therefore throw an AgeRestrictedContentException
                // explicitly.
                throw AgeRestrictedContentException(
                        "This age-restricted video cannot be watched.")
            }
        }
        if (((status.equals("unplayable", ignoreCase = true) || status.equals("error", ignoreCase = true))
                        && reason != null)) {
            if (reason.contains("Music Premium")) {
                throw YoutubeMusicPremiumContentException()
            }
            if (reason.contains("payment")) {
                throw PaidContentException("This video is a paid video")
            }
            if (reason.contains("members-only")) {
                throw PaidContentException(("This video is only available"
                        + " for members of the channel of this video"))
            }
            if (reason.contains("unavailable")) {
                val detailedErrorMessage: String? = YoutubeParsingHelper.getTextFromObject(newPlayabilityStatus
                        .getObject("errorScreen")
                        .getObject("playerErrorMessageRenderer")
                        .getObject("subreason"))
                if (detailedErrorMessage != null && detailedErrorMessage.contains("country")) {
                    throw GeographicRestrictionException(
                            "This video is not available in client's country.")
                } else if (detailedErrorMessage != null) {
                    throw ContentNotAvailableException(detailedErrorMessage)
                } else {
                    throw ContentNotAvailableException(reason)
                }
            }
        }
        throw ContentNotAvailableException("Got error: \"" + reason + "\"")
    }

    /**
     * Fetch the Android Mobile API and assign the streaming data to the androidStreamingData JSON
     * object.
     */
    @Throws(IOException::class, ExtractionException::class)
    private fun fetchAndroidMobileJsonPlayer(contentCountry: ContentCountry?,
                                             localization: Localization?,
                                             videoId: String?) {
        androidCpn = YoutubeParsingHelper.generateContentPlaybackNonce()
        val mobileBody: ByteArray = JsonWriter.string(
                YoutubeParsingHelper.prepareAndroidMobileJsonBuilder(localization, contentCountry)
                        .value(YoutubeParsingHelper.VIDEO_ID, videoId)
                        .value(YoutubeParsingHelper.CPN, androidCpn)
                        .value(YoutubeParsingHelper.CONTENT_CHECK_OK, true)
                        .value(YoutubeParsingHelper.RACY_CHECK_OK, true) // Workaround getting streaming URLs which return 403 HTTP response code by
                        // using some parameters for Android client requests
                        .value("params", "CgIQBg")
                        .done())
                .toByteArray(StandardCharsets.UTF_8)
        val androidPlayerResponse: JsonObject? = YoutubeParsingHelper.getJsonAndroidPostResponse(PLAYER,
                mobileBody, localization, ("&t=" + YoutubeParsingHelper.generateTParameter()
                + "&id=" + videoId))
        if (isPlayerResponseNotValid(androidPlayerResponse, videoId)) {
            return
        }
        val streamingData: JsonObject = androidPlayerResponse!!.getObject(STREAMING_DATA)
        if (!Utils.isNullOrEmpty(streamingData)) {
            androidStreamingData = streamingData
            if (html5StreamingData == null) {
                playerResponse = androidPlayerResponse
            }
        }
    }

    /**
     * Fetch the iOS Mobile API and assign the streaming data to the iosStreamingData JSON
     * object.
     */
    @Throws(IOException::class, ExtractionException::class)
    private fun fetchIosMobileJsonPlayer(contentCountry: ContentCountry?,
                                         localization: Localization?,
                                         videoId: String?) {
        iosCpn = YoutubeParsingHelper.generateContentPlaybackNonce()
        val mobileBody: ByteArray = JsonWriter.string(
                YoutubeParsingHelper.prepareIosMobileJsonBuilder(localization, contentCountry)
                        .value(YoutubeParsingHelper.VIDEO_ID, videoId)
                        .value(YoutubeParsingHelper.CPN, iosCpn)
                        .value(YoutubeParsingHelper.CONTENT_CHECK_OK, true)
                        .value(YoutubeParsingHelper.RACY_CHECK_OK, true)
                        .done())
                .toByteArray(StandardCharsets.UTF_8)
        val iosPlayerResponse: JsonObject? = YoutubeParsingHelper.getJsonIosPostResponse(PLAYER,
                mobileBody, localization, ("&t=" + YoutubeParsingHelper.generateTParameter()
                + "&id=" + videoId))
        if (isPlayerResponseNotValid(iosPlayerResponse, videoId)) {
            return
        }
        val streamingData: JsonObject = iosPlayerResponse!!.getObject(STREAMING_DATA)
        if (!Utils.isNullOrEmpty(streamingData)) {
            iosStreamingData = streamingData
            if (html5StreamingData == null) {
                playerResponse = iosPlayerResponse
            }
        }
    }

    /**
     * Download the `TVHTML5_SIMPLY_EMBEDDED_PLAYER` JSON player as an embed client to bypass
     * some age-restrictions and assign the streaming data to the `html5StreamingData` JSON
     * object.
     *
     * @param contentCountry the content country to use
     * @param localization   the localization to use
     * @param videoId        the video id
     */
    @Throws(IOException::class, ExtractionException::class)
    private fun fetchTvHtml5EmbedJsonPlayer(contentCountry: ContentCountry?,
                                            localization: Localization?,
                                            videoId: String?) {
        // Because a cpn is unique to each request, we need to generate it again
        html5Cpn = YoutubeParsingHelper.generateContentPlaybackNonce()
        val tvHtml5EmbedPlayerResponse: JsonObject? = YoutubeParsingHelper.getJsonPostResponse(PLAYER,
                YoutubeParsingHelper.createDesktopPlayerBody(localization,
                        contentCountry,
                        videoId,
                        YoutubeJavaScriptPlayerManager.getSignatureTimestamp(videoId),
                        true,
                        html5Cpn), localization)
        if (isPlayerResponseNotValid(tvHtml5EmbedPlayerResponse, videoId)) {
            return
        }
        val streamingData: JsonObject = tvHtml5EmbedPlayerResponse!!.getObject(
                STREAMING_DATA)
        if (!Utils.isNullOrEmpty(streamingData)) {
            playerResponse = tvHtml5EmbedPlayerResponse
            html5StreamingData = streamingData
        }
    }

    private fun getVideoSecondaryInfoRenderer(): JsonObject? {
        if (videoSecondaryInfoRenderer != null) {
            return videoSecondaryInfoRenderer
        }
        videoSecondaryInfoRenderer = getVideoInfoRenderer("videoSecondaryInfoRenderer")
        return videoSecondaryInfoRenderer
    }

    private fun getVideoInfoRenderer(videoRendererName: String): JsonObject {
        return nextResponse!!.getObject("contents")
                .getObject("twoColumnWatchNextResults")
                .getObject("results")
                .getObject("results")
                .getArray("contents")
                .stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .filter(Predicate({ content: JsonObject -> content.has(videoRendererName) }))
                .map(Function({ content: JsonObject -> content.getObject(videoRendererName) }))
                .findFirst()
                .orElse(JsonObject())
    }

    @Throws(ParsingException::class)
    private fun <T : org.schabi.newpipe.extractor.stream.Stream?> getItags(
            streamingDataKey: String,
            itagTypeWanted: ItagType,
            streamBuilderHelper: Function<ItagInfo?, T>,
            streamTypeExceptionMessage: String): List<T?> {
        try {
            val videoId: String? = getId()
            val streamList: MutableList<T?> = ArrayList()
            Stream.of<Pair<JsonObject?, String?>>( // Use the androidStreamingData object first because there is no n param and no
                    // signatureCiphers in streaming URLs of the Android client
                    Pair<JsonObject?, String?>(androidStreamingData, androidCpn),
                    Pair<JsonObject?, String?>(html5StreamingData, html5Cpn),  // Use the iosStreamingData object in the last position because most of the
                    // available streams can be extracted with the Android and web clients and also
                    // because the iOS client is only enabled by default on livestreams
                    Pair<JsonObject?, String?>(iosStreamingData, iosCpn)
            )
                    .flatMap<ItagInfo?>(Function<Pair<JsonObject?, String?>, Stream<out ItagInfo?>>({ pair: Pair<JsonObject?, String?> ->
                        getStreamsFromStreamingDataKey(videoId, pair.getFirst(),
                                streamingDataKey, itagTypeWanted, pair.getSecond())
                    }))
                    .map<T>(streamBuilderHelper)
                    .forEachOrdered(Consumer<T>({ stream: T ->
                        if (!org.schabi.newpipe.extractor.stream.Stream.Companion.containSimilarStream(stream, streamList)) {
                            streamList.add(stream)
                        }
                    }))
            return streamList
        } catch (e: Exception) {
            throw ParsingException(
                    "Could not get " + streamTypeExceptionMessage + " streams", e)
        }
    }

    private val audioStreamBuilderHelper: Function<ItagInfo?, AudioStream?>
        /**
         * Get the stream builder helper which will be used to build [AudioStream]s in
         * [.getItags]
         *
         *
         *
         * The `StreamBuilderHelper` will set the following attributes in the
         * [AudioStream]s built:
         *
         *  * the [ItagItem]'s id of the stream as its id;
         *  * [ItagInfo.getContent] and [ItagInfo.getIsUrl] as its content and
         * and as the value of `isUrl`;
         *  * the media format returned by the [ItagItem] as its media format;
         *  * its average bitrate with the value returned by [     ][ItagItem.getAverageBitrate];
         *  * the [ItagItem];
         *  * the [DASH delivery method][DeliveryMethod.DASH], for OTF streams, live streams
         * and ended streams.
         *
         *
         *
         *
         *
         * Note that the [ItagItem] comes from an [ItagInfo] instance.
         *
         *
         * @return a stream builder helper to build [AudioStream]s
         */
        private get() {
            return Function({ itagInfo: ItagInfo? ->
                val itagItem: ItagItem? = itagInfo.getItagItem()
                val builder: AudioStream.Builder? = AudioStream.Builder()
                        .setId(itagItem!!.id.toString())
                        .setContent(itagInfo.getContent(), itagInfo.getIsUrl())
                        .setMediaFormat(itagItem.getMediaFormat())
                        .setAverageBitrate(itagItem.getAverageBitrate())
                        .setAudioTrackId(itagItem.getAudioTrackId())
                        .setAudioTrackName(itagItem.getAudioTrackName())
                        .setAudioLocale(itagItem.getAudioLocale())
                        .setAudioTrackType(itagItem.getAudioTrackType())
                        .setItagItem(itagItem)
                if ((streamType == StreamType.LIVE_STREAM
                                ) || (streamType == StreamType.POST_LIVE_STREAM
                                ) || !itagInfo.getIsUrl()) {
                    // For YouTube videos on OTF streams and for all streams of post-live streams
                    // and live streams, only the DASH delivery method can be used.
                    builder!!.setDeliveryMethod(DeliveryMethod.DASH)
                }
                builder!!.build()
            })
        }

    /**
     * Get the stream builder helper which will be used to build [VideoStream]s in
     * [.getItags]
     *
     *
     *
     * The `StreamBuilderHelper` will set the following attributes in the
     * [VideoStream]s built:
     *
     *  * the [ItagItem]'s id of the stream as its id;
     *  * [ItagInfo.getContent] and [ItagInfo.getIsUrl] as its content and
     * and as the value of `isUrl`;
     *  * the media format returned by the [ItagItem] as its media format;
     *  * whether it is video-only with the `areStreamsVideoOnly` parameter
     *  * the [ItagItem];
     *  * the resolution, by trying to use, in this order:
     *
     *  1. the height returned by the [ItagItem] + `p` + the frame rate if
     * it is more than 30;
     *  1. the default resolution string from the [ItagItem];
     *  1. an empty string.
     *
     *
     *  * the [DASH delivery method][DeliveryMethod.DASH], for OTF streams, live streams
     * and ended streams.
     *
     *
     *
     *
     * Note that the [ItagItem] comes from an [ItagInfo] instance.
     *
     *
     * @param areStreamsVideoOnly whether the stream builder helper will set the video
     * streams as video-only streams
     * @return a stream builder helper to build [VideoStream]s
     */
    private fun getVideoStreamBuilderHelper(
            areStreamsVideoOnly: Boolean): Function<ItagInfo?, VideoStream?> {
        return Function({ itagInfo: ItagInfo? ->
            val itagItem: ItagItem? = itagInfo.getItagItem()
            val builder: VideoStream.Builder? = VideoStream.Builder()
                    .setId(itagItem!!.id.toString())
                    .setContent(itagInfo.getContent(), itagInfo.getIsUrl())
                    .setMediaFormat(itagItem.getMediaFormat())
                    .setIsVideoOnly(areStreamsVideoOnly)
                    .setItagItem(itagItem)
            val resolutionString: String? = itagItem.getResolutionString()
            builder!!.setResolution(if (resolutionString != null) resolutionString else "")
            if (streamType != StreamType.VIDEO_STREAM || !itagInfo.getIsUrl()) {
                // For YouTube videos on OTF streams and for all streams of post-live streams
                // and live streams, only the DASH delivery method can be used.
                builder.setDeliveryMethod(DeliveryMethod.DASH)
            }
            builder.build()
        })
    }

    private fun getStreamsFromStreamingDataKey(
            videoId: String?,
            streamingData: JsonObject?,
            streamingDataKey: String,
            itagTypeWanted: ItagType,
            contentPlaybackNonce: String?): Stream<ItagInfo?> {
        if (streamingData == null || !streamingData.has(streamingDataKey)) {
            return Stream.empty()
        }
        return streamingData.getArray(streamingDataKey).stream()
                .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map<ItagInfo?>(Function<JsonObject, ItagInfo?>({ formatData: JsonObject ->
                    try {
                        val itagItem: ItagItem = ItagItem.Companion.getItag(formatData.getInt("itag"))
                        if (itagItem.itagType == itagTypeWanted) {
                            return@map buildAndAddItagInfoToList(videoId, formatData, itagItem,
                                    itagItem.itagType, contentPlaybackNonce)
                        }
                    } catch (ignored: IOException) {
                        // if the itag is not supported and getItag fails, we end up here
                    } catch (ignored: ExtractionException) {
                    }
                    null
                }))
                .filter(Predicate<ItagInfo?>({ obj: ItagInfo? -> Objects.nonNull(obj) }))
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun buildAndAddItagInfoToList(
            videoId: String?,
            formatData: JsonObject,
            itagItem: ItagItem,
            itagType: ItagType?,
            contentPlaybackNonce: String?): ItagInfo {
        var streamUrl: String?
        if (formatData.has("url")) {
            streamUrl = formatData.getString("url")
        } else {
            // This url has an obfuscated signature
            val cipherString: String = if (formatData.has(CIPHER)) formatData.getString(CIPHER) else formatData.getString(SIGNATURE_CIPHER)
            val cipher: Map<String?, String?>? = Parser.compatParseMap(
                    cipherString)
            streamUrl = (cipher!!.get("url") + "&" + cipher.get("sp") + "="
                    + YoutubeJavaScriptPlayerManager.deobfuscateSignature(videoId, cipher.get("s")))
        }

        // Add the content playback nonce to the stream URL
        streamUrl += "&" + YoutubeParsingHelper.CPN + "=" + contentPlaybackNonce

        // Decrypt the n parameter if it is present
        streamUrl = tryDeobfuscateThrottlingParameterOfUrl(streamUrl, videoId)
        val initRange: JsonObject = formatData.getObject("initRange")
        val indexRange: JsonObject = formatData.getObject("indexRange")
        val mimeType: String = formatData.getString("mimeType", "")
        val codec: String = if (mimeType.contains("codecs")) mimeType.split("\"".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(1) else ""
        itagItem.setBitrate(formatData.getInt("bitrate"))
        itagItem.setWidth(formatData.getInt("width"))
        itagItem.setHeight(formatData.getInt("height"))
        itagItem.setInitStart(initRange.getString("start", "-1").toInt())
        itagItem.setInitEnd(initRange.getString("end", "-1").toInt())
        itagItem.setIndexStart(indexRange.getString("start", "-1").toInt())
        itagItem.setIndexEnd(indexRange.getString("end", "-1").toInt())
        itagItem.setQuality(formatData.getString("quality"))
        itagItem.setCodec(codec)
        if (streamType == StreamType.LIVE_STREAM || streamType == StreamType.POST_LIVE_STREAM) {
            itagItem.setTargetDurationSec(formatData.getInt("targetDurationSec"))
        }
        if (itagType == ItagType.VIDEO || itagType == ItagType.VIDEO_ONLY) {
            itagItem.setFps(formatData.getInt("fps"))
        } else if (itagType == ItagType.AUDIO) {
            // YouTube return the audio sample rate as a string
            itagItem.setSampleRate(formatData.getString("audioSampleRate").toInt())
            itagItem.setAudioChannels(formatData.getInt("audioChannels",  // Most audio streams have two audio channels, so use this value if the real
                    // count cannot be extracted
                    // Doing this prevents an exception when generating the
                    // AudioChannelConfiguration element of DASH manifests of audio streams in
                    // YoutubeDashManifestCreatorUtils
                    2))
            val audioTrackId: String = formatData.getObject("audioTrack")
                    .getString("id")
            if (!Utils.isNullOrEmpty(audioTrackId)) {
                itagItem.setAudioTrackId(audioTrackId)
                val audioTrackIdLastLocaleCharacter: Int = audioTrackId.indexOf(".")
                if (audioTrackIdLastLocaleCharacter != -1) {
                    // Audio tracks IDs are in the form LANGUAGE_CODE.TRACK_NUMBER
                    LocaleCompat.forLanguageTag(
                            audioTrackId.substring(0, audioTrackIdLastLocaleCharacter)
                    ).ifPresent(Consumer({ audioLocale: Locale? -> itagItem.setAudioLocale(audioLocale) }))
                }
                itagItem.setAudioTrackType(YoutubeParsingHelper.extractAudioTrackType(streamUrl))
            }
            itagItem.setAudioTrackName(formatData.getObject("audioTrack")
                    .getString("displayName"))
        }

        // YouTube return the content length and the approximate duration as strings
        itagItem.setContentLength(formatData.getString("contentLength", ItagItem.Companion.CONTENT_LENGTH_UNKNOWN.toString()).toLong())
        itagItem.setApproxDurationMs(formatData.getString("approxDurationMs", ItagItem.Companion.APPROX_DURATION_MS_UNKNOWN.toString()).toLong())
        val itagInfo: ItagInfo = ItagInfo(streamUrl, itagItem)
        if (streamType == StreamType.VIDEO_STREAM) {
            itagInfo.setIsUrl(!formatData.getString("type", "")
                    .equals("FORMAT_STREAM_TYPE_OTF", ignoreCase = true))
        } else {
            // We are currently not able to generate DASH manifests for running
            // livestreams, so because of the requirements of StreamInfo
            // objects, return these streams as DASH URL streams (even if they
            // are not playable).
            // Ended livestreams are returned as non URL streams
            itagInfo.setIsUrl(streamType != StreamType.POST_LIVE_STREAM)
        }
        return itagInfo
    }

    @get:Throws(ExtractionException::class)
    override val frames: List<Frameset>
        get() {
            try {
                val storyboards: JsonObject = playerResponse!!.getObject("storyboards")
                val storyboardsRenderer: JsonObject? = storyboards.getObject(
                        if (storyboards.has("playerLiveStoryboardSpecRenderer")) "playerLiveStoryboardSpecRenderer" else "playerStoryboardSpecRenderer"
                )
                if (storyboardsRenderer == null) {
                    return emptyList()
                }
                val storyboardsRendererSpec: String? = storyboardsRenderer.getString("spec")
                if (storyboardsRendererSpec == null) {
                    return emptyList()
                }
                val spec: Array<String> = storyboardsRendererSpec.split("\\|".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val url: String = spec.get(0)
                val result: MutableList<Frameset> = ArrayList(spec.size - 1)
                for (i in 1 until spec.size) {
                    val parts: Array<String> = spec.get(i).split("#".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    if (parts.size != 8 || parts.get(5).toInt() == 0) {
                        continue
                    }
                    val totalCount: Int = parts.get(2).toInt()
                    val framesPerPageX: Int = parts.get(3).toInt()
                    val framesPerPageY: Int = parts.get(4).toInt()
                    val baseUrl: String = url.replace("\$L", (i - 1).toString())
                            .replace("\$N", parts.get(6)) + "&sigh=" + parts.get(7)
                    val urls: List<String>
                    if (baseUrl.contains("\$M")) {
                        val totalPages: Int = ceil(totalCount / (framesPerPageX * framesPerPageY).toDouble()).toInt()
                        urls = ArrayList(totalPages)
                        for (j in 0 until totalPages) {
                            urls.add(baseUrl.replace("\$M", j.toString()))
                        }
                    } else {
                        urls = listOf(baseUrl)
                    }
                    result.add(Frameset(
                            urls, parts.get(0).toInt(), parts.get(1).toInt(),
                            totalCount, parts.get(5).toInt(),
                            framesPerPageX,
                            framesPerPageY
                    ))
                }
                return result
            } catch (e: Exception) {
                throw ExtractionException("Could not get frames", e)
            }
        }

    override val privacy: Privacy?
        get() {
            return if (playerMicroFormatRenderer!!.getBoolean("isUnlisted")) Privacy.UNLISTED else Privacy.PUBLIC
        }

    override val category: String?
        get() {
            return playerMicroFormatRenderer!!.getString("category", "")
        }

    @get:Throws(ParsingException::class)
    override val licence: String?
        get() {
            val metadataRowRenderer: JsonObject = getVideoSecondaryInfoRenderer()
                    .getObject("metadataRowContainer")
                    .getObject("metadataRowContainerRenderer")
                    .getArray("rows")
                    .getObject(0)
                    .getObject("metadataRowRenderer")
            val contents: JsonArray = metadataRowRenderer.getArray("contents")
            val license: String? = YoutubeParsingHelper.getTextFromObject(contents.getObject(0))
            return if ((license != null
                            && ("Licence" == YoutubeParsingHelper.getTextFromObject(metadataRowRenderer.getObject("title"))))) license else "YouTube licence"
        }
    override val languageInfo: Locale?
        get() {
            return null
        }

    override val tags: List<String?>?
        get() {
            return JsonUtils.getStringListFromJsonArray(playerResponse!!.getObject("videoDetails")
                    .getArray("keywords"))
        }

    @get:Throws(ParsingException::class)
    override val streamSegments: List<StreamSegment>
        get() {
            if (!nextResponse!!.has("engagementPanels")) {
                return emptyList()
            }
            val segmentsArray: JsonArray? = nextResponse!!.getArray("engagementPanels")
                    .stream() // Check if object is a JsonObject
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) })) // Check if the panel is the correct one
                    .filter(Predicate({ panel: JsonObject ->
                        ("engagement-panel-macro-markers-description-chapters" ==
                                panel
                                        .getObject("engagementPanelSectionListRenderer")
                                        .getString("panelIdentifier"))
                    })) // Extract the data
                    .map(Function({ panel: JsonObject ->
                        panel
                                .getObject("engagementPanelSectionListRenderer")
                                .getObject("content")
                                .getObject("macroMarkersListRenderer")
                                .getArray("contents")
                    }))
                    .findFirst()
                    .orElse(null)

            // If no data was found exit
            if (segmentsArray == null) {
                return emptyList()
            }
            val duration: Long = length
            val segments: MutableList<StreamSegment> = ArrayList()
            for (segmentJson: JsonObject in segmentsArray.stream()
                    .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map<JsonObject>(Function<JsonObject, JsonObject>({ `object`: JsonObject -> `object`.getObject("macroMarkersListItemRenderer") }))
                    .collect(Collectors.toList<JsonObject>())) {
                val startTimeSeconds: Int = segmentJson.getObject("onTap")
                        .getObject("watchEndpoint").getInt("startTimeSeconds", -1)
                if (startTimeSeconds == -1) {
                    throw ParsingException("Could not get stream segment start time.")
                }
                if (startTimeSeconds > duration) {
                    break
                }
                val title: String? = YoutubeParsingHelper.getTextFromObject(segmentJson.getObject("title"))
                if (Utils.isNullOrEmpty(title)) {
                    throw ParsingException("Could not get stream segment title.")
                }
                val segment: StreamSegment = StreamSegment(title, startTimeSeconds)
                segment.setUrl(getUrl() + "?t=" + startTimeSeconds)
                if (segmentJson.has("thumbnail")) {
                    val previewsArray: JsonArray = segmentJson
                            .getObject("thumbnail")
                            .getArray("thumbnails")
                    if (!previewsArray.isEmpty()) {
                        // Assume that the thumbnail with the highest resolution is at the last position
                        val url: String = previewsArray
                                .getObject(previewsArray.size - 1)
                                .getString("url")
                        segment.setPreviewUrl(YoutubeParsingHelper.fixThumbnailUrl(url))
                    }
                }
                segments.add(segment)
            }
            return segments
        }

    @get:Throws(ParsingException::class)
    override val metaInfo: List<MetaInfo?>?
        get() {
            return YoutubeMetaInfoHelper.getMetaInfo(nextResponse
                    .getObject("contents")
                    .getObject("twoColumnWatchNextResults")
                    .getObject("results")
                    .getObject("results")
                    .getArray("contents"))
        }

    companion object {
        private var isAndroidClientFetchForced: Boolean = false
        private var isIosClientFetchForced: Boolean = false
        @Throws(ParsingException::class)
        private fun parseLikeCountFromLikeButtonRenderer(
                topLevelButtons: JsonArray): Long {
            var likesString: String? = null
            val likeToggleButtonRenderer: JsonObject? = topLevelButtons.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ button: JsonObject ->
                        button.getObject("segmentedLikeDislikeButtonRenderer")
                                .getObject("likeButton")
                                .getObject("toggleButtonRenderer")
                    }))
                    .filter(Predicate({ toggleButtonRenderer: JsonObject? -> !Utils.isNullOrEmpty(toggleButtonRenderer) }))
                    .findFirst()
                    .orElse(null)
            if (likeToggleButtonRenderer != null) {
                // Use one of the accessibility strings available (this one has the same path as the
                // one used for comments' like count extraction)
                likesString = likeToggleButtonRenderer.getObject("accessibilityData")
                        .getObject("accessibilityData")
                        .getString("label")

                // Use the other accessibility string available which contains the exact like count
                if (likesString == null) {
                    likesString = likeToggleButtonRenderer.getObject("accessibility")
                            .getString("label")
                }

                // Last method: use the defaultText's accessibility data, which contains the exact like
                // count too, except when it is equal to 0, where a localized string is returned instead
                if (likesString == null) {
                    likesString = likeToggleButtonRenderer.getObject("defaultText")
                            .getObject("accessibility")
                            .getObject("accessibilityData")
                            .getString("label")
                }

                // This check only works with English localizations!
                if (likesString != null && likesString.lowercase(Locale.getDefault()).contains("no likes")) {
                    return 0
                }
            }

            // If ratings are allowed and the likes string is null, it means that we couldn't extract
            // the full like count from accessibility data
            if (likesString == null) {
                throw ParsingException("Could not get like count from accessibility data")
            }
            try {
                return Utils.removeNonDigitCharacters(likesString).toLong()
            } catch (e: NumberFormatException) {
                throw ParsingException("Could not parse \"" + likesString + "\" as a long", e)
            }
        }

        @Throws(ParsingException::class)
        private fun parseLikeCountFromLikeButtonViewModel(
                topLevelButtons: JsonArray): Long {
            // Try first with the current video actions buttons data structure
            val likeToggleButtonViewModel: JsonObject? = topLevelButtons.stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .map(Function({ button: JsonObject ->
                        button.getObject("segmentedLikeDislikeButtonViewModel")
                                .getObject("likeButtonViewModel")
                                .getObject("likeButtonViewModel")
                                .getObject("toggleButtonViewModel")
                                .getObject("toggleButtonViewModel")
                                .getObject("defaultButtonViewModel")
                                .getObject("buttonViewModel")
                    }))
                    .filter(Predicate({ buttonViewModel: JsonObject? -> !Utils.isNullOrEmpty(buttonViewModel) }))
                    .findFirst()
                    .orElse(null)
            if (likeToggleButtonViewModel == null) {
                throw ParsingException("Could not find buttonViewModel object")
            }
            val accessibilityText: String? = likeToggleButtonViewModel.getString("accessibilityText")
            if (accessibilityText == null) {
                throw ParsingException("Could not find buttonViewModel's accessibilityText string")
            }

            // The like count is always returned as a number in this element, even for videos with no
            // likes
            try {
                return Utils.removeNonDigitCharacters(accessibilityText).toLong()
            } catch (e: NumberFormatException) {
                throw ParsingException(
                        "Could not parse \"" + accessibilityText + "\" as a long", e)
            }
        }

        private fun getManifestUrl(manifestType: String,
                                   streamingDataObjects: List<JsonObject?>): String {
            val manifestKey: String = manifestType + "ManifestUrl"
            return streamingDataObjects.stream()
                    .filter(Predicate({ obj: JsonObject? -> Objects.nonNull(obj) }))
                    .map(Function({ streamingDataObject: JsonObject? -> streamingDataObject!!.getString(manifestKey) }))
                    .filter(Predicate({ obj: String? -> Objects.nonNull(obj) }))
                    .findFirst()
                    .orElse("")
        }

        /*//////////////////////////////////////////////////////////////////////////
    // Fetch page
    ////////////////////////////////////////////////////////////////////////// */
        private val FORMATS: String = "formats"
        private val ADAPTIVE_FORMATS: String = "adaptiveFormats"
        private val STREAMING_DATA: String = "streamingData"
        private val PLAYER: String = "player"
        private val NEXT: String = "next"
        private val SIGNATURE_CIPHER: String = "signatureCipher"
        private val CIPHER: String = "cipher"

        /**
         * Checks whether a player response is invalid.
         *
         *
         *
         * If YouTube detect that requests come from a third party client, they may replace the real
         * player response by another one of a video saying that this content is not available on this
         * app and to watch it on the latest version of YouTube. This behavior has been observed on the
         * `ANDROID` client, see
         * [
 * https://github.com/TeamNewPipe/NewPipe/issues/8713](https://github.com/TeamNewPipe/NewPipe/issues/8713).
         *
         *
         *
         *
         * YouTube may also sometimes for currently unknown reasons rate-limit an IP, and replace the
         * real one by a player response with a video that says that the requested video is
         * unavailable. This behaviour has been observed in Piped on the InnerTube clients used by the
         * extractor (`ANDROID` and `WEB` clients) which should apply for all clients, see
         * [
 * https://github.com/TeamPiped/Piped/issues/2487](https://github.com/TeamPiped/Piped/issues/2487).
         *
         *
         *
         *
         * We can detect this by checking whether the video ID of the player response returned is the
         * same as the one requested by the extractor.
         *
         *
         * @param playerResponse a player response from any client
         * @param videoId        the video ID of the content requested
         * @return whether the video ID of the player response is not equal to the one requested
         */
        private fun isPlayerResponseNotValid(
                playerResponse: JsonObject?,
                videoId: String?): Boolean {
            return !(videoId == playerResponse!!.getObject("videoDetails")
                    .getString("videoId"))
        }

        /**
         * Enable or disable the fetch of the Android client for all stream types.
         *
         *
         *
         * By default, the fetch of the Android client will be made only on videos, in order to reduce
         * data usage, because available streams of the Android client will be almost equal to the ones
         * available on the `WEB` client: you can get exclusively a 48kbps audio stream and a
         * 3GPP very low stream (which is, most of times, a 144p8 stream).
         *
         *
         * @param forceFetchAndroidClientValue whether to always fetch the Android client and not only
         * for videos
         */
        fun forceFetchAndroidClient(forceFetchAndroidClientValue: Boolean) {
            isAndroidClientFetchForced = forceFetchAndroidClientValue
        }

        /**
         * Enable or disable the fetch of the iOS client for all stream types.
         *
         *
         *
         * By default, the fetch of the iOS client will be made only on livestreams, in order to get an
         * HLS manifest with separated audio and video which has also an higher replay time (up to one
         * hour, depending of the content instead of 30 seconds with non-iOS clients).
         *
         *
         *
         *
         * Enabling this option will allow you to get an HLS manifest also for regular videos, which
         * contains resolutions up to 1080p60.
         *
         *
         * @param forceFetchIosClientValue whether to always fetch the iOS client and not only for
         * livestreams
         */
        fun forceFetchIosClient(forceFetchIosClientValue: Boolean) {
            isIosClientFetchForced = forceFetchIosClientValue
        }
    }
}
