/*
 * Created by Christian Schabesberger on 31.07.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * ChannelInfo.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.Info
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import java.io.IOException

class ChannelInfo(serviceId: Int,
                  id: String?,
                  url: String?,
                  originalUrl: String?,
                  name: String?) : Info(serviceId, id, url, originalUrl, name) {
    var parentChannelName: String? = null
    var parentChannelUrl: String? = null
    var feedUrl: String? = null
    var subscriberCount: Long = -1
    var description: String? = null
    var donationLinks: Array<String>

    var avatars: List<Image?>? = listOf<Image>()

    var banners: List<Image?>? = listOf<Image>()

    var parentChannelAvatars: List<Image?>? = listOf<Image>()
    var isVerified: Boolean = false

    var tabs: List<ListLinkHandler?>? = listOf<ListLinkHandler>()

    var tags: List<String?>? = listOf<String>()

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): ChannelInfo {
            return getInfo(NewPipe.getServiceByUrl(url), url)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService?, url: String?): ChannelInfo {
            val extractor: ChannelExtractor? = service!!.getChannelExtractor(url)
            extractor!!.fetchPage()
            return getInfo(extractor)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(extractor: ChannelExtractor?): ChannelInfo {
            val serviceId: Int = extractor.getServiceId()
            val id: String? = extractor.getId()
            val url: String? = extractor.getUrl()
            val originalUrl: String? = extractor.getOriginalUrl()
            val name: String? = extractor.getName()
            val info: ChannelInfo = ChannelInfo(serviceId, id, url, originalUrl, name)
            try {
                info.avatars = extractor.getAvatars()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.banners = extractor.getBanners()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.feedUrl = extractor.getFeedUrl()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.subscriberCount = extractor.getSubscriberCount()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.description = extractor.getDescription()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.parentChannelName = extractor.getParentChannelName()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.parentChannelUrl = extractor.getParentChannelUrl()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.parentChannelAvatars = extractor.getParentChannelAvatars()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.isVerified = extractor!!.isVerified()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.tabs = extractor.getTabs()
            } catch (e: Exception) {
                info.addError(e)
            }
            try {
                info.tags = extractor.getTags()
            } catch (e: Exception) {
                info.addError(e)
            }
            return info
        }
    }
}
