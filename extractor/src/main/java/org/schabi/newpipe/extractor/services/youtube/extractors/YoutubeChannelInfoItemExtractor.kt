/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeChannelInfoItemExtractor.java is part of NewPipe Extractor.
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
package org.schabi.newpipe.extractor.services.youtube.extractors

import com.grack.nanojson.JsonObject
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.utils.Utils

class YoutubeChannelInfoItemExtractor(private val channelInfoItem: JsonObject) : ChannelInfoItemExtractor {
    /**
     * New layout:
     * "subscriberCountText": Channel handle
     * "videoCountText": Subscriber count
     */
    private val withHandle: Boolean

    init {
        var wHandle: Boolean = false
        val subscriberCountText: String? = YoutubeParsingHelper.getTextFromObject(
                channelInfoItem.getObject("subscriberCountText"))
        if (subscriberCountText != null) {
            wHandle = subscriberCountText.startsWith("@")
        }
        withHandle = wHandle
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            try {
                return YoutubeParsingHelper.getThumbnailsFromInfoItem(channelInfoItem)
            } catch (e: Exception) {
                throw ParsingException("Could not get thumbnails", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val name: String?
        get() {
            try {
                return YoutubeParsingHelper.getTextFromObject(channelInfoItem.getObject("title"))
            } catch (e: Exception) {
                throw ParsingException("Could not get name", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val url: String?
        get() {
            try {
                val id: String = "channel/" + channelInfoItem.getString("channelId")
                return YoutubeChannelLinkHandlerFactory.Companion.getInstance().getUrl(id)
            } catch (e: Exception) {
                throw ParsingException("Could not get url", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val subscriberCount: Long
        get() {
            try {
                if (!channelInfoItem.has("subscriberCountText")) {
                    // Subscription count is not available for this channel item.
                    return -1
                }
                if (withHandle) {
                    if (channelInfoItem.has("videoCountText")) {
                        return Utils.mixedNumberWordToLong(YoutubeParsingHelper.getTextFromObject(
                                channelInfoItem.getObject("videoCountText")))
                    } else {
                        return -1
                    }
                }
                return Utils.mixedNumberWordToLong(YoutubeParsingHelper.getTextFromObject(
                        channelInfoItem.getObject("subscriberCountText")))
            } catch (e: Exception) {
                throw ParsingException("Could not get subscriber count", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val streamCount: Long
        get() {
            try {
                if (withHandle || !channelInfoItem.has("videoCountText")) {
                    // Video count is not available, either the channel has no public uploads
                    // or YouTube displays the channel handle instead.
                    return ListExtractor.Companion.ITEM_COUNT_UNKNOWN
                }
                return Utils.removeNonDigitCharacters(YoutubeParsingHelper.getTextFromObject(
                        channelInfoItem.getObject("videoCountText"))).toLong()
            } catch (e: Exception) {
                throw ParsingException("Could not get stream count", e)
            }
        }

    @get:Throws(ParsingException::class)
    override val isVerified: Boolean
        get() {
            return YoutubeParsingHelper.isVerified(channelInfoItem.getArray("ownerBadges"))
        }

    @get:Throws(ParsingException::class)
    override val description: String?
        get() {
            try {
                if (!channelInfoItem.has("descriptionSnippet")) {
                    // Channel have no description.
                    return null
                }
                return YoutubeParsingHelper.getTextFromObject(channelInfoItem.getObject("descriptionSnippet"))
            } catch (e: Exception) {
                throw ParsingException("Could not get description", e)
            }
        }
}
