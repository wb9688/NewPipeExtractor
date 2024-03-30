package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem

import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper

class BandcampSearchStreamInfoItemExtractor(private val searchResult: Element,
                                            uploaderUrl: String?) : BandcampStreamInfoItemExtractor(uploaderUrl) {
    private val resultInfo: Element?

    init {
        resultInfo = searchResult.getElementsByClass("result-info").first()
    }

    override val uploaderName: String?
        get() {
            val subhead: String = resultInfo!!.getElementsByClass("subhead").text()
            val splitBy: Array<String> = subhead.split("by ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (splitBy.size > 1) {
                return splitBy.get(1)
            } else {
                return splitBy.get(0)
            }
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
    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromSearchResult(searchResult)
        }
    override val duration: Long
        get() {
            return -1
        }
}
