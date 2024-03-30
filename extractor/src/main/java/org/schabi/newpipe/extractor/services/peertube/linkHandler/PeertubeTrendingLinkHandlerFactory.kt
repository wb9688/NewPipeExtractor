package org.schabi.newpipe.extractor.services.peertube.linkHandler

import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import java.net.MalformedURLException
import java.net.URL
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

class PeertubeTrendingLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?): String? {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.getBaseUrl())
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?,
                               baseUrl: String?): String? {
        return String.format((KIOSK_MAP.get(id))!!, baseUrl)
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        val cleanUrl: String = url.replace(ServiceList.PeerTube.getBaseUrl(), "%s")
        if (cleanUrl.contains("/videos/trending")) {
            return KIOSK_TRENDING
        } else if (cleanUrl.contains("/videos/most-liked")) {
            return KIOSK_MOST_LIKED
        } else if (cleanUrl.contains("/videos/recently-added")) {
            return KIOSK_RECENT
        } else if (cleanUrl.contains("/videos/local")) {
            return KIOSK_LOCAL
        } else {
            return KIOSK_MAP.entries.stream()
                    .filter(Predicate<Map.Entry<String?, String>>({ entry: Map.Entry<String?, String> -> (cleanUrl == entry.value) }))
                    .findFirst()
                    .map<String>(Function<Map.Entry<String?, String>, String>({ java.util.Map.Entry.key }))
                    .orElseThrow<ParsingException>(Supplier<ParsingException>({ ParsingException("no id found for this url") }))
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            URL(url)
            return (url!!.contains("/videos?") || url.contains("/videos/trending")
                    || url.contains("/videos/most-liked") || url.contains("/videos/recently-added")
                    || url.contains("/videos/local"))
        } catch (e: MalformedURLException) {
            return false
        }
    }

    companion object {
        val instance: PeertubeTrendingLinkHandlerFactory = PeertubeTrendingLinkHandlerFactory()
        val KIOSK_TRENDING: String = "Trending"
        val KIOSK_MOST_LIKED: String = "Most liked"
        val KIOSK_RECENT: String = "Recently added"
        val KIOSK_LOCAL: String = "Local"
        val KIOSK_MAP: Map<String?, String> = java.util.Map.of(
                KIOSK_TRENDING, "%s/api/v1/videos?sort=-trending",
                KIOSK_MOST_LIKED, "%s/api/v1/videos?sort=-likes",
                KIOSK_RECENT, "%s/api/v1/videos?sort=-publishedAt",
                KIOSK_LOCAL, "%s/api/v1/videos?sort=-publishedAt&isLocal=true")
    }
}
