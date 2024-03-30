/*
 * Created by Christian Schabesberger on 02.03.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeParsingHelper.java is part of NewPipe Extractor.
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
package org.schabi.newpipe.extractor.services.youtube

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonBuilder
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import com.grack.nanojson.JsonWriter
import org.jsoup.nodes.Entities
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.AccountTerminatedException
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.stream.AudioTrackType
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.RandomStringFromAlphabetGenerator
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.Optional
import java.util.Random
import java.util.function.Function
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

object YoutubeParsingHelper {
    /**
     * The base URL of requests of the `WEB` clients to the InnerTube internal API.
     */
    @JvmField
    val YOUTUBEI_V1_URL: String = "https://www.youtube.com/youtubei/v1/"

    /**
     * The base URL of requests of non-web clients to the InnerTube internal API.
     */
    val YOUTUBEI_V1_GAPIS_URL: String = "https://youtubei.googleapis.com/youtubei/v1/"

    /**
     * The base URL of YouTube Music.
     */
    private val YOUTUBE_MUSIC_URL: String = "https://music.youtube.com"

    /**
     * A parameter to disable pretty-printed response of InnerTube requests, to reduce response
     * sizes.
     *
     *
     *
     * Sent in query parameters of the requests, **after** the API key.
     *
     */
    @JvmField
    val DISABLE_PRETTY_PRINT_PARAMETER: String = "&prettyPrint=false"

    /**
     * A parameter sent by official clients named `contentPlaybackNonce`.
     *
     *
     *
     * It is sent by official clients on videoplayback requests, and by all clients (except the
     * `WEB` one to the player requests.
     *
     *
     *
     *
     * It is composed of 16 characters which are generated from
     * [this alphabet][.CONTENT_PLAYBACK_NONCE_ALPHABET], with the use of strong random
     * values.
     *
     *
     * @see .generateContentPlaybackNonce
     */
    val CPN: String = "cpn"
    val VIDEO_ID: String = "videoId"

    /**
     * A parameter sent by official clients named `contentCheckOk`.
     *
     *
     *
     * Setting it to `true` allows us to get streaming data on videos with a warning about
     * what the sensible content they contain.
     *
     */
    val CONTENT_CHECK_OK: String = "contentCheckOk"

    /**
     * A parameter which may be sent by official clients named `racyCheckOk`.
     *
     *
     *
     * What this parameter does is not really known, but it seems to be linked to sensitive
     * contents such as age-restricted content.
     *
     */
    val RACY_CHECK_OK: String = "racyCheckOk"

    /**
     * The client version for InnerTube requests with the `WEB` client, used as the last
     * fallback if the extraction of the real one failed.
     */
    private val HARDCODED_CLIENT_VERSION: String = "2.20231208.01.00"

    /**
     * The InnerTube API key which should be used by YouTube's desktop website, used as a fallback
     * if the extraction of the real one failed.
     */
    private val HARDCODED_KEY: String = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

    /**
     * The hardcoded client version of the Android app used for InnerTube requests with this
     * client.
     *
     *
     *
     * It can be extracted by getting the latest release version of the app in an APK repository
     * such as [APKMirror](https://www.apkmirror.com/apk/google-inc/youtube/).
     *
     */
    private val ANDROID_YOUTUBE_CLIENT_VERSION: String = "18.48.37"

    /**
     * The InnerTube API key used by the `ANDROID` client. Found with the help of
     * reverse-engineering app network requests.
     */
    private val ANDROID_YOUTUBE_KEY: String = "AIzaSyA8eiZmM1FaDVjRy-df2KTyQ_vz_yYM39w"

    /**
     * The hardcoded client version of the iOS app used for InnerTube requests with this
     * client.
     *
     *
     *
     * It can be extracted by getting the latest release version of the app on
     * [the App
 * Store page of the YouTube app](https://apps.apple.com/us/app/youtube-watch-listen-stream/id544007664/), in the `What’s New` section.
     *
     */
    private val IOS_YOUTUBE_CLIENT_VERSION: String = "18.48.3"

    /**
     * The InnerTube API key used by the `iOS` client. Found with the help of
     * reverse-engineering app network requests.
     */
    private val IOS_YOUTUBE_KEY: String = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc"

    /**
     * The hardcoded client version used for InnerTube requests with the TV HTML5 embed client.
     */
    private val TVHTML5_SIMPLY_EMBED_CLIENT_VERSION: String = "2.0"
    private var clientVersion: String? = null
    private var key: String? = null
    private val HARDCODED_YOUTUBE_MUSIC_KEY: Array<String?> = arrayOf("AIzaSyC9XL3ZjWddXya6X74dJoCTL-WEYFDNX30", "67", "1.20231204.01.00")
    private var youtubeMusicKey: Array<String?>?
    private var keyAndVersionExtracted: Boolean = false
    private var hardcodedClientVersionAndKeyValid: Optional<Boolean> = Optional.empty()
    private val INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES: Array<String?> = arrayOf("INNERTUBE_CONTEXT_CLIENT_VERSION\":\"([0-9\\.]+?)\"",
            "innertube_context_client_version\":\"([0-9\\.]+?)\"",
            "client.version=([0-9\\.]+)")
    private val INNERTUBE_API_KEY_REGEXES: Array<String?> = arrayOf("INNERTUBE_API_KEY\":\"([0-9a-zA-Z_-]+?)\"",
            "innertubeApiKey\":\"([0-9a-zA-Z_-]+?)\"")
    private val INITIAL_DATA_REGEXES: Array<String?> = arrayOf("window\\[\"ytInitialData\"\\]\\s*=\\s*(\\{.*?\\});",
            "var\\s*ytInitialData\\s*=\\s*(\\{.*?\\});")
    private val INNERTUBE_CLIENT_NAME_REGEX: String = "INNERTUBE_CONTEXT_CLIENT_NAME\":([0-9]+?),"
    private val CONTENT_PLAYBACK_NONCE_ALPHABET: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    /**
     * The device machine id for the iPhone 15, used to get 60fps with the `iOS` client.
     *
     *
     *
     * See [this GitHub Gist](https://gist.github.com/adamawolf/3048717) for more
     * information.
     *
     */
    private val IOS_DEVICE_MODEL: String = "iPhone15,4"
    private var numberGenerator: Random = Random()
    private val FEED_BASE_CHANNEL_ID: String = "https://www.youtube.com/feeds/videos.xml?channel_id="
    private val FEED_BASE_USER: String = "https://www.youtube.com/feeds/videos.xml?user="
    private val C_WEB_PATTERN: Pattern = Pattern.compile("&c=WEB")
    private val C_TVHTML5_SIMPLY_EMBEDDED_PLAYER_PATTERN: Pattern = Pattern.compile("&c=TVHTML5_SIMPLY_EMBEDDED_PLAYER")
    private val C_ANDROID_PATTERN: Pattern = Pattern.compile("&c=ANDROID")
    private val C_IOS_PATTERN: Pattern = Pattern.compile("&c=IOS")
    private val GOOGLE_URLS: Set<String> = setOf("google.", "m.google.", "www.google.")
    private val INVIDIOUS_URLS: Set<String> = setOf("invidio.us", "dev.invidio.us",
            "www.invidio.us", "redirect.invidious.io", "invidious.snopyta.org", "yewtu.be",
            "tube.connect.cafe", "tubus.eduvid.org", "invidious.kavin.rocks", "invidious.site",
            "invidious-us.kavin.rocks", "piped.kavin.rocks", "vid.mint.lgbt", "invidiou.site",
            "invidious.fdn.fr", "invidious.048596.xyz", "invidious.zee.li", "vid.puffyan.us",
            "ytprivate.com", "invidious.namazso.eu", "invidious.silkky.cloud", "ytb.trom.tf",
            "invidious.exonip.de", "inv.riverside.rocks", "invidious.blamefran.net", "y.com.cm",
            "invidious.moomoo.me", "yt.cyberhost.uk")
    private val YOUTUBE_URLS: Set<String> = setOf("youtube.com", "www.youtube.com",
            "m.youtube.com", "music.youtube.com")
    /**
     * Get the value of the consent's acceptance.
     *
     * @see .setConsentAccepted
     * @return the consent's acceptance value
     */
    /**
     * Determines how the consent cookie that is required for YouTube, `SOCS`, will be
     * generated.
     *
     *
     *  * `false` (the default value) will use `CAE=`;
     *  * `true` will use `CAISAiAD`.
     *
     *
     *
     *
     * Setting this value to `true` is needed to extract mixes and some YouTube Music
     * playlists in some countries such as the EU ones.
     *
     */
    var isConsentAccepted: Boolean = false
    fun isGoogleURL(url: String?): Boolean {
        val cachedUrl: String? = extractCachedUrlIfNeeded(url)
        try {
            val u: URL = URL(cachedUrl)
            return GOOGLE_URLS.stream().anyMatch(Predicate({ item: String? -> u.getHost().startsWith((item)!!) }))
        } catch (e: MalformedURLException) {
            return false
        }
    }

    fun isYoutubeURL(@Nonnull url: URL?): Boolean {
        return YOUTUBE_URLS.contains(url!!.getHost().lowercase())
    }

    fun isYoutubeServiceURL(@Nonnull url: URL?): Boolean {
        val host: String = url!!.getHost()
        return (host.equals("www.youtube-nocookie.com", ignoreCase = true)
                || host.equals("youtu.be", ignoreCase = true))
    }

    fun isHooktubeURL(@Nonnull url: URL?): Boolean {
        val host: String = url!!.getHost()
        return host.equals("hooktube.com", ignoreCase = true)
    }

    fun isInvidiousURL(@Nonnull url: URL?): Boolean {
        return INVIDIOUS_URLS.contains(url!!.getHost().lowercase())
    }

    fun isY2ubeURL(@Nonnull url: URL?): Boolean {
        return url!!.getHost().equals("y2u.be", ignoreCase = true)
    }

    /**
     * Parses the duration string of the video expecting ":" or "." as separators
     *
     * @return the duration in seconds
     * @throws ParsingException when more than 3 separators are found
     */
    @JvmStatic
    @Throws(ParsingException::class, NumberFormatException::class)
    fun parseDurationString(@Nonnull input: String?): Int {
        // If time separator : is not detected, try . instead
        val splitInput: Array<String> = if (input!!.contains(":")) input.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray() else input.split("\\.".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val units: IntArray = intArrayOf(24, 60, 60, 1)
        val offset: Int = units.size - splitInput.size
        if (offset < 0) {
            throw ParsingException("Error duration string with unknown format: " + input)
        }
        var duration: Int = 0
        for (i in splitInput.indices) {
            duration = units.get(i + offset) * (duration + convertDurationToInt(splitInput.get(i)))
        }
        return duration
    }

    /**
     * Tries to convert a duration string to an integer without throwing an exception.
     * <br></br>
     * Helper method for [.parseDurationString].
     * <br></br>
     * Note: This method is also used as a workaround for NewPipe#8034 (YT shorts no longer
     * display any duration in channels).
     *
     * @param input The string to process
     * @return The converted integer or 0 if the conversion failed.
     */
    private fun convertDurationToInt(input: String?): Int {
        if (input == null || input.isEmpty()) {
            return 0
        }
        val clearedInput: String? = Utils.removeNonDigitCharacters(input)
        try {
            return clearedInput!!.toInt()
        } catch (ex: NumberFormatException) {
            return 0
        }
    }

    @Nonnull
    fun getFeedUrlFrom(@Nonnull channelIdOrUser: String?): String {
        if (channelIdOrUser!!.startsWith("user/")) {
            return FEED_BASE_USER + channelIdOrUser.replace("user/", "")
        } else if (channelIdOrUser.startsWith("channel/")) {
            return FEED_BASE_CHANNEL_ID + channelIdOrUser.replace("channel/", "")
        } else {
            return FEED_BASE_CHANNEL_ID + channelIdOrUser
        }
    }

    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String?): OffsetDateTime {
        try {
            return OffsetDateTime.parse(textualUploadDate)
        } catch (e: DateTimeParseException) {
            try {
                return LocalDate.parse(textualUploadDate).atStartOfDay().atOffset(ZoneOffset.UTC)
            } catch (e1: DateTimeParseException) {
                throw ParsingException("Could not parse date: \"" + textualUploadDate + "\"",
                        e1)
            }
        }
    }

    /**
     * Checks if the given playlist id is a YouTube Mix (auto-generated playlist)
     * Ids from a YouTube Mix start with "RD"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube Mix
     */
    fun isYoutubeMixId(@Nonnull playlistId: String?): Boolean {
        return playlistId!!.startsWith("RD")
    }

    /**
     * Checks if the given playlist id is a YouTube My Mix (auto-generated playlist)
     * Ids from a YouTube My Mix start with "RDMM"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube My Mix
     */
    fun isYoutubeMyMixId(@Nonnull playlistId: String): Boolean {
        return playlistId.startsWith("RDMM")
    }

    /**
     * Checks if the given playlist id is a YouTube Music Mix (auto-generated playlist)
     * Ids from a YouTube Music Mix start with "RDAMVM" or "RDCLAK"
     *
     * @param playlistId the playlist id
     * @return Whether given id belongs to a YouTube Music Mix
     */
    fun isYoutubeMusicMixId(@Nonnull playlistId: String?): Boolean {
        return playlistId!!.startsWith("RDAMVM") || playlistId.startsWith("RDCLAK")
    }

    /**
     * Checks if the given playlist id is a YouTube Channel Mix (auto-generated playlist)
     * Ids from a YouTube channel Mix start with "RDCM"
     *
     * @return Whether given id belongs to a YouTube Channel Mix
     */
    fun isYoutubeChannelMixId(@Nonnull playlistId: String?): Boolean {
        return playlistId!!.startsWith("RDCM")
    }

    /**
     * Checks if the given playlist id is a YouTube Genre Mix (auto-generated playlist)
     * Ids from a YouTube Genre Mix start with "RDGMEM"
     *
     * @return Whether given id belongs to a YouTube Genre Mix
     */
    fun isYoutubeGenreMixId(@Nonnull playlistId: String?): Boolean {
        return playlistId!!.startsWith("RDGMEM")
    }

    /**
     * @param playlistId the playlist id to parse
     * @return the [PlaylistInfo.PlaylistType] extracted from the playlistId (mix playlist
     * types included)
     * @throws ParsingException if the playlistId is null or empty, if the playlistId is not a mix,
     * if it is a mix but it's not based on a specific stream (this is the
     * case for channel or genre mixes)
     */
    @Nonnull
    @Throws(ParsingException::class)
    fun extractVideoIdFromMixId(playlistId: String): String {
        if (Utils.isNullOrEmpty(playlistId)) {
            throw ParsingException("Video id could not be determined from empty playlist id")
        } else if (isYoutubeMyMixId(playlistId)) {
            return playlistId.substring(4)
        } else if (isYoutubeMusicMixId(playlistId)) {
            return playlistId.substring(6)
        } else if (isYoutubeChannelMixId(playlistId)) {
            // Channel mixes are of the form RMCM{channelId}, so videoId can't be determined
            throw ParsingException(("Video id could not be determined from channel mix id: "
                    + playlistId))
        } else if (isYoutubeGenreMixId(playlistId)) {
            // Genre mixes are of the form RDGMEM{garbage}, so videoId can't be determined
            throw ParsingException(("Video id could not be determined from genre mix id: "
                    + playlistId))
        } else if (isYoutubeMixId(playlistId)) { // normal mix
            if (playlistId.length != 13) {
                // Stream YouTube mixes are of the form RD{videoId}, but if videoId is not exactly
                // 11 characters then it can't be a video id, hence we are dealing with a different
                // type of mix (e.g. genre mixes handled above, of the form RDGMEM{garbage})
                throw ParsingException(("Video id could not be determined from mix id: "
                        + playlistId))
            }
            return playlistId.substring(2)
        } else { // not a mix
            throw ParsingException(("Video id could not be determined from playlist id: "
                    + playlistId))
        }
    }

    /**
     * @param playlistId the playlist id to parse
     * @return the [PlaylistInfo.PlaylistType] extracted from the playlistId (mix playlist
     * types included)
     * @throws ParsingException if the playlistId is null or empty
     */
    @Nonnull
    @Throws(ParsingException::class)
    fun extractPlaylistTypeFromPlaylistId(
            playlistId: String?): PlaylistType {
        if (Utils.isNullOrEmpty(playlistId)) {
            throw ParsingException("Could not extract playlist type from empty playlist id")
        } else if (isYoutubeMusicMixId(playlistId)) {
            return PlaylistType.MIX_MUSIC
        } else if (isYoutubeChannelMixId(playlistId)) {
            return PlaylistType.MIX_CHANNEL
        } else if (isYoutubeGenreMixId(playlistId)) {
            return PlaylistType.MIX_GENRE
        } else if (isYoutubeMixId(playlistId)) { // normal mix
            // Either a normal mix based on a stream, or a "my mix" (still based on a stream).
            // NOTE: if YouTube introduces even more types of mixes that still start with RD,
            // they will default to this, even though they might not be based on a stream.
            return PlaylistType.MIX_STREAM
        } else {
            // not a known type of mix: just consider it a normal playlist
            return PlaylistType.NORMAL
        }
    }

    /**
     * @param playlistUrl the playlist url to parse
     * @return the [PlaylistInfo.PlaylistType] extracted from the playlistUrl's list param
     * (mix playlist types included)
     * @throws ParsingException if the playlistUrl is malformed, if has no list param or if the list
     * param is empty
     */
    @Throws(ParsingException::class)
    fun extractPlaylistTypeFromPlaylistUrl(
            playlistUrl: String?): PlaylistType {
        try {
            return extractPlaylistTypeFromPlaylistId(
                    Utils.getQueryValue(Utils.stringToURL(playlistUrl), "list"))
        } catch (e: MalformedURLException) {
            throw ParsingException("Could not extract playlist type from malformed url", e)
        }
    }

    @Throws(ParsingException::class)
    private fun getInitialData(html: String?): JsonObject {
        try {
            return JsonParser.`object`().from(Utils.getStringResultFromRegexArray(html,
                    INITIAL_DATA_REGEXES, 1))
        } catch (e: JsonParserException) {
            throw ParsingException("Could not get ytInitialData", e)
        } catch (e: RegexException) {
            throw ParsingException("Could not get ytInitialData", e)
        }
    }

    @JvmStatic
    @Throws(IOException::class, ExtractionException::class)
    fun areHardcodedClientVersionAndKeyValid(): Boolean {
        if (hardcodedClientVersionAndKeyValid.isPresent()) {
            return hardcodedClientVersionAndKeyValid.get()
        }
        // @formatter:off
         val body: ByteArray = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("hl", "en-GB")
        .value("gl", "GB")
        .value("clientName", "WEB")
        .value("clientVersion", HARDCODED_CLIENT_VERSION)
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .value("fetchLiveState", true)
        .end().done().toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val headers: Map<String?, List<String?>?> = getClientHeaders("1", HARDCODED_CLIENT_VERSION)

        // This endpoint is fetched by the YouTube website to get the items of its main menu and is
        // pretty lightweight (around 30kB)
        val response: Response? = NewPipe.getDownloader().postWithContentTypeJson(
                YOUTUBEI_V1_URL + "guide?key=" + HARDCODED_KEY + DISABLE_PRETTY_PRINT_PARAMETER,
                headers, body)
        val responseBody: String? = response!!.responseBody()
        val responseCode: Int = response.responseCode()
        hardcodedClientVersionAndKeyValid = Optional.of((responseBody!!.length > 5000
                && responseCode == 200)) // Ensure to have a valid response
        return hardcodedClientVersionAndKeyValid.get()
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun extractClientVersionAndKeyFromSwJs() {
        if (keyAndVersionExtracted) {
            return
        }
        val url: String = "https://www.youtube.com/sw.js"
        val headers: Map<String?, List<String?>?> = getOriginReferrerHeaders("https://www.youtube.com")
        val response: String? = NewPipe.getDownloader().get(url, headers).responseBody()
        try {
            clientVersion = Utils.getStringResultFromRegexArray(response,
                    INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
            key = Utils.getStringResultFromRegexArray(response, INNERTUBE_API_KEY_REGEXES, 1)
        } catch (e: RegexException) {
            throw ParsingException(("Could not extract YouTube WEB InnerTube client version "
                    + "and API key from sw.js"), e)
        }
        keyAndVersionExtracted = true
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun extractClientVersionAndKeyFromHtmlSearchResultsPage() {
        // Don't extract the client version and the InnerTube key if it has been already extracted
        if (keyAndVersionExtracted) {
            return
        }

        // Don't provide a search term in order to have a smaller response
        val url: String = "https://www.youtube.com/results?search_query=&ucbcb=1"
        val html: String? = NewPipe.getDownloader().get(url, cookieHeader).responseBody()
        val initialData: JsonObject = getInitialData(html)
        val serviceTrackingParams: JsonArray = initialData.getObject("responseContext")
                .getArray("serviceTrackingParams")

        // Try to get version from initial data first
        val serviceTrackingParamsStream: Stream<JsonObject> = serviceTrackingParams.stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
        clientVersion = getClientVersionFromServiceTrackingParam(
                serviceTrackingParamsStream, "CSI", "cver")
        if (clientVersion == null) {
            try {
                clientVersion = Utils.getStringResultFromRegexArray(html,
                        INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
            } catch (ignored: RegexException) {
            }
        }

        // Fallback to get a shortened client version which does not contain the last two
        // digits
        if (Utils.isNullOrEmpty(clientVersion)) {
            clientVersion = getClientVersionFromServiceTrackingParam(
                    serviceTrackingParamsStream, "ECATCHER", "client.version")
        }
        try {
            key = Utils.getStringResultFromRegexArray(html, INNERTUBE_API_KEY_REGEXES, 1)
        } catch (ignored: RegexException) {
        }
        if (Utils.isNullOrEmpty(key)) {
            throw ParsingException( // CHECKSTYLE:OFF
                    "Could not extract YouTube WEB InnerTube API key from HTML search results page")
            // CHECKSTYLE:ON
        }
        if (clientVersion == null) {
            throw ParsingException( // CHECKSTYLE:OFF
                    "Could not extract YouTube WEB InnerTube client version from HTML search results page")
            // CHECKSTYLE:ON
        }
        keyAndVersionExtracted = true
    }

    private fun getClientVersionFromServiceTrackingParam(
            @Nonnull serviceTrackingParamsStream: Stream<JsonObject>,
            @Nonnull serviceName: String,
            @Nonnull clientVersionKey: String): String? {
        return serviceTrackingParamsStream.filter(Predicate({ serviceTrackingParam: JsonObject ->
            (serviceTrackingParam.getString("service", "")
                    == serviceName)
        }))
                .flatMap(Function<JsonObject, Stream<*>>({ serviceTrackingParam: JsonObject ->
                    serviceTrackingParam.getArray("params")
                            .stream()
                }))
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .filter(Predicate({ param: JsonObject ->
                    (param.getString("key", "")
                            == clientVersionKey)
                }))
                .map(Function({ param: JsonObject -> param.getString("value") }))
                .filter(Predicate({ paramValue: String? -> !Utils.isNullOrEmpty(paramValue) }))
                .findFirst()
                .orElse(null)
    }

    /**
     * Get the client version used by YouTube website on InnerTube requests.
     */
    @Throws(IOException::class, ExtractionException::class)
    fun getClientVersion(): String? {
        if (!Utils.isNullOrEmpty(clientVersion)) {
            return clientVersion
        }

        // Always extract the latest client version, by trying first to extract it from the
        // JavaScript service worker, then from HTML search results page as a fallback, to prevent
        // fingerprinting based on the client version used
        try {
            extractClientVersionAndKeyFromSwJs()
        } catch (e: Exception) {
            extractClientVersionAndKeyFromHtmlSearchResultsPage()
        }
        if (keyAndVersionExtracted) {
            return clientVersion
        }

        // Fallback to the hardcoded one if it is valid
        if (areHardcodedClientVersionAndKeyValid()) {
            clientVersion = HARDCODED_CLIENT_VERSION
            return clientVersion
        }
        throw ExtractionException("Could not get YouTube WEB client version")
    }

    /**
     * Get the internal API key used by YouTube website on InnerTube requests.
     */
    @JvmStatic
    @Throws(IOException::class, ExtractionException::class)
    fun getKey(): String? {
        if (!Utils.isNullOrEmpty(key)) {
            return key
        }

        // Always extract the key used by the website, by trying first to extract it from the
        // JavaScript service worker, then from HTML search results page as a fallback, to prevent
        // fingerprinting based on the key and/or invalid key issues
        try {
            extractClientVersionAndKeyFromSwJs()
        } catch (e: Exception) {
            extractClientVersionAndKeyFromHtmlSearchResultsPage()
        }
        if (keyAndVersionExtracted) {
            return key
        }

        // Fallback to the hardcoded one if it's valid
        if (areHardcodedClientVersionAndKeyValid()) {
            key = HARDCODED_KEY
            return key
        }

        // The ANDROID API key is also valid with the WEB client so return it if we couldn't
        // extract the WEB API key. This can be used as a way to fingerprint the extractor in this
        // case
        return ANDROID_YOUTUBE_KEY
    }

    /**
     *
     *
     * **Only used in tests.**
     *
     *
     *
     *
     * Quick-and-dirty solution to reset global state in between test classes.
     *
     *
     *
     * This is needed for the mocks because in order to reach that state a network request has to
     * be made. If the global state is not reset and the RecordingDownloader is used,
     * then only the first test class has that request recorded. Meaning running the other
     * tests with mocks will fail, because the mock is missing.
     *
     */
    @JvmStatic
    fun resetClientVersionAndKey() {
        clientVersion = null
        key = null
        keyAndVersionExtracted = false
    }

    /**
     *
     *
     * **Only used in tests.**
     *
     */
    @JvmStatic
    fun setNumberGenerator(random: Random) {
        numberGenerator = random
    }

    @JvmStatic
    @get:Throws(IOException::class, ReCaptchaException::class)
    val isHardcodedYoutubeMusicKeyValid: Boolean
        get() {
            val url: String = ("https://music.youtube.com/youtubei/v1/music/get_search_suggestions?key="
                    + HARDCODED_YOUTUBE_MUSIC_KEY.get(0) + DISABLE_PRETTY_PRINT_PARAMETER)

            // @formatter:off
         val json: ByteArray = JsonWriter.string()
        .`object`()
        .`object`("context")
        .`object`("client")
        .value("clientName", "WEB_REMIX")
        .value("clientVersion", HARDCODED_YOUTUBE_MUSIC_KEY.get(2))
        .value("hl", "en-GB")
        .value("gl", "GB")
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
        .value("input", "")
        .end().done().toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
            val headers: HashMap<String?, List<String?>?> = HashMap(getOriginReferrerHeaders(YOUTUBE_MUSIC_URL))
            headers.putAll(getClientHeaders(HARDCODED_YOUTUBE_MUSIC_KEY.get(1),
                    HARDCODED_YOUTUBE_MUSIC_KEY.get(2)))
            val response: Response? = NewPipe.getDownloader().postWithContentTypeJson(url, headers, json)
            // Ensure to have a valid response
            return response!!.responseBody().length > 500 && response.responseCode() == 200
        }

    @Throws(IOException::class, ReCaptchaException::class, RegexException::class)
    fun getYoutubeMusicKey(): Array<String?>? {
        if (youtubeMusicKey != null && youtubeMusicKey!!.size == 3) {
            return youtubeMusicKey
        }
        if (isHardcodedYoutubeMusicKeyValid) {
            youtubeMusicKey = HARDCODED_YOUTUBE_MUSIC_KEY
            return youtubeMusicKey
        }
        var musicClientVersion: String?
        var musicKey: String?
        var musicClientName: String?
        try {
            val url: String = "https://music.youtube.com/sw.js"
            val headers: Map<String?, List<String?>?> = getOriginReferrerHeaders(YOUTUBE_MUSIC_URL)
            val response: String? = NewPipe.getDownloader().get(url, headers).responseBody()
            musicClientVersion = Utils.getStringResultFromRegexArray(response,
                    INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
            musicKey = Utils.getStringResultFromRegexArray(response, INNERTUBE_API_KEY_REGEXES, 1)
            musicClientName = Parser.matchGroup1(INNERTUBE_CLIENT_NAME_REGEX, response)
        } catch (e: Exception) {
            val url: String = "https://music.youtube.com/?ucbcb=1"
            val html: String? = NewPipe.getDownloader().get(url, cookieHeader).responseBody()
            musicKey = Utils.getStringResultFromRegexArray(html, INNERTUBE_API_KEY_REGEXES, 1)
            musicClientVersion = Utils.getStringResultFromRegexArray(html,
                    INNERTUBE_CONTEXT_CLIENT_VERSION_REGEXES, 1)
            musicClientName = Parser.matchGroup1(INNERTUBE_CLIENT_NAME_REGEX, html)
        }
        youtubeMusicKey = arrayOf(musicKey, musicClientName, musicClientVersion)
        return youtubeMusicKey
    }

    fun getUrlFromNavigationEndpoint(
            @Nonnull navigationEndpoint: JsonObject): String? {
        if (navigationEndpoint.has("urlEndpoint")) {
            var internUrl: String = navigationEndpoint.getObject("urlEndpoint")
                    .getString("url")
            if (internUrl.startsWith("https://www.youtube.com/redirect?")) {
                // remove https://www.youtube.com part to fall in the next if block
                internUrl = internUrl.substring(23)
            }
            if (internUrl.startsWith("/redirect?")) {
                // q parameter can be the first parameter
                internUrl = internUrl.substring(10)
                val params: Array<String> = internUrl.split("&".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                for (param: String in params) {
                    if ((param.split("=".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(0) == "q")) {
                        try {
                            return Utils.decodeUrlUtf8(param.split("=".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(1))
                        } catch (e: UnsupportedEncodingException) {
                            return null
                        }
                    }
                }
            } else if (internUrl.startsWith("http")) {
                return internUrl
            } else if ((internUrl.startsWith("/channel") || internUrl.startsWith("/user")
                            || internUrl.startsWith("/watch"))) {
                return "https://www.youtube.com" + internUrl
            }
        }
        if (navigationEndpoint.has("browseEndpoint")) {
            val browseEndpoint: JsonObject = navigationEndpoint.getObject("browseEndpoint")
            val canonicalBaseUrl: String = browseEndpoint.getString("canonicalBaseUrl")
            val browseId: String? = browseEndpoint.getString("browseId")

            // All channel ids are prefixed with UC
            if (browseId != null && browseId.startsWith("UC")) {
                return "https://www.youtube.com/channel/" + browseId
            }
            if (!Utils.isNullOrEmpty(canonicalBaseUrl)) {
                return "https://www.youtube.com" + canonicalBaseUrl
            }
        }
        if (navigationEndpoint.has("watchEndpoint")) {
            val url: StringBuilder = StringBuilder()
            url.append("https://www.youtube.com/watch?v=")
                    .append(navigationEndpoint.getObject("watchEndpoint")
                            .getString(VIDEO_ID))
            if (navigationEndpoint.getObject("watchEndpoint").has("playlistId")) {
                url.append("&list=").append(navigationEndpoint.getObject("watchEndpoint")
                        .getString("playlistId"))
            }
            if (navigationEndpoint.getObject("watchEndpoint").has("startTimeSeconds")) {
                url.append("&t=")
                        .append(navigationEndpoint.getObject("watchEndpoint")
                                .getInt("startTimeSeconds"))
            }
            return url.toString()
        }
        if (navigationEndpoint.has("watchPlaylistEndpoint")) {
            return ("https://www.youtube.com/playlist?list="
                    + navigationEndpoint.getObject("watchPlaylistEndpoint")
                    .getString("playlistId"))
        }
        if (navigationEndpoint.has("commandMetadata")) {
            val metadata: JsonObject = navigationEndpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
            if (metadata.has("url")) {
                return "https://www.youtube.com" + metadata.getString("url")
            }
        }
        return null
    }

    /**
     * Get the text from a JSON object that has either a `simpleText` or a `runs`
     * array.
     *
     * @param textObject JSON object to get the text from
     * @param html       whether to return HTML, by parsing the `navigationEndpoint`
     * @return text in the JSON object or `null`
     */
    fun getTextFromObject(textObject: JsonObject?, html: Boolean): String? {
        if (Utils.isNullOrEmpty(textObject)) {
            return null
        }
        if (textObject!!.has("simpleText")) {
            return textObject.getString("simpleText")
        }
        if (textObject.getArray("runs").isEmpty()) {
            return null
        }
        val textBuilder: StringBuilder = StringBuilder()
        for (o: Any in textObject.getArray("runs")) {
            val run: JsonObject = o as JsonObject
            var text: String? = run.getString("text")
            if (html) {
                if (run.has("navigationEndpoint")) {
                    val url: String? = getUrlFromNavigationEndpoint(
                            run.getObject("navigationEndpoint"))
                    if (!Utils.isNullOrEmpty(url)) {
                        text = ("<a href=\"" + Entities.escape((url)!!) + "\">" + Entities.escape((text)!!)
                                + "</a>")
                    }
                }
                val bold: Boolean = (run.has("bold")
                        && run.getBoolean("bold"))
                val italic: Boolean = (run.has("italics")
                        && run.getBoolean("italics"))
                val strikethrough: Boolean = (run.has("strikethrough")
                        && run.getBoolean("strikethrough"))
                if (bold) {
                    textBuilder.append("<b>")
                }
                if (italic) {
                    textBuilder.append("<i>")
                }
                if (strikethrough) {
                    textBuilder.append("<s>")
                }
                textBuilder.append(text)
                if (strikethrough) {
                    textBuilder.append("</s>")
                }
                if (italic) {
                    textBuilder.append("</i>")
                }
                if (bold) {
                    textBuilder.append("</b>")
                }
            } else {
                textBuilder.append(text)
            }
        }
        var text: String = textBuilder.toString()
        if (html) {
            text = text.replace("\\n".toRegex(), "<br>")
            text = text.replace(" {2}".toRegex(), " &nbsp;")
        }
        return text
    }

    /**
     * Parse a video description in the new "attributed" format, which contains the entire visible
     * plaintext (`content`) and an array of `commandRuns`.
     *
     *
     *
     * The `commandRuns` include the links and their position in the text.
     *
     *
     * @param attributedDescription the JSON object of the attributed description
     * @return the parsed description, in HTML format, as a string
     */
    fun getAttributedDescription(
            attributedDescription: JsonObject?): String? {
        if (Utils.isNullOrEmpty(attributedDescription)) {
            return null
        }
        val content: String? = attributedDescription!!.getString("content")
        if (content == null) {
            return null
        }
        val commandRuns: JsonArray = attributedDescription.getArray("commandRuns")
        val textBuilder: StringBuilder = StringBuilder()
        var textStart: Int = 0
        for (commandRun: Any? in commandRuns) {
            if (!(commandRun is JsonObject)) {
                continue
            }
            val run: JsonObject = commandRun
            val startIndex: Int = run.getInt("startIndex", -1)
            val length: Int = run.getInt("length")
            val navigationEndpoint: JsonObject? = run.getObject("onTap")
                    .getObject("innertubeCommand")
            if ((startIndex < 0) || (length < 1) || (navigationEndpoint == null)) {
                continue
            }
            val url: String? = getUrlFromNavigationEndpoint(navigationEndpoint)
            if (url == null) {
                continue
            }

            // Append text before the link
            if (startIndex > textStart) {
                textBuilder.append(content, textStart, startIndex)
            }

            // Trim and append link text
            // Channel/Video format: 3xu00a0, (/ •), u00a0, <Name>, 2xu00a0
            val linkText: String = content.substring(startIndex, startIndex + length)
                    .replace('\u00a0', ' ')
                    .trim({ it <= ' ' })
                    .replaceFirst("^[/•] *".toRegex(), "")
            textBuilder.append("<a href=\"")
                    .append(Entities.escape(url))
                    .append("\">")
                    .append(Entities.escape(linkText))
                    .append("</a>")
            textStart = startIndex + length
        }

        // Append the remaining text
        if (textStart < content.length) {
            textBuilder.append(content.substring(textStart))
        }
        return textBuilder.toString()
                .replace("\\n".toRegex(), "<br>")
                .replace(" {2}".toRegex(), " &nbsp;")
    }

    @Nonnull
    @Throws(ParsingException::class)
    fun getTextFromObjectOrThrow(textObject: JsonObject?, error: String): String {
        val result: String? = getTextFromObject(textObject)
        if (result == null) {
            throw ParsingException("Could not extract text: " + error)
        }
        return result
    }

    fun getTextFromObject(textObject: JsonObject?): String? {
        return getTextFromObject(textObject, false)
    }

    fun getUrlFromObject(textObject: JsonObject): String? {
        if (Utils.isNullOrEmpty(textObject)) {
            return null
        }
        if (textObject.getArray("runs").isEmpty()) {
            return null
        }
        for (textPart: Any in textObject.getArray("runs")) {
            val url: String? = getUrlFromNavigationEndpoint((textPart as JsonObject)
                    .getObject("navigationEndpoint"))
            if (!Utils.isNullOrEmpty(url)) {
                return url
            }
        }
        return null
    }

    fun getTextAtKey(@Nonnull jsonObject: JsonObject?, theKey: String?): String? {
        if (jsonObject!!.isString(theKey)) {
            return jsonObject.getString(theKey)
        } else {
            return getTextFromObject(jsonObject.getObject(theKey))
        }
    }

    fun fixThumbnailUrl(@Nonnull thumbnailUrl: String?): String? {
        var result: String? = thumbnailUrl
        if (result!!.startsWith("//")) {
            result = result.substring(2)
        }
        if (result.startsWith(Utils.HTTP)) {
            result = Utils.replaceHttpWithHttps(result)
        } else if (!result.startsWith(Utils.HTTPS)) {
            result = "https://" + result
        }
        return result
    }

    /**
     * Get thumbnails from a [JsonObject] representing a YouTube
     * [InfoItem][org.schabi.newpipe.extractor.InfoItem].
     *
     *
     *
     * Thumbnails are got from the `thumbnails` [JsonArray] inside the `thumbnail`
     * [JsonObject] of the YouTube [InfoItem][org.schabi.newpipe.extractor.InfoItem],
     * using [.getImagesFromThumbnailsArray].
     *
     *
     * @param infoItem a YouTube [InfoItem][org.schabi.newpipe.extractor.InfoItem]
     * @return an unmodifiable list of [Image]s found in the `thumbnails`
     * [JsonArray]
     * @throws ParsingException if an exception occurs when
     * [.getImagesFromThumbnailsArray] is executed
     */
    @Nonnull
    @Throws(ParsingException::class)
    fun getThumbnailsFromInfoItem(@Nonnull infoItem: JsonObject): List<Image> {
        try {
            return getImagesFromThumbnailsArray(infoItem.getObject("thumbnail")
                    .getArray("thumbnails"))
        } catch (e: Exception) {
            throw ParsingException("Could not get thumbnails from InfoItem", e)
        }
    }

    /**
     * Get images from a YouTube `thumbnails` [JsonArray].
     *
     *
     *
     * The properties of the [Image]s created will be set using the corresponding ones of
     * thumbnail items.
     *
     *
     * @param thumbnails a YouTube `thumbnails` [JsonArray]
     * @return an unmodifiable list of [Image]s extracted from the given [JsonArray]
     */
    @Nonnull
    fun getImagesFromThumbnailsArray(
            @Nonnull thumbnails: JsonArray?): List<Image> {
        return thumbnails!!.stream()
                .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .filter(Predicate<JsonObject>({ thumbnail: JsonObject -> !Utils.isNullOrEmpty(thumbnail.getString("url")) }))
                .map<Image>(Function<JsonObject, Image>({ thumbnail: JsonObject ->
                    val height: Int = thumbnail.getInt("height", Image.Companion.HEIGHT_UNKNOWN)
                    Image(fixThumbnailUrl(thumbnail.getString("url")),
                            height,
                            thumbnail.getInt("width", Image.Companion.WIDTH_UNKNOWN),
                            ResolutionLevel.Companion.fromHeight(height))
                }))
                .collect(Collectors.toUnmodifiableList<Image>())
    }

    @Nonnull
    @Throws(ParsingException::class, MalformedURLException::class)
    fun getValidJsonResponseBody(@Nonnull response: Response?): String? {
        if (response!!.responseCode() == 404) {
            throw ContentNotAvailableException(("Not found"
                    + " (\"" + response.responseCode() + " " + response.responseMessage() + "\")"))
        }
        val responseBody: String? = response.responseBody()
        if (responseBody!!.length < 50) { // Ensure to have a valid response
            throw ParsingException("JSON response is too short")
        }

        // Check if the request was redirected to the error page.
        val latestUrl: URL = URL(response.latestUrl())
        if (latestUrl.getHost().equals("www.youtube.com", ignoreCase = true)) {
            val path: String = latestUrl.getPath()
            if (path.equals("/oops", ignoreCase = true) || path.equals("/error", ignoreCase = true)) {
                throw ContentNotAvailableException("Content unavailable")
            }
        }
        val responseContentType: String? = response.getHeader("Content-Type")
        if ((responseContentType != null
                        && responseContentType.lowercase(Locale.getDefault()).contains("text/html"))) {
            throw ParsingException(("Got HTML document, expected JSON response"
                    + " (latest url was: \"" + response.latestUrl() + "\")"))
        }
        return responseBody
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getJsonPostResponse(endpoint: String,
                            body: ByteArray?,
                            localization: Localization?): JsonObject? {
        val headers: Map<String?, List<String?>?> = youTubeHeaders
        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                NewPipe.getDownloader().postWithContentTypeJson((YOUTUBEI_V1_URL + endpoint + "?key="
                        + getKey() + DISABLE_PRETTY_PRINT_PARAMETER), headers, body, localization)))
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getJsonAndroidPostResponse(
            endpoint: String,
            body: ByteArray,
            @Nonnull localization: Localization?,
            endPartOfUrlRequest: String?): JsonObject? {
        return getMobilePostResponse(endpoint, body, localization,
                getAndroidUserAgent(localization), ANDROID_YOUTUBE_KEY, endPartOfUrlRequest)
    }

    @Throws(IOException::class, ExtractionException::class)
    fun getJsonIosPostResponse(
            endpoint: String,
            body: ByteArray,
            @Nonnull localization: Localization?,
            endPartOfUrlRequest: String?): JsonObject? {
        return getMobilePostResponse(endpoint, body, localization, getIosUserAgent(localization),
                IOS_YOUTUBE_KEY, endPartOfUrlRequest)
    }

    @Throws(IOException::class, ExtractionException::class)
    private fun getMobilePostResponse(
            endpoint: String,
            body: ByteArray,
            @Nonnull localization: Localization?,
            @Nonnull userAgent: String,
            @Nonnull innerTubeApiKey: String,
            endPartOfUrlRequest: String?): JsonObject? {
        val headers: Map<String?, List<String?>?> = java.util.Map.of("User-Agent", java.util.List.of(userAgent),
                "X-Goog-Api-Format-Version", listOf("2"))
        val baseEndpointUrl: String = (YOUTUBEI_V1_GAPIS_URL + endpoint + "?key=" + innerTubeApiKey
                + DISABLE_PRETTY_PRINT_PARAMETER)
        return JsonUtils.toJsonObject(getValidJsonResponseBody(
                NewPipe.getDownloader().postWithContentTypeJson(if (Utils.isNullOrEmpty(endPartOfUrlRequest)) baseEndpointUrl else baseEndpointUrl + endPartOfUrlRequest,
                        headers, body, localization)))
    }

    @JvmStatic
    @Nonnull
    @Throws(IOException::class, ExtractionException::class)
    fun prepareDesktopJsonBuilder(
            @Nonnull localization: Localization?,
            @Nonnull contentCountry: ContentCountry?): JsonBuilder<JsonObject?> {
        return prepareDesktopJsonBuilder(localization, contentCountry, null)
    }

    @JvmStatic
    @Nonnull
    @Throws(IOException::class, ExtractionException::class)
    fun prepareDesktopJsonBuilder(
            @Nonnull localization: Localization?,
            @Nonnull contentCountry: ContentCountry?,
            visitorData: String?): JsonBuilder<JsonObject?> {
        // @formatter:off
     val builder: JsonBuilder<JsonObject?> = JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("hl", localization.getLocalizationCode())
        .value("gl", contentCountry.getCountryCode())
        .value("clientName", "WEB")
        .value("clientVersion", getClientVersion())
        .value("originalUrl", "https://www.youtube.com")
        .value("platform", "DESKTOP")
        .value("utcOffsetMinutes", 0)
        if (visitorData != null) {
            builder.value("visitorData", visitorData)
        }
        return builder.end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    @Nonnull
    fun prepareAndroidMobileJsonBuilder(
            @Nonnull localization: Localization?,
            @Nonnull contentCountry: ContentCountry?): JsonBuilder<JsonObject?> {
        // @formatter:off
    return JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("clientName", "ANDROID")
        .value("clientVersion", ANDROID_YOUTUBE_CLIENT_VERSION)
        .value("platform", "MOBILE")
        .value("osName", "Android")
        .value("osVersion", "14") /*
                        A valid Android SDK version is required to be sure to get a valid player
                        response
                        If this parameter is not provided, the player response is replaced by an
                        error saying the message "The following content is not available on this
                        app. Watch this content on the latest version on YouTube" (it was
                        previously a 5-minute video with this message)
                        See https://github.com/TeamNewPipe/NewPipe/issues/8713
                        The Android SDK version corresponding to the Android version used in
                        requests is sent
                        */
        .value("androidSdkVersion", 34)
        .value("hl", localization.getLocalizationCode())
        .value("gl", contentCountry.getCountryCode())
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    @Nonnull
    fun prepareIosMobileJsonBuilder(
            @Nonnull localization: Localization?,
            @Nonnull contentCountry: ContentCountry?): JsonBuilder<JsonObject?> {
        // @formatter:off
    return JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("clientName", "IOS")
        .value("clientVersion", IOS_YOUTUBE_CLIENT_VERSION)
        .value("deviceMake", "Apple") // Device model is required to get 60fps streams
        .value("deviceModel", IOS_DEVICE_MODEL)
        .value("platform", "MOBILE")
        .value("osName", "iOS") /*
                        The value of this field seems to use the following structure:
                        "iOS major version.minor version.patch version.build version", where
                        "patch version" is equal to 0 if it isn't set
                        The build version corresponding to the iOS version used can be found on
                        https://theapplewiki.com/wiki/Firmware/iPhone/17.x#iPhone_15
                         */
        .value("osVersion", "17.1.2.21B101")
        .value("hl", localization.getLocalizationCode())
        .value("gl", contentCountry.getCountryCode())
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    @Nonnull
    fun prepareTvHtml5EmbedJsonBuilder(
            @Nonnull localization: Localization?,
            @Nonnull contentCountry: ContentCountry?,
            @Nonnull videoId: String?): JsonBuilder<JsonObject> {
        // @formatter:off
    return JsonObject.builder()
        .`object`("context")
        .`object`("client")
        .value("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER")
        .value("clientVersion", TVHTML5_SIMPLY_EMBED_CLIENT_VERSION)
        .value("clientScreen", "EMBED")
        .value("platform", "TV")
        .value("hl", localization.getLocalizationCode())
        .value("gl", contentCountry.getCountryCode())
        .value("utcOffsetMinutes", 0)
        .end()
        .`object`("thirdParty")
        .value("embedUrl", "https://www.youtube.com/watch?v=" + videoId)
        .end()
        .`object`("request")
        .array("internalExperimentFlags")
        .end()
        .value("useSsl", true)
        .end()
        .`object`("user") // TODO: provide a way to enable restricted mode with:
 //  .value("enableSafetyMode", boolean)
        .value("lockedSafetyMode", false)
        .end()
        .end()
            // @formatter:on
    }

    @Nonnull
    @Throws(IOException::class, ExtractionException::class)
    fun createDesktopPlayerBody(
            @Nonnull localization: Localization?,
            @Nonnull contentCountry: ContentCountry?,
            @Nonnull videoId: String?,
            @Nonnull sts: Int?,
            isTvHtml5DesktopJsonBuilder: Boolean,
            @Nonnull contentPlaybackNonce: String?): ByteArray {
        // @formatter:off
    return JsonWriter.string((if (isTvHtml5DesktopJsonBuilder)prepareTvHtml5EmbedJsonBuilder(localization, contentCountry, videoId) else prepareDesktopJsonBuilder(localization, contentCountry))
        .`object`("playbackContext")
        .`object`("contentPlaybackContext") // Signature timestamp from the JavaScript base player is needed to get
 // working obfuscated URLs
        .value("signatureTimestamp", sts)
        .value("referer", "https://www.youtube.com/watch?v=" + videoId)
        .end()
        .end()
        .value(CPN, contentPlaybackNonce)
        .value(VIDEO_ID, videoId)
        .value(CONTENT_CHECK_OK, true)
        .value(RACY_CHECK_OK, true)
        .done())
        .toByteArray(StandardCharsets.UTF_8)
            // @formatter:on
    }

    /**
     * Get the user-agent string used as the user-agent for InnerTube requests with the Android
     * client.
     *
     *
     *
     * If the [Localization] provided is `null`, fallbacks to
     * [the default one][Localization.DEFAULT].
     *
     *
     * @param localization the [Localization] to set in the user-agent
     * @return the Android user-agent used for InnerTube requests with the Android client,
     * depending on the [Localization] provided
     */
    @Nonnull
    fun getAndroidUserAgent(localization: Localization?): String {
        // Spoofing an Android 14 device with the hardcoded version of the Android app
        return ("com.google.android.youtube/" + ANDROID_YOUTUBE_CLIENT_VERSION
                + " (Linux; U; Android 14; "
                + (if (localization != null) localization else Localization.Companion.DEFAULT).getCountryCode()
                + ") gzip")
    }

    /**
     * Get the user-agent string used as the user-agent for InnerTube requests with the iOS
     * client.
     *
     *
     *
     * If the [Localization] provided is `null`, fallbacks to
     * [the default one][Localization.DEFAULT].
     *
     *
     * @param localization the [Localization] to set in the user-agent
     * @return the iOS user-agent used for InnerTube requests with the iOS client, depending on the
     * [Localization] provided
     */
    @Nonnull
    fun getIosUserAgent(localization: Localization?): String {
        // Spoofing an iPhone 15 running iOS 17.1.2 with the hardcoded version of the iOS app
        return ("com.google.ios.youtube/" + IOS_YOUTUBE_CLIENT_VERSION
                + "(" + IOS_DEVICE_MODEL + "; U; CPU iOS 17_1_2 like Mac OS X; "
                + (if (localization != null) localization else Localization.Companion.DEFAULT).getCountryCode()
                + ")")
    }

    @get:Nonnull
    val youtubeMusicHeaders: Map<String?, List<String?>?>
        /**
         * Returns a [Map] containing the required YouTube Music headers.
         */
        get() {
            val headers: HashMap<String?, List<String?>?> = HashMap(getOriginReferrerHeaders(YOUTUBE_MUSIC_URL))
            headers.putAll(getClientHeaders(youtubeMusicKey!!.get(1), youtubeMusicKey!!.get(2)))
            return headers
        }

    @get:Throws(ExtractionException::class, IOException::class)
    val youTubeHeaders: Map<String?, List<String?>?>
        /**
         * Returns a [Map] containing the required YouTube headers, including the
         * `CONSENT` cookie to prevent redirects to `consent.youtube.com`
         */
        get() {
            val headers: MutableMap<String?, List<String?>?> = clientInfoHeaders
            headers.put("Cookie", java.util.List.of(generateConsentCookie()))
            return headers
        }

    @get:Throws(ExtractionException::class, IOException::class)
    val clientInfoHeaders: MutableMap<String?, List<String?>?>
        /**
         * Returns a [Map] containing the `X-YouTube-Client-Name`,
         * `X-YouTube-Client-Version`, `Origin`, and `Referer` headers.
         */
        get() {
            val headers: HashMap<String?, List<String?>?> = HashMap(getOriginReferrerHeaders("https://www.youtube.com"))
            headers.putAll(getClientHeaders("1", getClientVersion()))
            return headers
        }

    /**
     * Returns an unmodifiable [Map] containing the `Origin` and `Referer`
     * headers set to the given URL.
     *
     * @param url The URL to be set as the origin and referrer.
     */
    private fun getOriginReferrerHeaders(@Nonnull url: String): Map<String?, List<String?>?> {
        val urlList: List<String?> = java.util.List.of(url)
        return java.util.Map.of("Origin", urlList, "Referer", urlList)
    }

    /**
     * Returns an unmodifiable [Map] containing the `X-YouTube-Client-Name` and
     * `X-YouTube-Client-Version` headers.
     *
     * @param name The X-YouTube-Client-Name value.
     * @param version X-YouTube-Client-Version value.
     */
    private fun getClientHeaders(@Nonnull name: String?,
                                 @Nonnull version: String?): Map<String?, List<String?>?> {
        return java.util.Map.of("X-YouTube-Client-Name", java.util.List.of(name),
                "X-YouTube-Client-Version", java.util.List.of(version))
    }

    val cookieHeader: Map<String?, List<String?>?>
        /**
         * Create a map with the required cookie header.
         * @return A singleton map containing the header.
         */
        get() {
            return java.util.Map.of("Cookie", java.util.List.of(generateConsentCookie()))
        }

    @Nonnull
    fun generateConsentCookie(): String {
        return "SOCS=" + (if (isConsentAccepted // CAISAiAD means that the user configured manually cookies YouTube, regardless of
        // the consent values
        // This value surprisingly allows to extract mixes and some YouTube Music playlists
        // in the same way when a user allows all cookies
        ) "CAISAiAD" // CAE= means that the user rejected all non-necessary cookies with the "Reject
        // all" button on the consent page
        else "CAE=")
    }

    fun extractCookieValue(cookieName: String,
                           @Nonnull response: Response?): String {
        val cookies: List<String?>? = response!!.responseHeaders().get("set-cookie")
        if (cookies == null) {
            return ""
        }
        var result: String = ""
        for (cookie: String? in cookies) {
            val startIndex: Int = cookie!!.indexOf(cookieName)
            if (startIndex != -1) {
                result = cookie.substring(startIndex + cookieName.length + "=".length,
                        cookie.indexOf(";", startIndex))
            }
        }
        return result
    }

    /**
     * Shared alert detection function, multiple endpoints return the error similarly structured.
     *
     *
     * Will check if the object has an alert of the type "ERROR".
     *
     *
     * @param initialData the object which will be checked if an alert is present
     * @throws ContentNotAvailableException if an alert is detected
     */
    @Throws(ParsingException::class)
    fun defaultAlertsCheck(@Nonnull initialData: JsonObject?) {
        val alerts: JsonArray = initialData!!.getArray("alerts")
        if (!Utils.isNullOrEmpty(alerts)) {
            val alertRenderer: JsonObject = alerts.getObject(0).getObject("alertRenderer")
            val alertText: String? = getTextFromObject(alertRenderer.getObject("text"))
            val alertType: String = alertRenderer.getString("type", "")
            if (alertType.equals("ERROR", ignoreCase = true)) {
                if ((alertText != null
                                && (alertText.contains("This account has been terminated")
                                || alertText.contains("This channel was removed")))) {
                    if ((alertText.matches(".*violat(ed|ion|ing).*".toRegex())
                                    || alertText.contains("infringement"))) {
                        // Possible error messages:
                        // "This account has been terminated for a violation of YouTube's Terms of
                        //     Service."
                        // "This account has been terminated due to multiple or severe violations of
                        //     YouTube's policy prohibiting hate speech."
                        // "This account has been terminated due to multiple or severe violations of
                        //     YouTube's policy prohibiting content designed to harass, bully or
                        //     threaten."
                        // "This account has been terminated due to multiple or severe violations
                        //     of YouTube's policy against spam, deceptive practices and misleading
                        //     content or other Terms of Service violations."
                        // "This account has been terminated due to multiple or severe violations of
                        //     YouTube's policy on nudity or sexual content."
                        // "This account has been terminated for violating YouTube's Community
                        //     Guidelines."
                        // "This account has been terminated because we received multiple
                        //     third-party claims of copyright infringement regarding material that
                        //     the user posted."
                        // "This account has been terminated because it is linked to an account that
                        //     received multiple third-party claims of copyright infringement."
                        // "This channel was removed because it violated our Community Guidelines."
                        throw AccountTerminatedException(alertText,
                                AccountTerminatedException.Reason.VIOLATION)
                    } else {
                        throw AccountTerminatedException(alertText)
                    }
                }
                throw ContentNotAvailableException("Got error: \"" + alertText + "\"")
            }
        }
    }

    /**
     * Sometimes, YouTube provides URLs which use Google's cache. They look like
     * `https://webcache.googleusercontent.com/search?q=cache:CACHED_URL`
     *
     * @param url the URL which might refer to the Google's webcache
     * @return the URL which is referring to the original site
     */
    @JvmStatic
    fun extractCachedUrlIfNeeded(url: String?): String? {
        if (url == null) {
            return null
        }
        if (url.contains("webcache.googleusercontent.com")) {
            return url.split("cache:".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(1)
        }
        return url
    }

    fun isVerified(badges: JsonArray): Boolean {
        if (Utils.isNullOrEmpty(badges)) {
            return false
        }
        for (badge: Any in badges) {
            val style: String? = (badge as JsonObject).getObject("metadataBadgeRenderer")
                    .getString("style")
            if (style != null && (((style == "BADGE_STYLE_TYPE_VERIFIED") || (style == "BADGE_STYLE_TYPE_VERIFIED_ARTIST")))) {
                return true
            }
        }
        return false
    }

    /**
     * Generate a content playback nonce (also called `cpn`), sent by YouTube clients in
     * playback requests (and also for some clients, in the player request body).
     *
     * @return a content playback nonce string
     */
    @Nonnull
    fun generateContentPlaybackNonce(): String? {
        return RandomStringFromAlphabetGenerator.generate(
                CONTENT_PLAYBACK_NONCE_ALPHABET, 16, numberGenerator)
    }

    /**
     * Try to generate a `t` parameter, sent by mobile clients as a query of the player
     * request.
     *
     *
     *
     * Some researches needs to be done to know how this parameter, unique at each request, is
     * generated.
     *
     *
     * @return a 12 characters string to try to reproduce the `` parameter
     */
    @Nonnull
    fun generateTParameter(): String? {
        return RandomStringFromAlphabetGenerator.generate(
                CONTENT_PLAYBACK_NONCE_ALPHABET, 12, numberGenerator)
    }

    /**
     * Check if the streaming URL is from the YouTube `WEB` client.
     *
     * @param url the streaming URL to be checked.
     * @return true if it's a `WEB` streaming URL, false otherwise
     */
    fun isWebStreamingUrl(@Nonnull url: String?): Boolean {
        return Parser.isMatch(C_WEB_PATTERN, url)
    }

    /**
     * Check if the streaming URL is a URL from the YouTube `TVHTML5_SIMPLY_EMBEDDED_PLAYER`
     * client.
     *
     * @param url the streaming URL on which check if it's a `TVHTML5_SIMPLY_EMBEDDED_PLAYER`
     * streaming URL.
     * @return true if it's a `TVHTML5_SIMPLY_EMBEDDED_PLAYER` streaming URL, false otherwise
     */
    fun isTvHtml5SimplyEmbeddedPlayerStreamingUrl(@Nonnull url: String?): Boolean {
        return Parser.isMatch(C_TVHTML5_SIMPLY_EMBEDDED_PLAYER_PATTERN, url)
    }

    /**
     * Check if the streaming URL is a URL from the YouTube `ANDROID` client.
     *
     * @param url the streaming URL to be checked.
     * @return true if it's a `ANDROID` streaming URL, false otherwise
     */
    fun isAndroidStreamingUrl(@Nonnull url: String?): Boolean {
        return Parser.isMatch(C_ANDROID_PATTERN, url)
    }

    /**
     * Check if the streaming URL is a URL from the YouTube `IOS` client.
     *
     * @param url the streaming URL on which check if it's a `IOS` streaming URL.
     * @return true if it's a `IOS` streaming URL, false otherwise
     */
    fun isIosStreamingUrl(@Nonnull url: String?): Boolean {
        return Parser.isMatch(C_IOS_PATTERN, url)
    }

    /**
     * Extract the audio track type from a YouTube stream URL.
     *
     *
     * The track type is parsed from the `xtags` URL parameter
     * (Example: `acont=original:lang=en`).
     *
     * @param streamUrl YouTube stream URL
     * @return [AudioTrackType] or `null` if no track type was found
     */
    @JvmStatic
    fun extractAudioTrackType(streamUrl: String?): AudioTrackType? {
        val xtags: String?
        try {
            xtags = Utils.getQueryValue(URL(streamUrl), "xtags")
        } catch (e: MalformedURLException) {
            return null
        }
        if (xtags == null) {
            return null
        }
        var atype: String? = null
        for (param: String in xtags.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
            val kv: Array<String> = param.split("=".toRegex(), limit = 2).toTypedArray()
            if (kv.size > 1 && (kv.get(0) == "acont")) {
                atype = kv.get(1)
                break
            }
        }
        if (atype == null) {
            return null
        }
        when (atype) {
            "original" -> return AudioTrackType.ORIGINAL
            "dubbed" -> return AudioTrackType.DUBBED
            "descriptive" -> return AudioTrackType.DESCRIPTIVE
            else -> return null
        }
    }
}
