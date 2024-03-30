package org.schabi.newpipe.extractor.playlist

import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.stream.Description

open interface PlaylistInfoItemExtractor : InfoItemExtractor {
    @get:Throws(ParsingException::class)
    val uploaderName: String?

    @get:Throws(ParsingException::class)
    val uploaderUrl: String?

    @get:Throws(ParsingException::class)
    val isUploaderVerified: Boolean

    @get:Throws(ParsingException::class)
    val streamCount: Long

    @get:Throws(ParsingException::class)
    val description: Description
        /**
         * Get the description of the playlist if there is any.
         * Otherwise, an [EMPTY_DESCRIPTION][Description.EMPTY_DESCRIPTION] is returned.
         * @return the playlist's description
         */
        get() {
            return Description.Companion.EMPTY_DESCRIPTION
        }

    @get:Throws(ParsingException::class)
    val playlistType: PlaylistType?
        /**
         * @return the type of this playlist, see [PlaylistInfo.PlaylistType] for a description
         * of types. If not overridden always returns [PlaylistInfo.PlaylistType.NORMAL].
         */
        get() {
            return PlaylistType.NORMAL
        }
}
