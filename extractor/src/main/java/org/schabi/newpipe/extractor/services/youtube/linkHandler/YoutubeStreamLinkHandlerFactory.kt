/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeStreamLinkHandlerFactory.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class YoutubeStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Nonnull
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?): String? {
        return "https://www.youtube.com/watch?v=" + id
    }

    @Nonnull
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(theUrlString: String?): String? {
        var urlString: String? = theUrlString
        try {
            val uri: URI = URI(urlString)
            val scheme: String? = uri.getScheme()
            if ((scheme != null
                            && ((scheme == "vnd.youtube") || (scheme == "vnd.youtube.launch")))) {
                val schemeSpecificPart: String = uri.getSchemeSpecificPart()
                if (schemeSpecificPart.startsWith("//")) {
                    val extractedId: String? = extractId(schemeSpecificPart.substring(2))
                    if (extractedId != null) {
                        return extractedId
                    }
                    urlString = "https:" + schemeSpecificPart
                } else {
                    return assertIsId(schemeSpecificPart)
                }
            }
        } catch (ignored: URISyntaxException) {
        }
        val url: URL?
        try {
            url = Utils.stringToURL(urlString)
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("The given URL is not valid")
        }
        val host: String = url.getHost()
        var path: String = url.getPath()
        // remove leading "/" of URL-path if URL-path is given
        if (!path.isEmpty()) {
            path = path.substring(1)
        }
        if (!Utils.isHTTP(url) || !((YoutubeParsingHelper.isYoutubeURL(url) || YoutubeParsingHelper.isYoutubeServiceURL(url)
                        || YoutubeParsingHelper.isHooktubeURL(url) || YoutubeParsingHelper.isInvidiousURL(url) || YoutubeParsingHelper.isY2ubeURL(url)))) {
            if (host.equals("googleads.g.doubleclick.net", ignoreCase = true)) {
                throw FoundAdException("Error: found ad: " + urlString)
            }
            throw ParsingException("The URL is not a YouTube URL")
        }
        if (YoutubePlaylistLinkHandlerFactory.Companion.getInstance().acceptUrl(urlString)) {
            throw ParsingException("Error: no suitable URL: " + urlString)
        }
        when (host.uppercase(Locale.getDefault())) {
            "WWW.YOUTUBE-NOCOOKIE.COM" -> {
                if (path.startsWith("embed/")) {
                    return assertIsId(path.substring(6))
                }
            }

            "YOUTUBE.COM", "WWW.YOUTUBE.COM", "M.YOUTUBE.COM", "MUSIC.YOUTUBE.COM" -> {
                if ((path == "attribution_link")) {
                    val uQueryValue: String? = Utils.getQueryValue(url, "u")
                    val decodedURL: URL?
                    try {
                        decodedURL = Utils.stringToURL("https://www.youtube.com" + uQueryValue)
                    } catch (e: MalformedURLException) {
                        throw ParsingException("Error: no suitable URL: " + urlString)
                    }
                    val viewQueryValue: String? = Utils.getQueryValue(decodedURL, "v")
                    return assertIsId(viewQueryValue)
                }
                val maybeId: String? = getIdFromSubpathsInPath(path)
                if (maybeId != null) {
                    return maybeId
                }
                val viewQueryValue: String? = Utils.getQueryValue(url, "v")
                return assertIsId(viewQueryValue)
            }

            "Y2U.BE", "YOUTU.BE" -> {
                val viewQueryValue: String? = Utils.getQueryValue(url, "v")
                if (viewQueryValue != null) {
                    return assertIsId(viewQueryValue)
                }
                return assertIsId(path)
            }

            "HOOKTUBE.COM", "INVIDIO.US", "DEV.INVIDIO.US", "WWW.INVIDIO.US", "REDIRECT.INVIDIOUS.IO", "INVIDIOUS.SNOPYTA.ORG", "YEWTU.BE", "TUBE.CONNECT.CAFE", "TUBUS.EDUVID.ORG", "INVIDIOUS.KAVIN.ROCKS", "INVIDIOUS-US.KAVIN.ROCKS", "PIPED.KAVIN.ROCKS", "INVIDIOUS.SITE", "VID.MINT.LGBT", "INVIDIOU.SITE", "INVIDIOUS.FDN.FR", "INVIDIOUS.048596.XYZ", "INVIDIOUS.ZEE.LI", "VID.PUFFYAN.US", "YTPRIVATE.COM", "INVIDIOUS.NAMAZSO.EU", "INVIDIOUS.SILKKY.CLOUD", "INVIDIOUS.EXONIP.DE", "INV.RIVERSIDE.ROCKS", "INVIDIOUS.BLAMEFRAN.NET", "INVIDIOUS.MOOMOO.ME", "YTB.TROM.TF", "YT.CYBERHOST.UK", "Y.COM.CM" -> {
                // code-block for hooktube.com and Invidious instances
                if ((path == "watch")) {
                    val viewQueryValue: String? = Utils.getQueryValue(url, "v")
                    if (viewQueryValue != null) {
                        return assertIsId(viewQueryValue)
                    }
                }
                val maybeId: String? = getIdFromSubpathsInPath(path)
                if (maybeId != null) {
                    return maybeId
                }
                val viewQueryValue: String? = Utils.getQueryValue(url, "v")
                if (viewQueryValue != null) {
                    return assertIsId(viewQueryValue)
                }
                return assertIsId(path)
            }
        }
        throw ParsingException("Error: no suitable URL: " + urlString)
    }

    @Throws(FoundAdException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            getId(url)
            return true
        } catch (fe: FoundAdException) {
            throw fe
        } catch (e: ParsingException) {
            return false
        }
    }

    @Throws(ParsingException::class)
    private fun getIdFromSubpathsInPath(path: String): String? {
        for (subpath: String in SUBPATHS) {
            if (path.startsWith(subpath)) {
                val id: String = path.substring(subpath.length)
                return assertIsId(id)
            }
        }
        return null
    }

    companion object {
        private val YOUTUBE_VIDEO_ID_REGEX_PATTERN: Pattern = Pattern.compile("^([a-zA-Z0-9_-]{11})")
        val instance: YoutubeStreamLinkHandlerFactory = YoutubeStreamLinkHandlerFactory()
        private val SUBPATHS: List<String> = listOf("embed/", "live/", "shorts/", "watch/", "v/", "w/")
        private fun extractId(id: String?): String? {
            if (id != null) {
                val m: Matcher = YOUTUBE_VIDEO_ID_REGEX_PATTERN.matcher(id)
                return if (m.find()) m.group(1) else null
            }
            return null
        }

        @Nonnull
        @Throws(ParsingException::class)
        private fun assertIsId(id: String?): String {
            val extractedId: String? = extractId(id)
            if (extractedId != null) {
                return extractedId
            } else {
                throw ParsingException("The given string is not a YouTube video ID")
            }
        }
    }
}
