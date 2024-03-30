package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonWriter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.comments.CommentsExtractor
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.utils.JsonUtils
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

class BandcampCommentsExtractor(service: StreamingService,
                                linkHandler: ListLinkHandler?) : CommentsExtractor(service, linkHandler) {
    private var document: Document? = null
    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        document = Jsoup.parse(downloader!!.get(getLinkHandler().getUrl()).responseBody())
    }

    @get:Throws(IOException::class, ExtractionException::class)
    override val initialPage: InfoItemsPage<R?>?
        get() {
            val collector: CommentsInfoItemsCollector = CommentsInfoItemsCollector(getServiceId())
            val collectorsData: JsonObject? = JsonUtils.toJsonObject(
                    document!!.getElementById("collectors-data")!!.attr("data-blob"))
            val reviews: JsonArray = collectorsData!!.getArray("reviews")
            for (review: Any in reviews) {
                collector.commit(
                        BandcampCommentsInfoItemExtractor(review as JsonObject, getUrl()))
            }
            if (!collectorsData.getBoolean("more_reviews_available")) {
                return InfoItemsPage(collector, null)
            }
            val trackId: String = trackId
            val token: String = getNextPageToken(reviews)
            return InfoItemsPage(collector, Page(java.util.List.of(trackId, token)))
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun getPage(page: Page?): InfoItemsPage<CommentsInfoItem?>? {
        val collector: CommentsInfoItemsCollector = CommentsInfoItemsCollector(getServiceId())
        val pageIds: List<String?>? = page.getIds()
        val trackId: String? = pageIds!!.get(0)
        val token: String? = pageIds.get(1)
        val reviewsData: JsonObject? = fetchReviewsData(trackId, token)
        val reviews: JsonArray = reviewsData!!.getArray("results")
        for (review: Any in reviews) {
            collector.commit(
                    BandcampCommentsInfoItemExtractor(review as JsonObject, getUrl()))
        }
        if (!reviewsData.getBoolean("more_available")) {
            return InfoItemsPage(collector, null)
        }
        return InfoItemsPage(collector,
                Page(java.util.List.of(trackId, getNextPageToken(reviews))))
    }

    @Throws(ParsingException::class)
    private fun fetchReviewsData(trackId: String?, token: String?): JsonObject? {
        try {
            return JsonUtils.toJsonObject(getDownloader().postWithContentTypeJson(
                    REVIEWS_API_URL, emptyMap(),
                    JsonWriter.string().`object`()
                            .value("tralbum_type", "t")
                            .value("tralbum_id", trackId)
                            .value("token", token)
                            .value("count", 7)
                            .array("exclude_fan_ids").end()
                            .end().done().toByteArray(StandardCharsets.UTF_8)).responseBody())
        } catch (e: IOException) {
            throw ParsingException("Could not fetch reviews", e)
        } catch (e: ReCaptchaException) {
            throw ParsingException("Could not fetch reviews", e)
        }
    }

    @Throws(ParsingException::class)
    private fun getNextPageToken(reviews: JsonArray): String {
        return reviews.stream()
                .filter(Predicate({ o: Any? -> JsonObject::class.java.isInstance(o) }))
                .map(Function({ obj: Any? -> JsonObject::class.java.cast(obj) }))
                .map(Function({ review: JsonObject -> review.getString("token") }))
                .reduce(BinaryOperator({ a: String?, b: String -> b })) // keep only the last element
                .orElseThrow(Supplier({ ParsingException("Could not get token") }))
    }

    @get:Throws(ParsingException::class)
    private val trackId: String
        private get() {
            val pageProperties: JsonObject? = JsonUtils.toJsonObject(
                    document!!.selectFirst("meta[name=bc-page-properties]")
                            .attr("content"))
            return pageProperties!!.getLong("item_id").toString()
        }

    @get:Throws(ExtractionException::class)
    override val isCommentsDisabled: Boolean
        get() {
            return BandcampExtractorHelper.isRadioUrl(getUrl())
        }

    companion object {
        private val REVIEWS_API_URL: String = BandcampExtractorHelper.BASE_API_URL + "/tralbumcollectors/2/reviews"
    }
}
