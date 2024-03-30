/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamInfoItemsCollector.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException

class StreamInfoItemsCollector : InfoItemsCollector<StreamInfoItem?, StreamInfoItemExtractor> {
    constructor(serviceId: Int) : super(serviceId)
    constructor(serviceId: Int,
                comparator: Comparator<StreamInfoItem>?) : super(serviceId, comparator)

    @Throws(ParsingException::class)
    public override fun extract(extractor: StreamInfoItemExtractor): StreamInfoItem? {
        if (extractor.isAd()) {
            throw FoundAdException("Found ad")
        }
        val resultItem: StreamInfoItem = StreamInfoItem(
                getServiceId(), extractor.getUrl(), extractor.getName(), extractor.getStreamType())

        // optional information
        try {
            resultItem.setDuration(extractor.getDuration())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setUploaderName(extractor.getUploaderName())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setTextualUploadDate(extractor.getTextualUploadDate())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setUploadDate(extractor.getUploadDate())
        } catch (e: ParsingException) {
            addError(e)
        }
        try {
            resultItem.setViewCount(extractor.getViewCount())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setThumbnails(extractor.getThumbnails())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setUploaderUrl(extractor.getUploaderUrl())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setUploaderAvatars(extractor.getUploaderAvatars())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setUploaderVerified(extractor.isUploaderVerified())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setShortDescription(extractor.getShortDescription())
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.setShortFormContent(extractor.isShortFormContent())
        } catch (e: Exception) {
            addError(e)
        }
        return resultItem
    }

    public override fun commit(extractor: StreamInfoItemExtractor) {
        try {
            addItem(extract(extractor))
        } catch (ignored: FoundAdException) {
        } catch (e: Exception) {
            addError(e)
        }
    }
}
