// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException

class BandcampChannelInfoItemExtractor(private val searchResult: Element) : ChannelInfoItemExtractor {
    private val resultInfo: Element?

    init {
        resultInfo = searchResult.getElementsByClass("result-info").first()
    }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return resultInfo!!.getElementsByClass("heading").text()
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return resultInfo!!.getElementsByClass("itemurl").text()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromSearchResult(searchResult)
        }
    override val description: String?
        get() {
            return resultInfo!!.getElementsByClass("subhead").text()
        }
    override val subscriberCount: Long
        get() {
            return -1
        }
    override val streamCount: Long
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return false
        }
}
