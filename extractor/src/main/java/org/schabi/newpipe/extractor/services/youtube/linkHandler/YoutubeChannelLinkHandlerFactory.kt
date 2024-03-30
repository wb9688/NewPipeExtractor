/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chrźis.schabesberger@mailbox.org>
 * YoutubeChannelLinkHandlerFactory.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.services.youtube.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.net.URL
import java.util.regex.Pattern

class YoutubeChannelLinkHandlerFactory  // CHECKSTYLE:ON
private constructor() : ListLinkHandlerFactory() {
    /**
     * Returns the URL to a channel from an ID.
     *
     * @param id the channel ID including e.g. 'channel/'
     * @return the URL to the channel
     */
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               searchFilter: String?): String? {
        return "https://www.youtube.com/" + id
    }

    /**
     * Checks whether the given path conforms to custom short channel URLs like
     * `youtube.com/yourcustomname`.
     *
     * @param splitPath the path segments array
     * @return whether the value conform to short channel URLs
     */
    private fun isCustomShortChannelUrl(@Nonnull splitPath: Array<String>): Boolean {
        return splitPath.size == 1 && !EXCLUDED_SEGMENTS.matcher(splitPath.get(0)).matches()
    }

    /**
     * Checks whether the given path conforms to handle URLs like `youtube.com/@yourhandle`.
     *
     * @param splitPath the path segments array
     * @return whether the value conform to handle URLs
     */
    private fun isHandle(@Nonnull splitPath: Array<String>): Boolean {
        return splitPath.size > 0 && splitPath.get(0).startsWith("@")
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        try {
            val urlObj: URL? = Utils.stringToURL(url)
            var path: String = urlObj!!.getPath()
            if (!Utils.isHTTP(urlObj) || !((YoutubeParsingHelper.isYoutubeURL(urlObj)
                            || YoutubeParsingHelper.isInvidiousURL(urlObj)
                            || YoutubeParsingHelper.isHooktubeURL(urlObj)))) {
                throw ParsingException("The URL given is not a YouTube URL")
            }

            // Remove leading "/"
            path = path.substring(1)
            var splitPath: Array<String> = path.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (isHandle(splitPath)) {
                // Handle YouTube handle URLs like youtube.com/@yourhandle
                return splitPath.get(0)
            } else if (isCustomShortChannelUrl(splitPath)) {
                // Handle custom short channel URLs like youtube.com/yourcustomname
                path = "c/" + path
                splitPath = path.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            }
            if ((!path.startsWith("user/") && !path.startsWith("channel/")
                            && !path.startsWith("c/"))) {
                throw ParsingException(
                        "The given URL is not a channel, a user or a handle URL")
            }
            val id: String = splitPath.get(1)
            if (Utils.isBlank(id)) {
                throw ParsingException("The given ID is not a YouTube channel or user ID")
            }
            return splitPath.get(0) + "/" + id
        } catch (e: Exception) {
            throw ParsingException("Could not parse URL :" + e.message, e)
        }
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            getId(url)
        } catch (e: ParsingException) {
            return false
        }
        return true
    }

    companion object {
        val instance: YoutubeChannelLinkHandlerFactory = YoutubeChannelLinkHandlerFactory()
        private val EXCLUDED_SEGMENTS: Pattern = Pattern.compile( // CHECKSTYLE:OFF
                "playlist|watch|attribution_link|watch_popup|embed|feed|select_site|account|reporthistory|redirect")
    }
}
