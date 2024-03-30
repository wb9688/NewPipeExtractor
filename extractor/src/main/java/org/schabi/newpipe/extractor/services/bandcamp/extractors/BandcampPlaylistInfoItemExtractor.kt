package org.schabi.newpipe.extractor.services.bandcamp.extractors

import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor

class BandcampPlaylistInfoItemExtractor(@param:Nonnull private val searchResult: Element) : PlaylistInfoItemExtractor {
    private val resultInfo: Element?

    init {
        resultInfo = searchResult.getElementsByClass("result-info").first()
    }

    override val uploaderName: String?
        get() {
            return resultInfo!!.getElementsByClass("subhead").text()
                    .split(" by".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(0)
        }
    override val uploaderUrl: String?
        get() {
            return null
        }
    override val isUploaderVerified: Boolean
        get() {
            return false
        }
    override val streamCount: Long
        get() {
            val length: String = resultInfo!!.getElementsByClass("length").text()
            return length.split(" track".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray().get(0).toInt().toLong()
        }
    override val name: String?
        get() {
            return resultInfo!!.getElementsByClass("heading").text()
        }
    override val url: String?
        get() {
            return resultInfo!!.getElementsByClass("itemurl").text()
        }

    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromSearchResult(searchResult)
        }
}
