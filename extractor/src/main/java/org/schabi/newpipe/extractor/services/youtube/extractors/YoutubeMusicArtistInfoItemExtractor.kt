package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.Utils

class YoutubeMusicArtistInfoItemExtractor(private val artistInfoItem: JsonObject) : ChannelInfoItemExtractor {
    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            try {
                return YoutubeParsingHelper.getImagesFromThumbnailsArray(
                        artistInfoItem.getObject("thumbnail")
                                .getObject("musicThumbnailRenderer")
                                .getObject("thumbnail")
                                .getArray("thumbnails"))
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            val name: String? = YoutubeParsingHelper.getTextFromObject(artistInfoItem.getArray("flexColumns")
                    .getObject(0)
                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                    .getObject("text"))
            if (!Utils.isNullOrEmpty(name)) {
                return name
            }
            throw ParsingException("Could not get name")
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            val url: String? = YoutubeParsingHelper.getUrlFromNavigationEndpoint(
                    artistInfoItem.getObject("navigationEndpoint"))
            if (!Utils.isNullOrEmpty(url)) {
                return url
            }
            throw ParsingException("Could not get URL")
        }

    @get:Throws(ParsingException::class)
    override val subscriberCount: Long
        get() {
            val subscriberCount: String? = YoutubeParsingHelper.getTextFromObject(artistInfoItem.getArray("flexColumns")
                    .getObject(2)
                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                    .getObject("text"))
            if (!Utils.isNullOrEmpty(subscriberCount)) {
                try {
                    return Utils.mixedNumberWordToLong(subscriberCount)
                } catch (ignored: RegexException) {
                    // probably subscriberCount == "No subscribers" or similar
                    return 0
                }
            }
            throw ParsingException("Could not get subscriber count")
        }
    override val streamCount: Long
        get() {
            return -1
        }
    override val isVerified: Boolean
        get() {
            // An artist on YouTube Music is always verified
            return true
        }
    override val description: String?
        get() {
            return null
        }
}
