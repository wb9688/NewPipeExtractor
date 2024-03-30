package org.schabi.newpipe.extractor.linkhandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.Utils
import java.util.Objects

/*
* Created by Christian Schabesberger on 26.07.16.
*
* Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
* LinkHandlerFactory.java is part of NewPipe Extractor.
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
abstract class LinkHandlerFactory() {
    ///////////////////////////////////
    // To Override
    ///////////////////////////////////
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    abstract fun getId(url: String?): String?
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    abstract fun getUrl(id: String?): String?
    @Throws(ParsingException::class)
    abstract fun onAcceptUrl(url: String?): Boolean
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    open fun getUrl(id: String?, baseUrl: String?): String? {
        return getUrl(id)
    }
    ///////////////////////////////////
    // Logic
    ///////////////////////////////////
    /**
     * Builds a [LinkHandler] from a url.<br></br>
     * Be sure to call [Utils.followGoogleRedirectIfNeeded] on the url if overriding
     * this function.
     *
     * @param url the url to extract path and id from
     * @return a [LinkHandler] complete with information
     */
    @Throws(ParsingException::class)
    open fun fromUrl(url: String?): LinkHandler? {
        if (Utils.isNullOrEmpty(url)) {
            throw IllegalArgumentException("The url is null or empty")
        }
        val polishedUrl: String? = Utils.followGoogleRedirectIfNeeded(url)
        val baseUrl: String? = Utils.getBaseUrl(polishedUrl)
        return fromUrl(polishedUrl, baseUrl)
    }

    /**
     * Builds a [LinkHandler] from an URL and a base URL. The URL is expected to be already
     * polished from Google search redirects (otherwise how could `baseUrl` have been
     * extracted?).<br></br>
     * So do not call [Utils.followGoogleRedirectIfNeeded] on the URL if overriding
     * this function, since that should be done in [.fromUrl].
     *
     * @param url     the URL without Google search redirects to extract id from
     * @param baseUrl the base URL
     * @return a [LinkHandler] complete with information
     */
    @Throws(ParsingException::class)
    open fun fromUrl(url: String?, baseUrl: String?): LinkHandler? {
        Objects.requireNonNull(url, "URL cannot be null")
        if (!acceptUrl(url)) {
            throw ParsingException("URL not accepted: " + url)
        }
        val id: String? = getId(url)
        return LinkHandler(url, getUrl(id, baseUrl), id)
    }

    @Throws(ParsingException::class)
    open fun fromId(id: String?): LinkHandler {
        Objects.requireNonNull(id, "ID cannot be null")
        val url: String? = getUrl(id)
        return LinkHandler(url, url, id)
    }

    @Throws(ParsingException::class)
    open fun fromId(id: String?, baseUrl: String?): LinkHandler {
        Objects.requireNonNull(id, "ID cannot be null")
        val url: String? = getUrl(id, baseUrl)
        return LinkHandler(url, url, id)
    }

    /**
     * When a VIEW_ACTION is caught this function will test if the url delivered within the calling
     * Intent was meant to be watched with this Service.
     * Return false if this service shall not allow to be called through ACTIONs.
     */
    @Throws(ParsingException::class)
    fun acceptUrl(url: String?): Boolean {
        return onAcceptUrl(url)
    }
}
