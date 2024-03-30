/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamInfoItem.java is part of NewPipe Extractor.
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.stream

import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.localization.DateWrapper

/**
 * Info object for previews of unopened videos, e.g. search results, related videos.
 */
class StreamInfoItem(serviceId: Int,
                     url: String?,
                     name: String?,
                     @JvmField val streamType: StreamType?) : InfoItem(InfoType.STREAM, serviceId, url, name) {
    @JvmField
    var uploaderName: String? = null
    @JvmField
    var shortDescription: String? = null
    @JvmField
    var textualUploadDate: String? = null
    @JvmField
    var uploadDate: DateWrapper? = null
    @JvmField
    var viewCount: Long = -1
    @JvmField
    var duration: Long = -1
    @JvmField
    var uploaderUrl: String? = null

    @JvmField
    @Nonnull
    var uploaderAvatars: List<Image?>? = listOf<Image>()
    var isUploaderVerified: Boolean = false
    var isShortFormContent: Boolean = false

    public override fun toString(): String {
        return ("StreamInfoItem{"
                + "streamType=" + streamType
                + ", uploaderName='" + uploaderName + '\''
                + ", textualUploadDate='" + textualUploadDate + '\''
                + ", viewCount=" + viewCount
                + ", duration=" + duration
                + ", uploaderUrl='" + uploaderUrl + '\''
                + ", infoType=" + getInfoType()
                + ", serviceId=" + getServiceId()
                + ", url='" + getUrl() + '\''
                + ", name='" + getName() + '\''
                + ", thumbnails='" + getThumbnails() + '\''
                + ", uploaderVerified='" + isUploaderVerified + '\''
                + '}')
    }
}