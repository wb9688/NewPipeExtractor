package org.schabi.newpipe.extractor.services.youtube.extractors

import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

class YoutubeFeedInfoItemExtractor(private val entryElement: Element) : StreamInfoItemExtractor {
    override val streamType: StreamType
        get() {
            // It is not possible to determine the stream type using the feed endpoint.
            // All entries are considered a video stream.
            return StreamType.VIDEO_STREAM
        }
    override val isAd: Boolean
        get() {
            return false
        }
    override val duration: Long
        get() {
            // Not available when fetching through the feed endpoint.
            return -1
        }
    override val viewCount: Long
        get() {
            return entryElement.getElementsByTag("media:statistics").first()
                    .attr("views").toLong()
        }
    override val uploaderName: String?
        get() {
            return entryElement.select("author > name").first()!!.text()
        }
    override val uploaderUrl: String?
        get() {
            return entryElement.select("author > uri").first()!!.text()
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val textualUploadDate: String?
        get() {
            return entryElement.getElementsByTag("published").first()!!.text()
        }

    @get:Throws(ParsingException::class)
    override val uploadDate: DateWrapper?
        get() {
            try {
                return DateWrapper(OffsetDateTime.parse(textualUploadDate))
            } catch (e: DateTimeParseException) {
                throw ParsingException("Could not parse date (\"" + textualUploadDate + "\")",
                        e)
            }
        }
    override val name: String?
        get() {
            return entryElement.getElementsByTag("title").first()!!.text()
        }
    override val url: String?
        get() {
            return entryElement.getElementsByTag("link").first()!!.attr("href")
        }

    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            val thumbnailElement: Element? = entryElement.getElementsByTag("media:thumbnail").first()
            if (thumbnailElement == null) {
                return listOf<Image>()
            }
            val feedThumbnailUrl: String = thumbnailElement.attr("url")

            // If the thumbnail URL is empty, it means that no thumbnail is available, return an empty
            // list in this case
            if (feedThumbnailUrl.isEmpty()) {
                return listOf<Image>()
            }

            // The hqdefault thumbnail has some black bars at the top and at the bottom, while the
            // mqdefault doesn't, so return the mqdefault one. It should always exist, according to
            // https://stackoverflow.com/a/20542029/9481500.
            val newFeedThumbnailUrl: String = feedThumbnailUrl.replace("hqdefault", "mqdefault")
            var height: Int
            var width: Int

            // If the new thumbnail URL is equal to the feed one, it means that a different image
            // resolution is used on feeds, so use the height and width provided instead of the
            // mqdefault ones
            if ((newFeedThumbnailUrl == feedThumbnailUrl)) {
                try {
                    height = thumbnailElement.attr("height").toInt()
                } catch (e: NumberFormatException) {
                    height = Image.Companion.HEIGHT_UNKNOWN
                }
                try {
                    width = thumbnailElement.attr("width").toInt()
                } catch (e: NumberFormatException) {
                    width = Image.Companion.WIDTH_UNKNOWN
                }
            } else {
                height = 320
                width = 180
            }
            return java.util.List.of<Image?>(
                    Image(newFeedThumbnailUrl, height, width, ResolutionLevel.Companion.fromHeight(height)))
        }
}
