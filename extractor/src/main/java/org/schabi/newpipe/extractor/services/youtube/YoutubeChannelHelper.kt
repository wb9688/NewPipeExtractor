package org.schabi.newpipe.extractor.services.youtube

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.youtube.YoutubeChannelHelper.ChannelHeader.HeaderType
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Optional
import java.util.function.Function
import java.util.function.Predicate

/**
 * Shared functions for extracting YouTube channel pages and tabs.
 */
object YoutubeChannelHelper {
    /**
     * Take a YouTube channel ID or URL path, resolve it if necessary and return a channel ID.
     *
     * @param idOrPath a YouTube channel ID or URL path
     * @return a YouTube channel ID
     * @throws IOException if a channel resolve request failed
     * @throws ExtractionException if a channel resolve request response could not be parsed or is
     * invalid
     */
    @Nonnull
    @Throws(ExtractionException::class, IOException::class)
    fun resolveChannelId(@Nonnull idOrPath: String?): String {
        val channelId: Array<String> = idOrPath!!.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        if (channelId.get(0).startsWith("UC")) {
            return channelId.get(0)
        }

        // If the URL is not a /channel URL, we need to use the navigation/resolve_url endpoint of
        // the InnerTube API to get the channel id. If this fails or if the URL is not a /channel
        // URL, then no information about the channel associated with this URL was found,
        // so the unresolved url will be returned.
        if (!(channelId.get(0) == "channel")) {
            val body: ByteArray = JsonWriter.string(
                    YoutubeParsingHelper.prepareDesktopJsonBuilder(Localization.Companion.DEFAULT, ContentCountry.Companion.DEFAULT)
                            .value("url", "https://www.youtube.com/" + idOrPath)
                            .done())
                    .toByteArray(StandardCharsets.UTF_8)
            val jsonResponse: JsonObject? = YoutubeParsingHelper.getJsonPostResponse(
                    "navigation/resolve_url", body, Localization.Companion.DEFAULT)
            checkIfChannelResponseIsValid(jsonResponse)
            val endpoint: JsonObject = jsonResponse!!.getObject("endpoint")
            val webPageType: String = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "")
            val browseEndpoint: JsonObject = endpoint.getObject("browseEndpoint")
            val browseId: String = browseEndpoint.getString("browseId", "")
            if ((webPageType.equals("WEB_PAGE_TYPE_BROWSE", ignoreCase = true)
                            || webPageType.equals("WEB_PAGE_TYPE_CHANNEL", ignoreCase = true)
                            && !browseId.isEmpty())) {
                if (!browseId.startsWith("UC")) {
                    throw ExtractionException("Redirected id is not pointing to a channel")
                }
                return browseId
            }
        }

        // return the unresolved URL
        return channelId.get(1)
    }

    /**
     * Fetch a YouTube channel tab response, using the given channel ID and tab parameters.
     *
     *
     *
     * Redirections to other channels are supported to up to 3 redirects, which could happen for
     * instance for localized channels or for auto-generated ones. For instance, there are three IDs
     * of the auto-generated "Movies and Shows" channel, i.e. `UCuJcl0Ju-gPDoksRjK1ya-w`,
     * `UChBfWrfBXL9wS6tQtgjt_OQ` and `UCok7UTQQEP1Rsctxiv3gwSQ`, and they all redirect
     * to the `UClgRkhTL3_hImCAmdLfDE4g` one.
     *
     *
     * @param channelId    a valid YouTube channel ID
     * @param parameters   the parameters to specify the YouTube channel tab; if invalid ones are
     * specified, YouTube should return the `Home` tab
     * @param localization the [Localization] to use
     * @param country      the [ContentCountry] to use
     * @return a [channel response data][ChannelResponseData]
     * @throws IOException if a channel request failed
     * @throws ExtractionException if a channel request response could not be parsed or is invalid
     */
    @Nonnull
    @Throws(ExtractionException::class, IOException::class)
    fun getChannelResponse(@Nonnull channelId: String?,
                           @Nonnull parameters: String?,
                           @Nonnull localization: Localization?,
                           @Nonnull country: ContentCountry?): ChannelResponseData {
        var id: String? = channelId
        var ajaxJson: JsonObject? = null
        var level: Int = 0
        while (level < 3) {
            val body: ByteArray = JsonWriter.string(YoutubeParsingHelper.prepareDesktopJsonBuilder(
                    localization, country)
                    .value("browseId", id)
                    .value("params", parameters)
                    .done())
                    .toByteArray(StandardCharsets.UTF_8)
            val jsonResponse: JsonObject? = YoutubeParsingHelper.getJsonPostResponse(
                    "browse", body, localization)
            checkIfChannelResponseIsValid(jsonResponse)
            val endpoint: JsonObject = jsonResponse!!.getArray("onResponseReceivedActions")
                    .getObject(0)
                    .getObject("navigateAction")
                    .getObject("endpoint")
            val webPageType: String = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "")
            val browseId: String = endpoint.getObject("browseEndpoint")
                    .getString("browseId", "")
            if ((webPageType.equals("WEB_PAGE_TYPE_BROWSE", ignoreCase = true)
                            || webPageType.equals("WEB_PAGE_TYPE_CHANNEL", ignoreCase = true)
                            && !browseId.isEmpty())) {
                if (!browseId.startsWith("UC")) {
                    throw ExtractionException("Redirected id is not pointing to a channel")
                }
                id = browseId
                level++
            } else {
                ajaxJson = jsonResponse
                break
            }
        }
        if (ajaxJson == null) {
            throw ExtractionException("Got no channel response after 3 redirects")
        }
        YoutubeParsingHelper.defaultAlertsCheck(ajaxJson)
        return ChannelResponseData(ajaxJson, id)
    }

    /**
     * Assert that a channel JSON response does not contain an `error` JSON object.
     *
     * @param jsonResponse a channel JSON response
     * @throws ContentNotAvailableException if the channel was not found
     */
    @Throws(ContentNotAvailableException::class)
    private fun checkIfChannelResponseIsValid(@Nonnull jsonResponse: JsonObject?) {
        if (!Utils.isNullOrEmpty(jsonResponse!!.getObject("error"))) {
            val errorJsonObject: JsonObject = jsonResponse.getObject("error")
            val errorCode: Int = errorJsonObject.getInt("code")
            if (errorCode == 404) {
                throw ContentNotAvailableException("This channel doesn't exist.")
            } else {
                throw ContentNotAvailableException(("Got error:\""
                        + errorJsonObject.getString("status") + "\": "
                        + errorJsonObject.getString("message")))
            }
        }
    }

    /**
     * Get a channel header as an [Optional] it if exists.
     *
     * @param channelResponse a full channel JSON response
     * @return an [Optional] containing a [ChannelHeader] or an empty [Optional]
     * if no supported header has been found
     */
    @Nonnull
    fun getChannelHeader(
            @Nonnull channelResponse: JsonObject?): Optional<ChannelHeader?> {
        val header: JsonObject = channelResponse!!.getObject("header")
        if (header.has("c4TabbedHeaderRenderer")) {
            return Optional.of(header.getObject("c4TabbedHeaderRenderer"))
                    .map(Function({ json: JsonObject -> ChannelHeader(json, HeaderType.C4_TABBED) }))
        } else if (header.has("carouselHeaderRenderer")) {
            return header.getObject("carouselHeaderRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                    .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                    .filter(Predicate({ item: JsonObject -> item.has("topicChannelDetailsRenderer") }))
                    .findFirst()
                    .map(Function({ item: JsonObject -> item.getObject("topicChannelDetailsRenderer") }))
                    .map(Function({ json: JsonObject -> ChannelHeader(json, HeaderType.CAROUSEL) }))
        } else if (header.has("pageHeaderRenderer")) {
            return Optional.of(header.getObject("pageHeaderRenderer"))
                    .map(Function({ json: JsonObject -> ChannelHeader(json, HeaderType.PAGE) }))
        } else if (header.has("interactiveTabbedHeaderRenderer")) {
            return Optional.of(header.getObject("interactiveTabbedHeaderRenderer"))
                    .map(Function({ json: JsonObject ->
                        ChannelHeader(json,
                                HeaderType.INTERACTIVE_TABBED)
                    }))
        } else {
            return Optional.empty()
        }
    }

    /**
     * Response data object for [.getChannelResponse], after any redirection in the allowed redirects count (`3`).
     */
    class ChannelResponseData(
            /**
             * The channel response as a JSON object, after all redirects.
             */
            @field:Nonnull @param:Nonnull val jsonResponse: JsonObject,
            /**
             * The channel ID after all redirects.
             */
            @field:Nonnull @param:Nonnull val channelId: String?)

    /**
     * A channel header response.
     *
     *
     *
     * This class allows the distinction between a classic header and a carousel one, used for
     * auto-generated ones like the gaming or music topic channels and for big events such as the
     * Coachella music festival, which have a different data structure and do not return the same
     * properties.
     *
     */
    class ChannelHeader(
            /**
             * The channel header JSON response.
             */
            @field:Nonnull @param:Nonnull val json: JsonObject,
            /**
             * The type of the channel header.
             *
             *
             *
             * See the documentation of the [HeaderType] class for more details.
             *
             */
            val headerType: HeaderType) {
        /**
         * Types of supported YouTube channel headers.
         */
        enum class HeaderType {
            /**
             * A `c4TabbedHeaderRenderer` channel header type.
             *
             *
             *
             * This header is returned on the majority of channels and contains the channel's name,
             * its banner and its avatar and its subscriber count in most cases.
             *
             */
            C4_TABBED,

            /**
             * An `interactiveTabbedHeaderRenderer` channel header type.
             *
             *
             *
             * This header is returned for gaming topic channels, and only contains the channel's
             * name, its banner and a poster as its "avatar".
             *
             */
            INTERACTIVE_TABBED,

            /**
             * A `carouselHeaderRenderer` channel header type.
             *
             *
             *
             * This header returns only the channel's name, its avatar and its subscriber count.
             *
             */
            CAROUSEL,

            /**
             * A `pageHeaderRenderer` channel header type.
             *
             *
             *
             * This header returns only the channel's name and its avatar.
             *
             */
            PAGE
        }
    }
}
