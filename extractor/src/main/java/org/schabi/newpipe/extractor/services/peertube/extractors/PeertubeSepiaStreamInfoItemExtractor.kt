package org.schabi.newpipe.extractor.services.peertube.extractors

import com.grack.nanojson.JsonObject

/**
 * A StreamInfoItem collected from SepiaSearch
 */
class PeertubeSepiaStreamInfoItemExtractor(item: JsonObject, baseUrl: String?) : PeertubeStreamInfoItemExtractor(item, baseUrl) {
    init {
        val embedUrl: String = super.item.getString("embedUrl")
        val embedPath: String = super.item.getString("embedPath")
        val itemBaseUrl: String = embedUrl.replace(embedPath, "")
        setBaseUrl(itemBaseUrl)

        // Usually, all videos, pictures and other content are hosted on the instance,
        // or can be accessed by the same URL path if the instance with baseUrl federates the one
        // where the video is actually uploaded. But it can't be accessed with Sepiasearch, so we
        // use the item's instance as base URL.
    }
}
