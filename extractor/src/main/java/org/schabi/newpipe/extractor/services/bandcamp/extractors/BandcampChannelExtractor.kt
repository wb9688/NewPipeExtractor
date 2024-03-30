// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelTabLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.Collections
import java.util.Objects
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream

class BandcampChannelExtractor(service: StreamingService,
                               linkHandler: ListLinkHandler?) : ChannelExtractor(service, linkHandler) {
    private var channelInfo: JsonObject? = null

    override val avatars: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageId(channelInfo!!.getLong("bio_image_id"), false)
        }

    @get:Throws(ParsingException::class)
    override val banners: List<Image?>?
        get() {
            /*
         * Mobile API does not return the header or not the correct header.
         * Therefore, we need to query the website
         */
            try {
                val html: String? = getDownloader()
                        .get(Utils.replaceHttpWithHttps(channelInfo!!.getString("bandcamp_url")))
                        .responseBody()
                return Stream.of<Element?>(Jsoup.parse((html)!!).getElementById("customHeader"))
                        .filter(Predicate<Element?>({ obj: Element? -> Objects.nonNull(obj) }))
                        .flatMap<Element>(Function<Element?, Stream<out Element>>({ element: Element? -> element!!.getElementsByTag("img").stream() }))
                        .map<String>(Function<Element, String>({ element: Element -> element.attr("src") }))
                        .filter(Predicate<String>({ url: String -> !url.isEmpty() }))
                        .map<Image?>(Function<String, Image?>({ url: String? ->
                            Image(
                                    Utils.replaceHttpWithHttps(url), Image.Companion.HEIGHT_UNKNOWN, Image.Companion.WIDTH_UNKNOWN,
                                    ResolutionLevel.UNKNOWN)
                        }))
                        .collect(Collectors.toUnmodifiableList<Image?>())
            } catch (e: IOException) {
                throw ParsingException("Could not download artist web site", e)
            } catch (e: ReCaptchaException) {
                throw ParsingException("Could not download artist web site", e)
            }
        }
    override val feedUrl: String?
        /**
         * Bandcamp discontinued their RSS feeds because it hadn't been used enough.
         */
        get() {
            return null
        }
    override val subscriberCount: Long
        get() {
            return -1
        }
    override val description: String?
        get() {
            return channelInfo!!.getString("bio")
        }
    override val parentChannelName: String?
        get() {
            return null
        }
    override val parentChannelUrl: String?
        get() {
            return null
        }

    override val parentChannelAvatars: List<Image?>?
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val tabs: List<ListLinkHandler>
        get() {
            val discography: JsonArray = channelInfo!!.getArray("discography")
            val builder: TabExtractorBuilder = TabExtractorBuilder(discography)
            val tabs: MutableList<ListLinkHandler> = ArrayList()
            var foundTrackItem: Boolean = false
            var foundAlbumItem: Boolean = false
            for (discographyItem: Any? in discography) {
                if (foundTrackItem && foundAlbumItem) {
                    break
                }
                if (!(discographyItem is JsonObject)) {
                    continue
                }
                val itemType: String = discographyItem.getString("item_type")
                if (!foundTrackItem && ("track" == itemType)) {
                    foundTrackItem = true
                    tabs.add(ReadyChannelTabListLinkHandler((getUrl()
                            + BandcampChannelTabLinkHandlerFactory.Companion.getUrlSuffix(ChannelTabs.TRACKS)),
                            getId(),
                            ChannelTabs.TRACKS,
                            builder))
                }
                if (!foundAlbumItem && ("album" == itemType)) {
                    foundAlbumItem = true
                    tabs.add(ReadyChannelTabListLinkHandler((getUrl()
                            + BandcampChannelTabLinkHandlerFactory.Companion.getUrlSuffix(ChannelTabs.ALBUMS)),
                            getId(),
                            ChannelTabs.ALBUMS,
                            builder))
                }
            }
            return Collections.unmodifiableList(tabs)
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(downloader: Downloader?) {
        channelInfo = BandcampExtractorHelper.getArtistDetails(getId())
    }

    override val name: String?
        get() {
            return channelInfo!!.getString("name")
        }

    private class TabExtractorBuilder internal constructor(private val discography: JsonArray) : ChannelTabExtractorBuilder {
        public override fun build(service: StreamingService,
                                  linkHandler: ListLinkHandler?): ChannelTabExtractor {
            return BandcampChannelTabExtractor.Companion.fromDiscography(service, linkHandler, discography)
        }
    }
}
