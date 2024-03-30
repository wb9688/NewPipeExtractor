package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate

class YoutubeCommentsExtractor(
        service: StreamingService,
        uiHandler: ListLinkHandler?) : CommentsExtractor(service, uiHandler) {
    /**
     * Whether comments are disabled on video.
     */
    override var isCommentsDisabled: Boolean = false
        private set
    /**
     * The second ajax **/
    next** response.
    */
    private var ajaxJson: JsonObject? = null

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            if (isCommentsDisabled) {
                return infoItemsPageForDisabledComments
            }
            return extractComments(ajaxJson)
        }

    /**
     * Finds the initial comments token and initializes commentsDisabled.
     * <br></br>
     * Also sets [.commentsDisabled].
     *
     * @return the continuation token or null if none was found
     */
    private fun findInitialCommentsToken(nextResponse: JsonObject?): String? {
        val contents: JsonArray? = getJsonContents(nextResponse)

        // For videos where comments are unavailable, this would be null
        if (contents == null) {
            return null
        }
        val token: String? = contents
                .stream() // Only use JsonObjects
                .filter(Predicate<Any>({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map<JsonObject>(Function<Any, JsonObject>({ obj: Any? -> JsonObject::class.java.cast(obj) })) // Check if the comment-section is present
                .filter(Predicate<JsonObject>({ jObj: JsonObject? ->
                    try {
                        return@filter ("comments-section" ==
                                JsonUtils.getString(jObj, "itemSectionRenderer.targetId"))
                    } catch (ignored: ParsingException) {
                        return@filter false
                    }
                }))
                .findFirst() // Extract the token (or null in case of error)
                .map<String?>(Function<JsonObject, String?>({ itemSectionRenderer: JsonObject ->
                    try {
                        return@map JsonUtils.getString(
                                itemSectionRenderer
                                        .getObject("itemSectionRenderer")
                                        .getArray("contents").getObject(0), (
                                "continuationItemRenderer.continuationEndpoint"
                                        + ".continuationCommand.token"))
                    } catch (ignored: ParsingException) {
                        return@map null
                    }
                }))
                .orElse(null)

        // The comments are disabled if we couldn't get a token
        isCommentsDisabled = token == null
        return token
    }

    private fun getJsonContents(nextResponse: JsonObject?): JsonArray? {
        try {
            return JsonUtils.getArray(nextResponse,
                    "contents.twoColumnWatchNextResults.results.results.contents")
        } catch (e: ParsingException) {
            return null
        }
    }

    private val infoItemsPageForDisabledComments: InfoItemsPage<CommentsInfoItem?>
        private get() {
            return InfoItemsPage(emptyList(), null, emptyList())
        }

    @Throws(ExtractionException::class)
    private fun getNextPage(jsonObject: JsonObject?): Page? {
        val onResponseReceivedEndpoints: JsonArray = jsonObject!!.getArray("onResponseReceivedEndpoints")

        // Prevent ArrayIndexOutOfBoundsException
        if (onResponseReceivedEndpoints.isEmpty()) {
            return null
        }
        val continuationItemsArray: JsonArray
        try {
            val endpoint: JsonObject = onResponseReceivedEndpoints
                    .getObject(onResponseReceivedEndpoints.size - 1)
            continuationItemsArray = endpoint
                    .getObject("reloadContinuationItemsCommand",
                            endpoint.getObject("appendContinuationItemsAction"))
                    .getArray("continuationItems")
        } catch (e: Exception) {
            return null
        }
        // Prevent ArrayIndexOutOfBoundsException
        if (continuationItemsArray.isEmpty()) {
            return null
        }
        val continuationItemRenderer: JsonObject = continuationItemsArray
                .getObject(continuationItemsArray.size - 1)
                .getObject("continuationItemRenderer")
        val jsonPath: String = if (continuationItemRenderer.has("button")) "button.buttonRenderer.command.continuationCommand.token" else "continuationEndpoint.continuationCommand.token"
        val continuation: String?
        try {
            continuation = JsonUtils.getString(continuationItemRenderer, jsonPath)
        } catch (e: Exception) {
            return null
        }
        return getNextPage(continuation)
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun getNextPage(continuation: String?): Page {
        return Page(getUrl(), continuation) // URL is ignored tho
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem?>? {
        if (isCommentsDisabled) {
            return infoItemsPageForDisabledComments
        }
        if (page == null || Utils.isNullOrEmpty(page.getId())) {
            throw IllegalArgumentException("Page doesn't have the continuation.")
        }
        val localization: Localization? = getExtractorLocalization()
        // @formatter:off
         val body: ByteArray = JsonWriter.string(
        YoutubeParsingHelper.prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
        .value("continuation", page.getId())
        .done())
        .toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val jsonObject: JsonObject? = YoutubeParsingHelper.getJsonPostResponse("next", body, localization)
        return extractComments(jsonObject)
    }

    @Throws(ExtractionException::class)
    private fun extractComments(jsonObject: JsonObject?): InfoItemsPage<CommentsInfoItem?> {
        val collector: CommentsInfoItemsCollector = CommentsInfoItemsCollector(
                getServiceId())
        collectCommentsFrom(collector, jsonObject)
        return InfoItemsPage(collector, getNextPage(jsonObject))
    }

    @Throws(ParsingException::class)
    private fun collectCommentsFrom(collector: CommentsInfoItemsCollector,
                                    jsonObject: JsonObject?) {
        val onResponseReceivedEndpoints: JsonArray = jsonObject!!.getArray("onResponseReceivedEndpoints")
        // Prevent ArrayIndexOutOfBoundsException
        if (onResponseReceivedEndpoints.isEmpty()) {
            return
        }
        val commentsEndpoint: JsonObject = onResponseReceivedEndpoints.getObject(onResponseReceivedEndpoints.size - 1)
        val path: String
        if (commentsEndpoint.has("reloadContinuationItemsCommand")) {
            path = "reloadContinuationItemsCommand.continuationItems"
        } else if (commentsEndpoint.has("appendContinuationItemsAction")) {
            path = "appendContinuationItemsAction.continuationItems"
        } else {
            // No comments
            return
        }
        val contents: JsonArray
        try {
            contents = JsonArray(JsonUtils.getArray(commentsEndpoint, path))
        } catch (e: Exception) {
            // No comments
            return
        }
        val index: Int = contents.size - 1
        if (!contents.isEmpty() && contents.getObject(index).has("continuationItemRenderer")) {
            contents.removeAt(index)
        }
        val jsonKey: String = if (contents.getObject(0).has("commentThreadRenderer")) "commentThreadRenderer" else "commentRenderer"
        val comments: List<Any?>?
        try {
            comments = JsonUtils.getValues(contents, jsonKey)
        } catch (e: Exception) {
            throw ParsingException("Unable to get parse youtube comments", e)
        }
        val url: String? = getUrl()
        comments.stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map(Function({ jObj: JsonObject -> YoutubeCommentsInfoItemExtractor(jObj, url, getTimeAgoParser()) }))
                .forEach(Consumer({ extractor: YoutubeCommentsInfoItemExtractor -> collector.commit(extractor) }))
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        val localization: Localization? = getExtractorLocalization()
        // @formatter:off
         val body: ByteArray = JsonWriter.string(
        YoutubeParsingHelper.prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
        .value("videoId", getId())
        .done())
        .toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        val initialToken: String? = findInitialCommentsToken(YoutubeParsingHelper.getJsonPostResponse("next", body, localization))
        if (initialToken == null) {
            return
        }

        // @formatter:off
         val ajaxBody: ByteArray = JsonWriter.string(
        YoutubeParsingHelper.prepareDesktopJsonBuilder(localization, getExtractorContentCountry())
        .value("continuation", initialToken)
        .done())
        .toByteArray(StandardCharsets.UTF_8)
                // @formatter:on
        ajaxJson = YoutubeParsingHelper.getJsonPostResponse("next", ajaxBody, localization)
    }

    @get:Throws(ExtractionException::class)
    override val commentsCount: Int
        get() {
            assertPageFetched()
            if (isCommentsDisabled) {
                return -1
            }
            val countText: JsonObject = ajaxJson
                    .getArray("onResponseReceivedEndpoints").getObject(0)
                    .getObject("reloadContinuationItemsCommand")
                    .getArray("continuationItems").getObject(0)
                    .getObject("commentsHeaderRenderer")
                    .getObject("countText")
            try {
                return
                Utils.removeNonDigitCharacters(YoutubeParsingHelper.getTextFromObject(countText))
                        .toInt()
            } catch (e: Exception) {
                throw ExtractionException("Unable to get comments count", e)
            }
        }
}
