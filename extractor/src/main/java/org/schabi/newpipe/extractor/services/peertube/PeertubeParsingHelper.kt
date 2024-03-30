package org.schabi.newpipe.extractor.services.peertube

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeChannelInfoItemExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubePlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeSepiaStreamInfoItemExtractor
import org.schabi.newpipe.extractor.services.peertube.extractors.PeertubeStreamInfoItemExtractor
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.Utils
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.Collections
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

object PeertubeParsingHelper {
    val START_KEY: String = "start"
    val COUNT_KEY: String = "count"
    val ITEMS_PER_PAGE: Int = 12
    val START_PATTERN: String = "start=(\\d*)"
    @Throws(ContentNotAvailableException::class)
    fun validate(json: JsonObject?) {
        val error: String = json!!.getString("error")
        if (!Utils.isBlank(error)) {
            throw ContentNotAvailableException(error)
        }
    }

    @Throws(ParsingException::class)
    fun parseDateFrom(textualUploadDate: String?): OffsetDateTime {
        try {
            return OffsetDateTime.ofInstant(Instant.parse(textualUploadDate), ZoneOffset.UTC)
        } catch (e: DateTimeParseException) {
            throw ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e)
        }
    }

    fun getNextPage(prevPageUrl: String?, total: Long): Page? {
        val prevStart: String?
        try {
            prevStart = Parser.matchGroup1(START_PATTERN, prevPageUrl)
        } catch (e: RegexException) {
            return null
        }
        if (Utils.isBlank(prevStart)) {
            return null
        }
        val nextStart: Long
        try {
            nextStart = prevStart.toLong() + ITEMS_PER_PAGE
        } catch (e: NumberFormatException) {
            return null
        }
        if (nextStart >= total) {
            return null
        } else {
            return Page(prevPageUrl!!.replace(
                    START_KEY + "=" + prevStart, START_KEY + "=" + nextStart))
        }
    }

    /**
     * Collect items from the given JSON object with the given collector.
     *
     *
     *
     * Supported info item types are streams with their Sepia variant, channels and playlists.
     *
     *
     * @param collector the collector used to collect information
     * @param json      the JSOn response to retrieve data from
     * @param baseUrl   the base URL of the instance
     * @param sepia     if we should use `PeertubeSepiaStreamInfoItemExtractor` to extract
     * streams or `PeertubeStreamInfoItemExtractor` otherwise
     */
    @JvmOverloads
    @Throws(ParsingException::class)
    fun collectItemsFrom(collector: InfoItemsCollector<*, *>,
                         json: JsonObject?,
                         baseUrl: String?,
                         sepia: Boolean = false) {
        val contents: JsonArray?
        try {
            contents = JsonUtils.getValue(json, "data") as JsonArray?
        } catch (e: Exception) {
            throw ParsingException("Unable to extract list info", e)
        }
        for (c: Any? in contents!!) {
            if (c is JsonObject) {
                var item: JsonObject = c

                // PeerTube playlists have the stream info encapsulated in an "video" object
                if (item.has("video")) {
                    item = item.getObject("video")
                }
                val isPlaylistInfoItem: Boolean = item.has("videosLength")
                val isChannelInfoItem: Boolean = item.has("followersCount")
                val extractor: InfoItemExtractor
                if (sepia) {
                    extractor = PeertubeSepiaStreamInfoItemExtractor(item, baseUrl)
                } else if (isPlaylistInfoItem) {
                    extractor = PeertubePlaylistInfoItemExtractor(item, baseUrl)
                } else if (isChannelInfoItem) {
                    extractor = PeertubeChannelInfoItemExtractor(item, baseUrl)
                } else {
                    extractor = PeertubeStreamInfoItemExtractor(item, baseUrl)
                }
                collector.commit(extractor)
            }
        }
    }

    /**
     * Get avatars from a `ownerAccount` or a `videoChannel` [JsonObject].
     *
     *
     *
     * If the `avatars` [JsonArray] is present and non null or empty, avatars will be
     * extracted from this array using [.getImagesFromAvatarOrBannerArray].
     *
     *
     *
     *
     * If that's not the case, an avatar will extracted using the `avatar` [JsonObject].
     *
     *
     *
     *
     * Note that only images for which paths are not null and not empty will be added to the
     * unmodifiable [Image] list returned.
     *
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the `ownerAccount` or `videoChannel`
     * [JsonObject]
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */
    @Nonnull
    fun getAvatarsFromOwnerAccountOrVideoChannelObject(
            @Nonnull baseUrl: String?,
            @Nonnull ownerAccountOrVideoChannelObject: JsonObject?): List<Image> {
        return getImagesFromAvatarsOrBanners(baseUrl, ownerAccountOrVideoChannelObject,
                "avatars", "avatar")
    }

    /**
     * Get banners from a `ownerAccount` or a `videoChannel` [JsonObject].
     *
     *
     *
     * If the `banners` [JsonArray] is present and non null or empty, banners will be
     * extracted from this array using [.getImagesFromAvatarOrBannerArray].
     *
     *
     *
     *
     * If that's not the case, a banner will extracted using the `banner` [JsonObject].
     *
     *
     *
     *
     * Note that only images for which paths are not null and not empty will be added to the
     * unmodifiable [Image] list returned.
     *
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the `ownerAccount` or `videoChannel`
     * [JsonObject]
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */
    @Nonnull
    fun getBannersFromAccountOrVideoChannelObject(
            @Nonnull baseUrl: String?,
            @Nonnull ownerAccountOrVideoChannelObject: JsonObject?): List<Image> {
        return getImagesFromAvatarsOrBanners(baseUrl, ownerAccountOrVideoChannelObject,
                "banners", "banner")
    }

    /**
     * Get thumbnails from a playlist or a video item [JsonObject].
     *
     *
     *
     * PeerTube provides two thumbnails in its API: a low one, represented by the value of the
     * `thumbnailPath` key, and a medium one, represented by the value of the
     * `previewPath` key.
     *
     *
     *
     *
     * If a value is not null or empty, an [Image] will be added to the list returned with
     * the URL to the thumbnail (`baseUrl + value`), a height and a width unknown and the
     * corresponding resolution level (see above).
     *
     *
     * @param baseUrl                   the base URL of the PeerTube instance
     * @param playlistOrVideoItemObject the playlist or the video item [JsonObject], which
     * must not be null
     * @return an unmodifiable list of [Image]s, which is never null but can be empty
     */
    @Nonnull
    fun getThumbnailsFromPlaylistOrVideoItem(
            @Nonnull baseUrl: String?,
            @Nonnull playlistOrVideoItemObject: JsonObject?): List<Image> {
        val imageList: MutableList<Image> = ArrayList(2)
        val thumbnailPath: String = playlistOrVideoItemObject!!.getString("thumbnailPath")
        if (!Utils.isNullOrEmpty(thumbnailPath)) {
            imageList.add(Image(baseUrl + thumbnailPath, Image.Companion.HEIGHT_UNKNOWN, Image.Companion.WIDTH_UNKNOWN,
                    ResolutionLevel.LOW))
        }
        val previewPath: String = playlistOrVideoItemObject.getString("previewPath")
        if (!Utils.isNullOrEmpty(previewPath)) {
            imageList.add(Image(baseUrl + previewPath, Image.Companion.HEIGHT_UNKNOWN, Image.Companion.WIDTH_UNKNOWN,
                    ResolutionLevel.MEDIUM))
        }
        return Collections.unmodifiableList(imageList)
    }

    /**
     * Utility method to get avatars and banners from video channels and accounts from given name
     * keys.
     *
     *
     *
     * Only images for which paths are not null and not empty will be added to the unmodifiable
     * [Image] list returned and only the width of avatars or banners is provided by the API,
     * and so is the only image dimension known.
     *
     *
     * @param baseUrl                          the base URL of the PeerTube instance
     * @param ownerAccountOrVideoChannelObject the `ownerAccount` or `videoChannel`
     * [JsonObject]
     * @param jsonArrayName                    the key name of the [JsonArray] to which
     * extract all images available (`avatars` or
     * `banners`)
     * @param jsonObjectName                   the key name of the [JsonObject] to which
     * extract a single image (`avatar` or
     * `banner`), used as a fallback if the images
     * [JsonArray] is null or empty
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */
    @Nonnull
    private fun getImagesFromAvatarsOrBanners(
            @Nonnull baseUrl: String?,
            @Nonnull ownerAccountOrVideoChannelObject: JsonObject?,
            @Nonnull jsonArrayName: String,
            @Nonnull jsonObjectName: String): List<Image> {
        val images: JsonArray = ownerAccountOrVideoChannelObject!!.getArray(jsonArrayName)
        if (!Utils.isNullOrEmpty(images)) {
            return getImagesFromAvatarOrBannerArray(baseUrl, images)
        }
        val image: JsonObject = ownerAccountOrVideoChannelObject.getObject(jsonObjectName)
        val path: String = image.getString("path")
        if (!Utils.isNullOrEmpty(path)) {
            return java.util.List.of<Image>(Image(baseUrl + path, Image.Companion.HEIGHT_UNKNOWN,
                    image.getInt("width", Image.Companion.WIDTH_UNKNOWN), ResolutionLevel.UNKNOWN))
        }
        return listOf()
    }

    /**
     * Get [Image]s from an `avatars` or a `banners` [JsonArray].
     *
     *
     *
     * Only images for which paths are not null and not empty will be added to the
     * unmodifiable [Image] list returned.
     *
     *
     *
     *
     * Note that only the width of avatars or banners is provided by the API, and so only is the
     * only dimension known of images.
     *
     *
     * @param baseUrl               the base URL of the PeerTube instance from which the
     * `avatarsOrBannersArray` [JsonArray] comes from
     * @param avatarsOrBannersArray an `avatars` or `banners` [JsonArray]
     * @return an unmodifiable list of [Image]s, which may be empty but never null
     */
    @Nonnull
    private fun getImagesFromAvatarOrBannerArray(
            @Nonnull baseUrl: String?,
            @Nonnull avatarsOrBannersArray: JsonArray): List<Image> {
        return avatarsOrBannersArray.stream()
                .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .filter(Predicate<JsonObject>({ image: JsonObject -> !Utils.isNullOrEmpty(image.getString("path")) }))
                .map<Image>(Function<JsonObject, Image>({ image: JsonObject ->
                    Image(baseUrl + image.getString("path"), Image.Companion.HEIGHT_UNKNOWN,
                            image.getInt("width", Image.Companion.WIDTH_UNKNOWN), ResolutionLevel.UNKNOWN)
                }))
                .collect(Collectors.toUnmodifiableList<Image>())
    }
}
