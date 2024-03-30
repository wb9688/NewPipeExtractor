/*
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeStreamInfoItemExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.Utils
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.function.Function
import java.util.function.Predicate
import java.util.regex.Pattern

open class YoutubeStreamInfoItemExtractor
/**
 * Creates an extractor of StreamInfoItems from a YouTube page.
 *
 * @param videoInfoItem The JSON page element
 * @param timeAgoParser A parser of the textual dates or `null`.
 */(private val videoInfo: JsonObject,
    private val timeAgoParser: TimeAgoParser?) : StreamInfoItemExtractor {
    private var cachedStreamType: StreamType? = null
    private var isPremiere: Boolean? = null
        private get() {
            if (field == null) {
                field = videoInfo.has("upcomingEventData")
            }
            return field
        }

    override val streamType: StreamType
        get() {
            if (cachedStreamType != null) {
                return cachedStreamType
            }
            val badges: JsonArray = videoInfo.getArray("badges")
            for (badge: Any? in badges) {
                if (!(badge is JsonObject)) {
                    continue
                }
                val badgeRenderer: JsonObject = badge.getObject("metadataBadgeRenderer")
                if (((badgeRenderer.getString("style", "") == "BADGE_STYLE_TYPE_LIVE_NOW") || (badgeRenderer.getString("label", "") == "LIVE NOW"))) {
                    cachedStreamType = StreamType.LIVE_STREAM
                    return cachedStreamType!!
                }
            }
            for (overlay: Any? in videoInfo.getArray("thumbnailOverlays")) {
                if (!(overlay is JsonObject)) {
                    continue
                }
                val style: String = overlay
                        .getObject("thumbnailOverlayTimeStatusRenderer")
                        .getString("style", "")
                if (style.equals("LIVE", ignoreCase = true)) {
                    cachedStreamType = StreamType.LIVE_STREAM
                    return cachedStreamType!!
                }
            }
            cachedStreamType = StreamType.VIDEO_STREAM
            return cachedStreamType!!
        }

    @get:Throws(ParsingException::class)
    override val isAd: Boolean
        get() {
            return (isPremium || (name == "[Private video]") || (name == "[Deleted video]"))
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            try {
                val videoId: String = videoInfo.getString("videoId")
                return YoutubeStreamLinkHandlerFactory.Companion.getInstance().getUrl(videoId)
            } catch (e: Exception) {
                throw ParsingException("Could not get url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("title"))
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            throw ParsingException("Could not get name")
        }

    @get:Throws(ParsingException::class)
    override val duration: Long
        get() {
            if (streamType == StreamType.LIVE_STREAM) {
                return -1
            }
            var duration: String? = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("lengthText"))
            if (Utils.isNullOrEmpty(duration)) {
                // Available in playlists for videos
                duration = videoInfo.getString("lengthSeconds")
                if (Utils.isNullOrEmpty(duration)) {
                    val timeOverlay: JsonObject? = videoInfo.getArray("thumbnailOverlays")
                            .stream()
                            .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                            .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                            .filter(Predicate({ thumbnailOverlay: JsonObject? -> thumbnailOverlay!!.has("thumbnailOverlayTimeStatusRenderer") }))
                            .findFirst()
                            .orElse(null)
                    if (timeOverlay != null) {
                        duration = YoutubeParsingHelper.getTextFromObject(
                                timeOverlay.getObject("thumbnailOverlayTimeStatusRenderer")
                                        .getObject("text"))
                    }
                }
                if (Utils.isNullOrEmpty(duration)) {
                    if ((isPremiere)!!) {
                        // Premieres can be livestreams, so the duration is not available in this
                        // case
                        return -1
                    }
                    throw ParsingException("Could not get duration")
                }
            }
            return YoutubeParsingHelper.parseDurationString(duration).toLong()
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            var name: String? = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("longBylineText"))
            if (Utils.isNullOrEmpty(name)) {
                name = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("ownerText"))
                if (Utils.isNullOrEmpty(name)) {
                    name = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("shortBylineText"))
                    if (Utils.isNullOrEmpty(name)) {
                        throw ParsingException("Could not get uploader name")
                    }
                }
            }
            return name
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            var url: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText")
                    .getArray("runs").getObject(0).getObject("navigationEndpoint"))
            if (Utils.isNullOrEmpty(url)) {
                url = YoutubeParsingHelper.getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText")
                        .getArray("runs").getObject(0).getObject("navigationEndpoint"))
                if (Utils.isNullOrEmpty(url)) {
                    url = YoutubeParsingHelper.getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText")
                            .getArray("runs").getObject(0).getObject("navigationEndpoint"))
                    if (Utils.isNullOrEmpty(url)) {
                        throw ParsingException("Could not get uploader url")
                    }
                }
            }
            return url
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            if (videoInfo.has("channelThumbnailSupportedRenderers")) {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(JsonUtils.getArray(videoInfo,  // CHECKSTYLE:OFF
                        "channelThumbnailSupportedRenderers.channelThumbnailWithLinkRenderer.thumbnail.thumbnails"))
                // CHECKSTYLE:ON
            }
            if (videoInfo.has("channelThumbnail")) {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(
                        JsonUtils.getArray(videoInfo, "channelThumbnail.thumbnails"))
            }
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return YoutubeParsingHelper.isVerified(videoInfo.getArray("ownerBadges"))
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            if ((streamType == StreamType.LIVE_STREAM)) {
                return null
            }
            if ((isPremiere)!!) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(dateFromPremiere)
            }
            var publishedTimeText: String? = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("publishedTimeText"))
            if (Utils.isNullOrEmpty(publishedTimeText) && videoInfo.has("videoInfo")) {
                /*
            Returned in playlists, in the form: view count separator upload date
            */
                publishedTimeText = videoInfo.getObject("videoInfo")
                        .getArray("runs")
                        .getObject(2)
                        .getString("text")
            }
            return if (Utils.isNullOrEmpty(publishedTimeText)) null else publishedTimeText
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            if ((streamType == StreamType.LIVE_STREAM)) {
                return null
            }
            if ((isPremiere)!!) {
                return DateWrapper(dateFromPremiere)
            }
            val textualUploadDate: String? = textualUploadDate
            if (timeAgoParser != null && !Utils.isNullOrEmpty(textualUploadDate)) {
                try {
                    return timeAgoParser.parse(textualUploadDate)
                } catch (e: ParsingException) {
                    throw ParsingException("Could not get upload date", e)
                }
            }
            return null
        }

    @get:Throws(ParsingException::class)
    override val viewCount: Long
        get() {
            if (isPremium || (isPremiere)!!) {
                return -1
            }

            // Ignore all exceptions, as the view count can be hidden by creators, and so cannot be
            // found in this case
            val viewCountText: String? = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("viewCountText"))
            if (!Utils.isNullOrEmpty(viewCountText)) {
                try {
                    return getViewCountFromViewCountText(viewCountText, false)
                } catch (ignored: Exception) {
                }
            }

            // Try parsing the real view count from accessibility data, if that's not a running
            // livestream (the view count is returned and not the count of people watching currently
            // the livestream)
            if (streamType != StreamType.LIVE_STREAM) {
                try {
                    return viewCountFromAccessibilityData
                } catch (ignored: Exception) {
                }
            }

            // Fallback to a short view count, always used for livestreams (see why above)
            if (videoInfo.has("videoInfo")) {
                // Returned in playlists, in the form: view count separator upload date
                try {
                    return getViewCountFromViewCountText(videoInfo.getObject("videoInfo")
                            .getArray("runs")
                            .getObject(0)
                            .getString("text", ""), true)
                } catch (ignored: Exception) {
                }
            }
            if (videoInfo.has("shortViewCountText")) {
                // Returned everywhere but in playlists, used by the website to show view counts
                try {
                    val shortViewCountText: String? = YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("shortViewCountText"))
                    if (!Utils.isNullOrEmpty(shortViewCountText)) {
                        return getViewCountFromViewCountText(shortViewCountText, true)
                    }
                } catch (ignored: Exception) {
                }
            }

            // No view count extracted: return -1, as the view count can be hidden by creators on videos
            return -1
        }

    @Throws(NumberFormatException::class, ParsingException::class)
    private fun getViewCountFromViewCountText(@Nonnull viewCountText: String?,
                                              isMixedNumber: Boolean): Long {
        // These approaches are language dependent
        if (viewCountText!!.lowercase(Locale.getDefault()).contains(NO_VIEWS_LOWERCASE)) {
            return 0
        } else if (viewCountText.lowercase(Locale.getDefault()).contains("recommended")) {
            return -1
        }
        return if (isMixedNumber) Utils.mixedNumberWordToLong(viewCountText) else Utils.removeNonDigitCharacters(viewCountText).toLong()
    }

    @get:Throws(NumberFormatException::class, RegexException::class)
    private val viewCountFromAccessibilityData: Long
        private get() {
            // These approaches are language dependent
            val videoInfoTitleAccessibilityData: String = videoInfo.getObject("title")
                    .getObject("accessibility")
                    .getObject("accessibilityData")
                    .getString("label", "")
            if (videoInfoTitleAccessibilityData.lowercase(Locale.getDefault()).endsWith(NO_VIEWS_LOWERCASE)) {
                return 0
            }
            return Utils.removeNonDigitCharacters(
                    Parser.matchGroup1(ACCESSIBILITY_DATA_VIEW_COUNT_REGEX,
                            videoInfoTitleAccessibilityData)).toLong()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return YoutubeParsingHelper.getThumbnailsFromInfoItem(videoInfo)
        }
    private val isPremium: Boolean
        private get() {
            val badges: JsonArray = videoInfo.getArray("badges")
            for (badge: Any in badges) {
                if (((badge as JsonObject).getObject("metadataBadgeRenderer")
                                .getString("label", "") == "Premium")) {
                    return true
                }
            }
            return false
        }

    @get:Throws(ParsingException::class)
    private val dateFromPremiere: OffsetDateTime
        private get() {
            val upcomingEventData: JsonObject = videoInfo.getObject("upcomingEventData")
            val startTime: String = upcomingEventData.getString("startTime")
            try {
                return OffsetDateTime.ofInstant(Instant.ofEpochSecond(startTime.toLong()),
                        ZoneOffset.UTC)
            } catch (e: Exception) {
                throw ParsingException("Could not parse date from premiere: \"" + startTime + "\"")
            }
        }

    @get:Throws(ParsingException::class)
    override val shortDescription: String?
        get() {
            if (videoInfo.has("detailedMetadataSnippets")) {
                return YoutubeParsingHelper.getTextFromObject(videoInfo.getArray("detailedMetadataSnippets")
                        .getObject(0)
                        .getObject("snippetText"))
            }
            if (videoInfo.has("descriptionSnippet")) {
                return YoutubeParsingHelper.getTextFromObject(videoInfo.getObject("descriptionSnippet"))
            }
            return null
        }

    @get:Throws(ParsingException::class)
    override val isShortFormContent: Boolean
        get() {
            try {
                val webPageType: String = videoInfo.getObject("navigationEndpoint")
                        .getObject("commandMetadata").getObject("webCommandMetadata")
                        .getString("webPageType")
                var isShort: Boolean = (!Utils.isNullOrEmpty(webPageType)
                        && (webPageType == "WEB_PAGE_TYPE_SHORTS"))
                if (!isShort) {
                    isShort = videoInfo.getObject("navigationEndpoint").has("reelWatchEndpoint")
                }
                if (!isShort) {
                    val thumbnailTimeOverlay: JsonObject? = videoInfo.getArray("thumbnailOverlays")
                            .stream()
                            .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                            .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                            .filter(Predicate({ thumbnailOverlay: JsonObject ->
                                thumbnailOverlay.has(
                                        "thumbnailOverlayTimeStatusRenderer")
                            }))
                            .map(Function({ thumbnailOverlay: JsonObject ->
                                thumbnailOverlay.getObject(
                                        "thumbnailOverlayTimeStatusRenderer")
                            }))
                            .findFirst()
                            .orElse(null)
                    if (!Utils.isNullOrEmpty(thumbnailTimeOverlay)) {
                        isShort = (thumbnailTimeOverlay!!.getString("style", "")
                                .equals("SHORTS", ignoreCase = true)
                                || thumbnailTimeOverlay.getObject("icon")
                                .getString("iconType", "")
                                .lowercase(Locale.getDefault())
                                .contains("shorts"))
                    }
                }
                return isShort
            } catch (e: Exception) {
                throw ParsingException("Could not determine if this is short-form content", e)
            }
        }

    companion object {
        private val ACCESSIBILITY_DATA_VIEW_COUNT_REGEX: Pattern = Pattern.compile("([\\d,]+) views$")
        private val NO_VIEWS_LOWERCASE: String = "no views"
    }
}
