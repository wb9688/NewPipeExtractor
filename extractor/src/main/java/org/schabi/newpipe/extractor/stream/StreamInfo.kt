/*
 * Created by Christian Schabesberger on 26.08.15.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * StreamInfo.java is part of NewPipe Extractor.
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
import org.schabi.newpipe.extractor.Info
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.MetaInfo
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.StreamExtractor.Privacy
import org.schabi.newpipe.extractor.utils.ExtractorHelper
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException
import java.util.Locale

/**
 * Info object for opened contents, i.e. the content ready to play.
 */
class StreamInfo(serviceId: Int,
                 url: String?,
                 originalUrl: String?,
                 /**
                  * Get the stream type
                  *
                  * @return the stream type
                  */
                 var streamType: StreamType?,
                 id: String?,
                 name: String?,
                 var ageLimit: Int) : Info(serviceId, id, url, originalUrl, name) {
    class StreamExtractException internal constructor(message: String?) : ExtractionException(message)

    /**
     * Get the thumbnail url
     *
     * @return the thumbnail url as a string
     */
    @Nonnull
    var thumbnails: List<Image?>? = listOf<Image>()
    var textualUploadDate: String? = null
    var uploadDate: DateWrapper? = null

    /**
     * Get the duration in seconds
     *
     * @return the duration in seconds
     */
    var duration: Long = -1
    var description: Description? = null
    var viewCount: Long = -1

    /**
     * Get the number of likes.
     *
     * @return The number of likes or -1 if this information is not available
     */
    var likeCount: Long = -1

    /**
     * Get the number of dislikes.
     *
     * @return The number of likes or -1 if this information is not available
     */
    var dislikeCount: Long = -1
    var uploaderName: String? = ""
    var uploaderUrl: String? = ""

    @Nonnull
    var uploaderAvatars: List<Image?>? = listOf<Image>()
    var isUploaderVerified: Boolean = false
    var uploaderSubscriberCount: Long = -1
    var subChannelName: String? = ""
    var subChannelUrl: String? = ""

    @Nonnull
    var subChannelAvatars: List<Image?>? = listOf<Image>()
    var videoStreams: List<VideoStream?>? = listOf<VideoStream>()
    var audioStreams: List<AudioStream?>? = listOf<AudioStream>()
    var videoOnlyStreams: List<VideoStream?>? = listOf<VideoStream>()
    var dashMpdUrl: String? = ""
    var hlsUrl: String? = ""
    var relatedItems: List<InfoItem?>? = listOf<InfoItem>()
    var startPosition: Long = 0
    var subtitles: List<SubtitlesStream?>? = listOf<SubtitlesStream>()
    var host: String? = ""
    var privacy: Privacy? = null
    var category: String? = ""
    var licence: String? = ""
    var supportInfo: String? = ""
    var languageInfo: Locale? = null
    var tags: List<String?>? = listOf<String>()
    var streamSegments: List<StreamSegment?>? = listOf<StreamSegment>()

    var metaInfo: List<MetaInfo?>? = listOf<MetaInfo>()
    var isShortFormContent: Boolean = false

    /**
     * Preview frames, e.g. for the storyboard / seekbar thumbnail preview
     */
    var previewFrames: List<Frameset?>? = listOf<Frameset>()

    @get:Deprecated("Use {@link #getRelatedItems()}")
    @set:Deprecated("Use {@link #setRelatedItems(List)}")
    var relatedStreams: List<InfoItem?>?
        get() {
            return relatedItems
        }
        set(relatedItemsToSet) {
            relatedItems = relatedItemsToSet
        }

    companion object {
        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(url: String): StreamInfo {
            return getInfo(NewPipe.getServiceByUrl(url), url)
        }

        @Throws(IOException::class, ExtractionException::class)
        fun getInfo(service: StreamingService?,
                    url: String?): StreamInfo {
            return getInfo(service!!.getStreamExtractor(url))
        }

        @Throws(ExtractionException::class, IOException::class)
        fun getInfo(extractor: StreamExtractor?): StreamInfo {
            extractor!!.fetchPage()
            val streamInfo: StreamInfo
            try {
                streamInfo = extractImportantData(extractor)
                extractStreams(streamInfo, extractor)
                extractOptionalData(streamInfo, extractor)
                return streamInfo
            } catch (e: ExtractionException) {
                // Currently, YouTube does not distinguish between age restricted videos and videos
                // blocked by country. This means that during the initialisation of the extractor, the
                // extractor will assume that a video is age restricted while in reality it is blocked
                // by country.
                //
                // We will now detect whether the video is blocked by country or not.
                val errorMessage: String? = extractor.getErrorMessage()
                if (Utils.isNullOrEmpty(errorMessage)) {
                    throw e
                } else {
                    throw ContentNotAvailableException(errorMessage, e)
                }
            }
        }

        @Nonnull
        @Throws(ExtractionException::class)
        private fun extractImportantData(extractor: StreamExtractor?): StreamInfo {
            // Important data, without it the content can't be displayed.
            // If one of these is not available, the frontend will receive an exception directly.
            val url: String? = extractor.getUrl()
            val streamType: StreamType? = extractor.getStreamType()
            val id: String? = extractor.getId()
            val name: String? = extractor.getName()
            val ageLimit: Int = extractor.getAgeLimit()

            // Suppress always-non-null warning as here we double-check it really is not null
            if (((streamType == StreamType.NONE
                            ) || Utils.isNullOrEmpty(url)
                            || Utils.isNullOrEmpty(id)
                            || (name == null /* but it can be empty of course */
                            ) || (ageLimit == -1))) {
                throw ExtractionException("Some important stream information was not given.")
            }
            return StreamInfo(extractor.getServiceId(), url, extractor.getOriginalUrl(),
                    streamType, id, name, ageLimit)
        }

        @Throws(ExtractionException::class)
        private fun extractStreams(streamInfo: StreamInfo,
                                   extractor: StreamExtractor?) {
            /* ---- Stream extraction goes here ---- */
            // At least one type of stream has to be available, otherwise an exception will be thrown
            // directly into the frontend.
            try {
                streamInfo.dashMpdUrl = extractor.getDashMpdUrl()
            } catch (e: Exception) {
                streamInfo.addError(ExtractionException("Couldn't get DASH manifest", e))
            }
            try {
                streamInfo.hlsUrl = extractor.getHlsUrl()
            } catch (e: Exception) {
                streamInfo.addError(ExtractionException("Couldn't get HLS manifest", e))
            }
            try {
                streamInfo.audioStreams = extractor.getAudioStreams()
            } catch (e: ContentNotSupportedException) {
                throw e
            } catch (e: Exception) {
                streamInfo.addError(ExtractionException("Couldn't get audio streams", e))
            }
            try {
                streamInfo.videoStreams = extractor.getVideoStreams()
            } catch (e: Exception) {
                streamInfo.addError(ExtractionException("Couldn't get video streams", e))
            }
            try {
                streamInfo.videoOnlyStreams = extractor.getVideoOnlyStreams()
            } catch (e: Exception) {
                streamInfo.addError(ExtractionException("Couldn't get video only streams", e))
            }

            // Either audio or video has to be available, otherwise we didn't get a stream (since
            // videoOnly are optional, they don't count).
            if ((streamInfo.videoStreams!!.isEmpty()) && (streamInfo.audioStreams!!.isEmpty())) {
                throw StreamExtractException(
                        "Could not get any stream. See error variable to get further details.")
            }
        }

        private fun extractOptionalData(streamInfo: StreamInfo,
                                        extractor: StreamExtractor?) {
            /* ---- Optional data goes here: ---- */
            // If one of these fails, the frontend needs to handle that they are not available.
            // Exceptions are therefore not thrown into the frontend, but stored into the error list,
            // so the frontend can afterwards check where errors happened.
            try {
                streamInfo.thumbnails = extractor.getThumbnails()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.duration = extractor.getLength()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.uploaderName = extractor.getUploaderName()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.uploaderUrl = extractor.getUploaderUrl()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.uploaderAvatars = extractor.getUploaderAvatars()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.isUploaderVerified = extractor!!.isUploaderVerified()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.uploaderSubscriberCount = extractor.getUploaderSubscriberCount()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.subChannelName = extractor.getSubChannelName()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.subChannelUrl = extractor.getSubChannelUrl()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.subChannelAvatars = extractor.getSubChannelAvatars()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.description = extractor.getDescription()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.viewCount = extractor.getViewCount()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.textualUploadDate = extractor.getTextualUploadDate()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.uploadDate = extractor.getUploadDate()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.startPosition = extractor.getTimeStamp()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.likeCount = extractor.getLikeCount()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.dislikeCount = extractor.getDislikeCount()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.subtitles = extractor.getSubtitlesDefault()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }

            // Additional info
            try {
                streamInfo.host = extractor.getHost()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.privacy = extractor.getPrivacy()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.category = extractor.getCategory()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.licence = extractor.getLicence()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.languageInfo = extractor.getLanguageInfo()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.tags = extractor.getTags()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.supportInfo = extractor.getSupportInfo()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.streamSegments = extractor.getStreamSegments()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.metaInfo = extractor.getMetaInfo()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.previewFrames = extractor.getFrames()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            try {
                streamInfo.isShortFormContent = extractor!!.isShortFormContent()
            } catch (e: Exception) {
                streamInfo.addError(e)
            }
            streamInfo.relatedItems = ExtractorHelper.getRelatedItemsOrLogError(streamInfo,
                    extractor)
        }
    }
}
