// Created by Fynn Godau 2021, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors

import org.jsoup.nodes.Element
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor

/**
 * Extracts recommended albums from tracks' website
 */
class BandcampRelatedPlaylistInfoItemExtractor(private val relatedAlbum: Element) : PlaylistInfoItemExtractor {
    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            return relatedAlbum.getElementsByClass("release-title").text()
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            return relatedAlbum.getElementsByClass("album-link").attr("abs:href")
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        get() {
            return BandcampExtractorHelper.getImagesFromImageUrl(relatedAlbum.getElementsByClass("album-art").attr("src"))
        }

    @get:Throws(ParsingException::class)
    override val uploaderName: String?
        get() {
            return relatedAlbum.getElementsByClass("by-artist").text().replace("by ", "")
        }

    @get:Throws(ParsingException::class)
    override val uploaderUrl: String?
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    override val isUploaderVerified: Boolean
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            return -1
        }
}
