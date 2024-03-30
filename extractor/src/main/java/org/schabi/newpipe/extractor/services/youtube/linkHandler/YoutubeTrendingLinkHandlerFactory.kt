/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeTrendingLinkHandlerFactory.java is part of NewPipe Extractor.
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
package org.schabi.newpipe.extractor.services.youtube.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.net.MalformedURLException
import java.net.URL

class YoutubeTrendingLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilters: List<String?>?,
                               sortFilter: String?): String? {
        return "https://www.youtube.com/feed/trending"
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        return "Trending"
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        val urlObj: URL?
        try {
            urlObj = Utils.stringToURL(url)
        } catch (e: MalformedURLException) {
            return false
        }
        val urlPath: String = urlObj.getPath()
        return (Utils.isHTTP(urlObj) && (YoutubeParsingHelper.isYoutubeURL(urlObj) || YoutubeParsingHelper.isInvidiousURL(urlObj))
                && (urlPath == "/feed/trending"))
    }

    companion object {
        val instance: YoutubeTrendingLinkHandlerFactory = YoutubeTrendingLinkHandlerFactory()
    }
}
