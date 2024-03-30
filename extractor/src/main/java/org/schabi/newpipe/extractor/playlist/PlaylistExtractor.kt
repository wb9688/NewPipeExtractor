package org.schabi.newpipe.extractor.playlist

import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfoItem

abstract class PlaylistExtractor(service: StreamingService, linkHandler: ListLinkHandler?) : ListExtractor<StreamInfoItem?>(service, linkHandler) {
    @JvmField
    @get:Throws(ParsingException::class)
    abstract val uploaderUrl: String?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val uploaderName: String?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val uploaderAvatars: List<Image?>?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val isUploaderVerified: Boolean

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val streamCount: Long

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val description: Description

    @get:Throws(ParsingException::class)
    open val thumbnails: List<Image?>?
        get() {
            return emptyList<Image>()
        }

    @get:Throws(ParsingException::class)
    val banners: List<Image>
        get() {
            return listOf()
        }

    @get:Throws(ParsingException::class)
    open val subChannelName: String?
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val subChannelUrl: String?
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val subChannelAvatars: List<Image?>?
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    open val playlistType: PlaylistType?
        get() {
            return PlaylistType.NORMAL
        }
}
