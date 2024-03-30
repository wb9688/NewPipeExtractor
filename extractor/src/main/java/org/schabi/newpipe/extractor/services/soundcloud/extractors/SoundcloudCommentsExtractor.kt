package org.schabi.newpipe.extractor.services.soundcloud.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class SoundcloudCommentsExtractor(service: StreamingService,
                                  uiHandler: ListLinkHandler?) : CommentsExtractor(service, uiHandler) {
    @get:Throws(ExtractionException::class, IOException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            return getPage(getUrl())
        }

    @Throws(ExtractionException::class, IOException::class)
    public override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem?>? {
        if (page == null || Utils.isNullOrEmpty(page.getUrl())) {
            throw IllegalArgumentException("Page doesn't contain an URL")
        }
        return getPage(page.getUrl())
    }

    @Nonnull
    @Throws(ParsingException::class, IOException::class, ReCaptchaException::class)
    private fun getPage(url: String?): InfoItemsPage<CommentsInfoItem?> {
        val downloader: Downloader? = NewPipe.getDownloader()
        val response: Response? = downloader!!.get(url)
        val json: JsonObject
        try {
            json = JsonParser.`object`().from(response!!.responseBody())
        } catch (e: JsonParserException) {
            throw ParsingException("Could not parse json", e)
        }
        val collector: CommentsInfoItemsCollector = CommentsInfoItemsCollector(
                getServiceId())
        collectStreamsFrom(collector, json.getArray("collection"))
        return InfoItemsPage(collector, Page(json.getString("next_href", null)))
    }

    public override fun onFetchPage(downloader: Downloader?) {}
    @Throws(ParsingException::class)
    private fun collectStreamsFrom(collector: CommentsInfoItemsCollector,
                                   entries: JsonArray) {
        val url: String? = getUrl()
        for (comment: Any in entries) {
            collector.commit(SoundcloudCommentsInfoItemExtractor(comment as JsonObject, url))
        }
    }
}
