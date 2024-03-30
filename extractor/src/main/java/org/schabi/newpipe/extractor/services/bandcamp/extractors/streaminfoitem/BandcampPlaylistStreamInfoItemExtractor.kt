// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later
package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.stream.StreamExtractor
import java.io.IOException

class BandcampPlaylistStreamInfoItemExtractor(private val track: JsonObject,
                                              uploaderUrl: String?,
                                              private val service: StreamingService?) : BandcampStreamInfoItemExtractor(uploaderUrl) {
    private var substituteCovers: List<Image?>?

    init {
        substituteCovers = emptyList<Image>()
    }

    constructor(track: JsonObject,
                uploaderUrl: String?,
                substituteCovers: List<Image?>?) : this(track, uploaderUrl, null as StreamingService?) {
        this.substituteCovers = substituteCovers
    }

    override val name: String?
        get() {
            return track.getString("title")
        }
    override val url: String?
        get() {
            return getUploaderUrl() + track.getString("title_link")
        }
    override val duration: Long
        get() {
            return track.getLong("duration")
        }
    override val uploaderName: String?
        get() {
            /* Tracks can have an individual artist name, but it is not included in the
         * given JSON.
         */
            return ""
        }

    @get:Throws(ParsingException::class)
    override val thumbnails: List<Image?>?
        /**
         * Each track can have its own cover art. Therefore, unless a substitute is provided,
         * the thumbnail is extracted using a stream extractor.
         */
        get() {
            if (substituteCovers!!.isEmpty()) {
                try {
                    val extractor: StreamExtractor? = service!!.getStreamExtractor(url)
                    extractor!!.fetchPage()
                    return extractor.getThumbnails()
                } catch (e: ExtractionException) {
                    throw ParsingException("Could not download cover art location", e)
                } catch (e: IOException) {
                    throw ParsingException("Could not download cover art location", e)
                }
            }
            return substituteCovers
        }
}
