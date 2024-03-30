/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * ChannelExtractor.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler

abstract class ChannelExtractor protected constructor(service: StreamingService, linkHandler: ListLinkHandler?) : Extractor(service, linkHandler) {
    @JvmField
    @get:Throws(ParsingException::class)
    abstract val avatars: List<Image?>?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val banners: List<Image?>?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val feedUrl: String?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val subscriberCount: Long

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val description: String?

    @get:Throws(ParsingException::class)
    abstract val parentChannelName: String?

    @get:Throws(ParsingException::class)
    abstract val parentChannelUrl: String?

    @get:Throws(ParsingException::class)
    abstract val parentChannelAvatars: List<Image?>?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val isVerified: Boolean

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val tabs: List<ListLinkHandler>

    @get:Throws(ParsingException::class)
    open val tags: List<String>
        get() {
            return listOf()
        }

    companion object {
        @JvmField
        val UNKNOWN_SUBSCRIBER_COUNT: Long = -1
    }
}
