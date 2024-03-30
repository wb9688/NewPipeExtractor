/*
 * Created by Christian Schabesberger on 10.08.18.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamExtractor.java is part of NewPipe Extractor.
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

import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.InfoItemExtractor
import org.schabi.newpipe.extractor.InfoItemsCollector
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import java.io.IOException
import java.util.Locale

/**
 * Scrapes information from a video/audio streaming service (eg, YouTube).
 */
abstract class StreamExtractor(service: StreamingService, linkHandler: LinkHandler?) : Extractor(service, linkHandler) {
    @get:Throws(ParsingException::class)
    open val textualUploadDate: String?
        /**
         * The original textual date provided by the service. Should be used as a fallback if
         * [.getUploadDate] isn't provided by the service, or it fails for some reason.
         *
         *
         * If the stream is a live stream, `null` should be returned.
         *
         * @return The original textual date provided by the service, or `null`.
         * @throws ParsingException if there is an error in the extraction
         * @see .getUploadDate
         */
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    open val uploadDate: DateWrapper?
        /**
         * A more general `Calendar` instance set to the date provided by the service.<br></br>
         * Implementations usually will just parse the date returned from the [ ][.getTextualUploadDate].
         *
         *
         * If the stream is a live stream, `null` should be returned.
         *
         * @return The date this item was uploaded, or `null`.
         * @throws ParsingException if there is an error in the extraction
         * or the extracted date couldn't be parsed.
         * @see .getTextualUploadDate
         */
        get() {
            return null
        }

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val thumbnails: List<Image?>?

    @get:Throws(ParsingException::class)
    open val description: Description
        /**
         * This is the stream description.
         *
         * @return The description of the stream/video or [Description.EMPTY_DESCRIPTION] if the
         * description is empty.
         */
        get() {
            return Description.Companion.EMPTY_DESCRIPTION
        }

    @get:Throws(ParsingException::class)
    open val length: Long
        /**
         * This should return the length of a video in seconds.
         *
         * @return The length of the stream in seconds or 0 when it has no length (e.g. a livestream).
         */
        get() {
            return 0
        }

    @get:Throws(ParsingException::class)
    open val timeStamp: Long
        /**
         * If the url you are currently handling contains a time stamp/seek, you can return the
         * position it represents here.
         * If the url has no time stamp simply return zero.
         *
         * @return the timestamp in seconds or 0 when there is no timestamp
         */
        get() {
            return 0
        }

    @get:Throws(ParsingException::class)
    open val viewCount: Long
        /**
         * The count of how many people have watched the video/listened to the audio stream.
         * If the current stream has no view count or its not available simply return -1
         *
         * @return amount of views or -1 if not available.
         */
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    open val likeCount: Long
        /**
         * The amount of likes a video/audio stream got.
         * If the current stream has no likes or its not available simply return -1
         *
         * @return the amount of likes the stream got or -1 if not available.
         */
        get() {
            return -1
        }

    @get:Throws(ParsingException::class)
    open val dislikeCount: Long
        /**
         * The amount of dislikes a video/audio stream got.
         * If the current stream has no dislikes or its not available simply return -1
         *
         * @return the amount of likes the stream got or -1 if not available.
         */
        get() {
            return -1
        }

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val uploaderUrl: String?

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val uploaderName: String?

    @get:Throws(ParsingException::class)
    open val isUploaderVerified: Boolean
        /**
         * Whether the uploader has been verified by the service's provider.
         * If there is no verification implemented, return `false`.
         *
         * @return whether the uploader has been verified by the service's provider
         */
        get() {
            return false
        }

    @get:Throws(ParsingException::class)
    open val uploaderAvatars: List<Image?>?
        /**
         * The image files/profile pictures/avatars of the creator/uploader of the stream.
         *
         *
         *
         * If they are not available in the stream on specific cases, you must return an empty list for
         * these ones, like it is made by default.
         *
         *
         * @return the avatars of the sub-channel of the stream or an empty list (default)
         */
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    open val subChannelUrl: String?
        /**
         * The Url to the page of the sub-channel of the stream. This must not be a homepage,
         * but the page offered by the service the extractor handles. This url will be handled by the
         * [ChannelExtractor], so be sure to implement that one before you return a value here,
         * otherwise NewPipe will crash if one selects this url.
         *
         * @return the url to the page of the sub-channel of the stream or an empty String
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val subChannelName: String?
        /**
         * The name of the sub-channel of the stream.
         * If the name is not available you can simply return an empty string.
         *
         * @return the name of the sub-channel of the stream or an empty String
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val subChannelAvatars: List<Image?>?
        /**
         * The avatars of the sub-channel of the stream.
         *
         *
         *
         * If they are not available in the stream on specific cases, you must return an empty list for
         * these ones, like it is made by default.
         *
         *
         *
         *
         * If the concept of sub-channels doesn't apply to the stream's service, keep the default
         * implementation.
         *
         *
         * @return the avatars of the sub-channel of the stream or an empty list (default)
         */
        get() {
            return listOf<Image>()
        }

    @get:Throws(ParsingException::class)
    open val dashMpdUrl: String
        /**
         * Get the dash mpd url. If you don't know what a dash MPD is you can read about it
         * [here](https://www.brendanlong.com/the-structure-of-an-mpeg-dash-mpd.html).
         *
         * @return the url as a string or an empty string or an empty string if not available
         * @throws ParsingException if an error occurs while reading
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val hlsUrl: String
        /**
         * I am not sure if this is in use, and how this is used. However the frontend is missing
         * support for HLS streams. Prove me if I am wrong. Please open an
         * [issue](https://github.com/teamnewpipe/newpipe/issues),
         * or fix this description if you know whats up with this.
         *
         * @return The Url to the hls stream or an empty string if not available.
         */
        get() {
            return ""
        }

    @JvmField
    @get:Throws(IOException::class, ExtractionException::class)
    abstract val audioStreams: List<AudioStream?>

    @JvmField
    @get:Throws(IOException::class, ExtractionException::class)
    abstract val videoStreams: List<VideoStream?>

    @JvmField
    @get:Throws(IOException::class, ExtractionException::class)
    abstract val videoOnlyStreams: List<VideoStream?>

    @get:Throws(IOException::class, ExtractionException::class)
    open val subtitlesDefault: List<SubtitlesStream?>
        /**
         * This will return a list of available [SubtitlesStream]s.
         * If no subtitles are available an empty list can be returned.
         *
         * @return a list of available subtitles or an empty list
         */
        get() {
            return emptyList<SubtitlesStream>()
        }

    /**
     * This will return a list of available [SubtitlesStream]s given by a specific type.
     * If no subtitles in that specific format are available an empty list can be returned.
     *
     * @param format the media format by which the subtitles should be filtered
     * @return a list of available subtitles or an empty list
     */
    @Nonnull
    @Throws(IOException::class, ExtractionException::class)
    open fun getSubtitles(format: MediaFormat): List<SubtitlesStream?> {
        return emptyList<SubtitlesStream>()
    }

    @JvmField
    @get:Throws(ParsingException::class)
    abstract val streamType: StreamType?

    @get:Throws(IOException::class, ExtractionException::class)
    open val relatedItems: InfoItemsCollector<out InfoItem?, out InfoItemExtractor?>?
        /**
         * Should return a list of streams related to the current handled. Many services show suggested
         * streams. If you don't like suggested streams you should implement them anyway since they can
         * be disabled by the user later in the frontend. The first related stream might be what was
         * previously known as a next stream.
         * If related streams aren't available simply return `null`.
         *
         * @return a list of InfoItems showing the related videos/streams
         */
        get() {
            return null
        }

    @get:Throws(IOException::class, ExtractionException::class)
    @get:Deprecated("Use {@link #getRelatedItems()}. May be removed in a future version.")
    val relatedStreams: StreamInfoItemsCollector?
        /**
         * @return The result of [.getRelatedItems] if it is a
         * [StreamInfoItemsCollector], `null` otherwise
         */
        get() {
            val collector: InfoItemsCollector<*, *>? = relatedItems
            if (collector is StreamInfoItemsCollector) {
                return collector
            } else {
                return null
            }
        }

    @get:Throws(ExtractionException::class)
    open val frames: List<Frameset>
        /**
         * Should return a list of Frameset object that contains preview of stream frames
         *
         * @return list of preview frames or empty list if frames preview is not supported or not found
         * for specified stream
         */
        get() {
            return emptyList()
        }
    open val errorMessage: String?
        /**
         * Should analyse the webpage's document and extracts any error message there might be.
         *
         * @return Error message; `null` if there is no error message.
         */
        get() {
            return null
        }
    //////////////////////////////////////////////////////////////////
    ///  Helper
    //////////////////////////////////////////////////////////////////
    /**
     * Override this function if the format of timestamp in the url is not the same format as that
     * from youtube.
     *
     * @return the time stamp/seek for the video in seconds
     */
    @Throws(ParsingException::class)
    protected fun getTimestampSeconds(regexPattern: String?): Long {
        val timestamp: String?
        try {
            timestamp = Parser.matchGroup1(regexPattern, getOriginalUrl())
        } catch (e: RegexException) {
            // catch this instantly since a url does not necessarily have a timestamp

            // -2 because the testing system will consequently know that the regex failed
            // not good, I know
            return -2
        }
        if (!timestamp.isEmpty()) {
            try {
                var secondsString: String? = ""
                var minutesString: String? = ""
                var hoursString: String? = ""
                try {
                    secondsString = Parser.matchGroup1("(\\d+)s", timestamp)
                    minutesString = Parser.matchGroup1("(\\d+)m", timestamp)
                    hoursString = Parser.matchGroup1("(\\d+)h", timestamp)
                } catch (e: Exception) {
                    // it could be that time is given in another method
                    if (secondsString!!.isEmpty() && minutesString!!.isEmpty()) {
                        // if nothing was obtained, treat as unlabelled seconds
                        secondsString = Parser.matchGroup1("t=(\\d+)", timestamp)
                    }
                }
                val seconds: Int = if (secondsString!!.isEmpty()) 0 else secondsString.toInt()
                val minutes: Int = if (minutesString!!.isEmpty()) 0 else minutesString.toInt()
                val hours: Int = if (hoursString!!.isEmpty()) 0 else hoursString.toInt()
                return seconds + (60L * minutes) + (3600L * hours)
            } catch (e: ParsingException) {
                throw ParsingException("Could not get timestamp.", e)
            }
        } else {
            return 0
        }
    }

    @get:Throws(ParsingException::class)
    open val host: String?
        /**
         * The host of the stream (Eg. peertube.cpy.re).
         * If the host is not available, or if the service doesn't use
         * a federated system, but a centralised system,
         * you can simply return an empty string.
         *
         * @return the host of the stream or an empty string.
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val privacy: Privacy?
        /**
         * The privacy of the stream (Eg. Public, Private, Unlisted…).
         *
         * @return the privacy of the stream.
         */
        get() {
            return Privacy.PUBLIC
        }

    @get:Throws(ParsingException::class)
    open val category: String?
        /**
         * The name of the category of the stream.
         * If the category is not available you can simply return an empty string.
         *
         * @return the category of the stream or an empty string.
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val licence: String?
        /**
         * The name of the licence of the stream.
         * If the licence is not available you can simply return an empty string.
         *
         * @return the licence of the stream or an empty String.
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val languageInfo: Locale?
        /**
         * The locale language of the stream.
         * If the language is not available you can simply return null.
         * If the language is provided by a language code, you can return
         * new Locale(language_code);
         *
         * @return the locale language of the stream or `null`.
         */
        get() {
            return null
        }

    @get:Throws(ParsingException::class)
    open val tags: List<String?>?
        /**
         * The list of tags of the stream.
         * If the tag list is not available you can simply return an empty list.
         *
         * @return the list of tags of the stream or Collections.emptyList().
         */
        get() {
            return emptyList<String>()
        }

    @get:Throws(ParsingException::class)
    open val supportInfo: String?
        /**
         * The support information of the stream.
         * see: https://framatube.org/videos/watch/ee408ec8-07cd-4e35-b884-fb681a4b9d37
         * (support button).
         * If the support information are not available,
         * you can simply return an empty String.
         *
         * @return the support information of the stream or an empty string.
         */
        get() {
            return ""
        }

    @get:Throws(ParsingException::class)
    open val streamSegments: List<StreamSegment>
        /**
         * The list of stream segments by timestamps for the stream.
         * If the segment list is not available you can simply return an empty list.
         *
         * @return The list of segments of the stream or an empty list.
         */
        get() {
            return emptyList()
        }

    @get:Throws(ParsingException::class)
    open val metaInfo: List<MetaInfo?>?
        /**
         * Meta information about the stream.
         *
         *
         * This can be information about the stream creator (e.g. if the creator is a public
         * broadcaster) or further information on the topic (e.g. hints that the video might contain
         * conspiracy theories or contains information about a current health situation like the
         * Covid-19 pandemic).
         *
         * The meta information often contains links to external sources like Wikipedia or the WHO.
         *
         * @return The meta info of the stream or an empty list if not provided.
         */
        get() {
            return emptyList<MetaInfo>()
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
         */
        get() {
            return false
        }

    enum class Privacy {
        PUBLIC,
        UNLISTED,
        PRIVATE,
        INTERNAL,
        OTHER
    }

    companion object {
        @get:Throws(ParsingException::class)
        val ageLimit: Int = 0
            /**
             * Get the age limit.
             *
             * @return The age which limits the content or {@value NO_AGE_LIMIT} if there is no limit
             * @throws ParsingException if an error occurs while parsing
             */
            get() {
                return Companion.field
            }

        @get:Throws(ParsingException::class)
        val uploaderSubscriberCount: Long = -1
            /**
             * The subscriber count of the uploader.
             * If the subscriber count is not implemented, or is unavailable, return `-1`.
             *
             * @return the subscriber count of the uploader or {@value UNKNOWN_SUBSCRIBER_COUNT} if not
             * available
             */
            get() {
                return Companion.field
            }
    }
}
