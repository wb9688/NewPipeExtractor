package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.nio.charset.StandardCharsets

class PeertubeCommentsExtractor(service: StreamingService,
                                uiHandler: ListLinkHandler?) : CommentsExtractor(service, uiHandler) {
    /**
     * Use [.isReply] to access this variable.
     */
    private var isReply: Boolean? = null

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Nonnull
    override val initialPage: InfoItemsPage<R?>?
        get() {
            if (isReply()) {
                return getPage(Page(getOriginalUrl()))
            } else {
                return getPage(Page((getUrl() + "?" + PeertubeParsingHelper.START_KEY + "=0&"
                        + PeertubeParsingHelper.COUNT_KEY + "=" + PeertubeParsingHelper.ITEMS_PER_PAGE)))
            }
        }

    @Throws(ParsingException::class)
    private fun isReply(): Boolean {
        if (isReply == null) {
            if (getOriginalUrl().contains("/videos/watch/")) {
                isReply = false
            } else {
                isReply = getOriginalUrl().contains("/comment-threads/")
            }
        }
        return isReply!!
    }

    @Throws(ParsingException::class)
    private fun collectCommentsFrom(collector: CommentsInfoItemsCollector,
                                    json: JsonObject) {
        val contents: JsonArray = json.getArray("data")
        for (c: Any in contents) {
            if (c is JsonObject) {
                val item: JsonObject = c
                if (!item.getBoolean(IS_DELETED)) {
                    collector.commit(PeertubeCommentsInfoItemExtractor(
                            item, null, getUrl(), getBaseUrl(), isReply()))
                }
            }
        }
    }

    @Throws(ParsingException::class)
    private fun collectRepliesFrom(collector: CommentsInfoItemsCollector,
                                   json: JsonObject?) {
        val contents: JsonArray = json!!.getArray(CHILDREN)
        for (c: Any in contents) {
            if (c is JsonObject) {
                val content: JsonObject = c
                val item: JsonObject = content.getObject("comment")
                val children: JsonArray = content.getArray(CHILDREN)
                if (!item.getBoolean(IS_DELETED)) {
                    collector.commit(PeertubeCommentsInfoItemExtractor(
                            item, children, getUrl(), getBaseUrl(), isReply()))
                }
            }
        }
    }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        var json: JsonObject? = null
        val collector: CommentsInfoItemsCollector = CommentsInfoItemsCollector(getServiceId())
        val total: Long
        if (page.getBody() == null) {
            val response: Response? = getDownloader().get(page.getUrl())
            if (response != null && !Utils.isBlank(response.responseBody())) {
                try {
                    json = JsonParser.`object`().from(response.responseBody())
                } catch (e: Exception) {
                    throw ParsingException("Could not parse json data for comments info", e)
                }
            }
            if (json != null) {
                PeertubeParsingHelper.validate(json)
                if (isReply() || json.has(CHILDREN)) {
                    total = json.getArray(CHILDREN).size.toLong()
                    collectRepliesFrom(collector, json)
                } else {
                    total = json.getLong(TOTAL)
                    collectCommentsFrom(collector, json)
                }
            } else {
                throw ExtractionException("Unable to get PeerTube kiosk info")
            }
        } else {
            try {
                json = JsonParser.`object`().from(String(page.getBody(), StandardCharsets.UTF_8))
                isReply = true
                total = json.getArray(CHILDREN).size.toLong()
                collectRepliesFrom(collector, json)
            } catch (e: JsonParserException) {
                throw ParsingException(
                        "Could not parse json data for nested comments  info", e)
            }
        }
        return InfoItemsPage(collector,
                PeertubeParsingHelper.getNextPage(page.getUrl(), total))
    }

    public override fun onFetchPage(downloader: Downloader?) {}

    companion object {
        val CHILDREN: String = "children"
        private val IS_DELETED: String = "isDeleted"
        private val TOTAL: String = "total"
    }
}
