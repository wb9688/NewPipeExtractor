package org.schabi.newpipe.extractor.playlist

import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.playlist.PlaylistInfo.PlaylistType
import org.schabi.newpipe.extractor.stream.Description

class PlaylistInfoItem(serviceId: Int, url: String?, name: String?) : InfoItem(InfoType.PLAYLIST, serviceId, url, name) {
    var uploaderName: String? = null
    var uploaderUrl: String? = null
    var isUploaderVerified: Boolean = false

    /**
     * How many streams this playlist have
     */
    @JvmField
    var streamCount: Long = 0
    var description: Description? = null
    @JvmField
    var playlistType: PlaylistType? = null
}
