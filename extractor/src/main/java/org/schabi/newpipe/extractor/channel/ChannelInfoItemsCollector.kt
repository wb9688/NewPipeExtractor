/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * ChannelInfoItemsCollector.java is part of NewPipe Extractor.
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
package org.schabi.newpipe.extractor.channel

import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.exceptions.ParsingException

class ChannelInfoItemsCollector(serviceId: Int) : InfoItemsCollector<ChannelInfoItem?, ChannelInfoItemExtractor?>(serviceId) {
    @Throws(ParsingException::class)
    public override fun extract(extractor: ChannelInfoItemExtractor): ChannelInfoItem {
        val resultItem: ChannelInfoItem = ChannelInfoItem(serviceId, extractor.url, extractor.name)

        // optional information
        try {
            resultItem.subscriberCount = extractor.subscriberCount
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.streamCount = extractor.streamCount
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.thumbnails = extractor.thumbnails
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.description = extractor.description
        } catch (e: Exception) {
            addError(e)
        }
        try {
            resultItem.isVerified = extractor.isVerified
        } catch (e: Exception) {
            addError(e)
        }
        return resultItem
    }
}
