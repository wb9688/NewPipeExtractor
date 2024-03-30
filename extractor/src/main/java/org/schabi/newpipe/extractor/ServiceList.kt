package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.services.bandcamp.BandcampService
import org.schabi.newpipe.extractor.services.media_ccc.MediaCCCService
import org.schabi.newpipe.extractor.services.peertube.PeertubeService
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudService
import org.schabi.newpipe.extractor.services.youtube.YoutubeService

/*
* Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
* ServiceList.java is part of NewPipe Extractor.
*
* NewPipe Extractor is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* NewPipe Extractor is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 * A list of supported services.
 */
// keep unusual names and inner assignments
object ServiceList {
    @JvmField
    val YouTube: YoutubeService = YoutubeService(0)
    @JvmField
    val SoundCloud: SoundcloudService = SoundcloudService(1)
    @JvmField
    val MediaCCC: MediaCCCService = MediaCCCService(2)
    @JvmField
    val PeerTube: PeertubeService = PeertubeService(3)
    @JvmField
    val Bandcamp: BandcampService = BandcampService(4)

    /**
     * When creating a new service, put this service in the end of this list,
     * and give it the next free id.
     */
    private val SERVICES: List<StreamingService?> = java.util.List.of(
            YouTube, SoundCloud, MediaCCC, PeerTube, Bandcamp)

    /**
     * Get all the supported services.
     *
     * @return a unmodifiable list of all the supported services
     */
    @JvmStatic
    fun all(): List<StreamingService?> {
        return SERVICES
    }
}
