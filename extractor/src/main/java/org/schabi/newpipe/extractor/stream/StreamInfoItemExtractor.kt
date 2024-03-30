/*
 * Created by Christian Schabesberger on 28.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamInfoItemExtractor.java is part of NewPipe Extractor.
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
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.localization.DateWrapper

open interface StreamInfoItemExtractor : InfoItemExtractor {
    @get:Throws(ParsingException::class)
    val streamType: StreamType

    @get:Throws(ParsingException::class)
    val isAd: Boolean

    @get:Throws(ParsingException::class)
    val duration: Long

    @get:Throws(ParsingException::class)
    val viewCount: Long

    @get:Throws(ParsingException::class)
    val uploaderName: String?

    @get:Throws(ParsingException::class)
    val uploaderUrl: String?

    @get:Throws(ParsingException::class)
    val uploaderAvatars: List<Image?>?
        /**
         * Get the uploader avatars.
         *
         * @return the uploader avatars or an empty list if not provided by the service
         * @throws ParsingException if there is an error in the extraction
         */
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    val isUploaderVerified: Boolean

    @get:Throws(ParsingException::class)
    val textualUploadDate: String?

    @get:Throws(ParsingException::class)
    val uploadDate: DateWrapper?

    @get:Throws(ParsingException::class)
    val shortDescription: String?
        /**
         * Get the video's short description.
         *
         * @return The video's short description or `null` if not provided by the service.
         * @throws ParsingException if there is an error in the extraction
         */
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    val isShortFormContent: Boolean
        /**
         * Whether the stream is a short-form content.
         *
         *
         *
         * Short-form contents are contents in the style of TikTok, YouTube Shorts, or Instagram Reels
         * videos.
         *
         *
         * @return whether the stream is a short-form content
         * @throws ParsingException if there is an error in the extraction
         */
        get() {
            return false
        }
}
