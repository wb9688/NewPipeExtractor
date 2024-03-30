package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.utils.JsonUtils
import java.nio.charset.StandardCharsets
import java.util.Objects

class PeertubeCommentsInfoItemExtractor(private val item: JsonObject,
                                        private val children: JsonArray?,
                                        private override val url: String?,
                                        private val baseUrl: String?,
                                        private val isReply: Boolean) : CommentsInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override var replyCount: Int? = null
        get() {
            if (field == null) {
                if (children != null && !children.isEmpty()) {
                    // The totalReplies field is inaccurate for nested replies and sometimes returns 0
                    // although there are replies to that reply stored in children.
                    field = children.size
                } else {
                    field = JsonUtils.getNumber(item, "totalReplies").toInt()
                }
            }
            return field
        }
        private set

    @Throws(ParsingException::class)
    public override fun getUrl(): String? {
        return url + "/" + commentId
    }

    override val thumbnails: List<Image?>?
        get() {
            return uploaderAvatars
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return JsonUtils.getString(item, "account.displayName")
        }

    @get:Throws(ParsingException::class)
    override val textualUploadDate: String?
        get() {
            return JsonUtils.getString(item, "createdAt")
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            val textualUploadDate: String? = textualUploadDate
            return DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate))
        }

    @get:Throws(ParsingException::class)
    override val commentText: Description
        get() {
            val htmlText: String? = JsonUtils.getString(item, "text")
            try {
                val doc: Document = Jsoup.parse((htmlText)!!)
                val text: String = doc.body().text()
                return Description(text, Description.Companion.PLAIN_TEXT)
            } catch (e: Exception) {
                val text: String = htmlText!!.replace("(?s)<[^>]*>(\\s*<[^>]*>)*".toRegex(), "")
                return Description(text, Description.Companion.PLAIN_TEXT)
            }
        }
    override val commentId: String?
        get() {
            return Objects.toString(item.getLong("id"), null)
        }

    override val uploaderAvatars: List<Image?>?
        get() {
            return PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, item.getObject("account"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return (JsonUtils.getString(item, "account.name") + "@"
                    + JsonUtils.getString(item, "account.host"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            val name: String? = JsonUtils.getString(item, "account.name")
            val host: String? = JsonUtils.getString(item, "account.host")
            return ServiceList.PeerTube.getChannelLHFactory()
                    .fromId("accounts/" + name + "@" + host, baseUrl).getUrl()
        }

    @get:Throws(ParsingException::class)
    override val replies: Page?
        get() {
            if (replyCount == 0) {
                return null
            }
            val threadId: String = JsonUtils.getNumber(item, "threadId").toString()
            val repliesUrl: String = url + "/" + threadId
            if (isReply && (children != null) && !children.isEmpty()) {
                // Nested replies are already included in the original thread's request.
                // Wrap the replies into a JsonObject, because the original thread's request body
                // is also structured like a JsonObject.
                val pageContent: JsonObject = JsonObject()
                pageContent.put(PeertubeCommentsExtractor.Companion.CHILDREN, children)
                return Page(repliesUrl, threadId,
                        JsonWriter.string(pageContent).toByteArray(StandardCharsets.UTF_8))
            }
            return Page(repliesUrl, threadId)
        }

    public override fun hasCreatorReply(): Boolean {
        return (item.has("totalRepliesFromVideoAuthor")
                && item.getInt("totalRepliesFromVideoAuthor") > 0)
    }
}
