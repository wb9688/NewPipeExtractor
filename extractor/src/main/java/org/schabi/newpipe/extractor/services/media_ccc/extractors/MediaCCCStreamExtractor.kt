package org.schabi.newpipe.extractor.services.media_ccc.extractors

import com.grack.nanojson.JsonArray
import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.Stream
import org.schabi.newpipe.extractor.stream.StreamExtractor
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.LocaleCompat
import java.io.IOException
import java.util.Locale
import java.util.function.Supplier

class MediaCCCStreamExtractor(service: StreamingService, linkHandler: LinkHandler?) : StreamExtractor(service, linkHandler) {
    private var data: JsonObject? = null
    private var conferenceData: JsonObject? = null

    @get:Nonnull
    override val textualUploadDate: String?
        get() {
            return data!!.getString("release_date")
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val uploadDate: DateWrapper?
        get() {
            return DateWrapper(MediaCCCParsingHelper.parseDateFrom(textualUploadDate))
        }

    @get:Nonnull
    override val thumbnails: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getThumbnailsFromStreamItem(data)
        }

    @get:Nonnull
    override val description: Description
        get() {
            return Description(data!!.getString("description"), Description.Companion.PLAIN_TEXT)
        }
    override val length: Long
        get() {
            return data!!.getInt("length").toLong()
        }
    override val viewCount: Long
        get() {
            return data!!.getInt("view_count").toLong()
        }

    @get:Nonnull
    override val uploaderUrl: String?
        get() {
            return MediaCCCConferenceLinkHandlerFactory.Companion.CONFERENCE_PATH + uploaderName
        }

    @get:Nonnull
    override val uploaderName: String?
        get() {
            return data!!.getString("conference_url")
                    .replaceFirst("https://(api\\.)?media\\.ccc\\.de/public/conferences/".toRegex(), "")
        }

    @get:Nonnull
    override val uploaderAvatars: List<Image?>?
        get() {
            return MediaCCCParsingHelper.getImageListFromLogoImageUrl(conferenceData!!.getString("logo_url"))
        }

    @get:Throws(ExtractionException::class)
    override val audioStreams: List<AudioStream?>
        get() {
            val recordings: JsonArray = data!!.getArray("recordings")
            val audioStreams: MutableList<AudioStream?> = ArrayList()
            for (i in recordings.indices) {
                val recording: JsonObject = recordings.getObject(i)
                val mimeType: String = recording.getString("mime_type")
                if (mimeType.startsWith("audio")) {
                    // First we need to resolve the actual video data from the CDN
                    val mediaFormat: MediaFormat?
                    if (mimeType.endsWith("opus")) {
                        mediaFormat = MediaFormat.OPUS
                    } else if (mimeType.endsWith("mpeg")) {
                        mediaFormat = MediaFormat.MP3
                    } else if (mimeType.endsWith("ogg")) {
                        mediaFormat = MediaFormat.OGG
                    } else {
                        mediaFormat = null
                    }
                    val builder: AudioStream.Builder? = AudioStream.Builder()
                            .setId(recording.getString("filename", Stream.Companion.ID_UNKNOWN))
                            .setContent(recording.getString("recording_url"), true)
                            .setMediaFormat(mediaFormat)
                            .setAverageBitrate(AudioStream.Companion.UNKNOWN_BITRATE)
                    val language: String? = recording.getString("language")
                    // If the language contains a - symbol, this means that the stream has an audio
                    // track with multiple languages, so there is no specific language for this stream
                    // Don't set the audio language in this case
                    if (language != null && !language.contains("-")) {
                        builder!!.setAudioLocale(LocaleCompat.forLanguageTag(language).orElseThrow(Supplier({
                            ParsingException(
                                    "Cannot convert this language to a locale: " + language)
                        })
                        ))
                    }

                    // Not checking containsSimilarStream here, since MediaCCC does not provide enough
                    // information to decide whether two streams are similar. Hence that method would
                    // always return false, e.g. even for different language variations.
                    audioStreams.add(builder!!.build())
                }
            }
            return audioStreams
        }

    @get:Throws(ExtractionException::class)
    override val videoStreams: List<VideoStream?>
        get() {
            val recordings: JsonArray = data!!.getArray("recordings")
            val videoStreams: MutableList<VideoStream?> = ArrayList()
            for (i in recordings.indices) {
                val recording: JsonObject = recordings.getObject(i)
                val mimeType: String = recording.getString("mime_type")
                if (mimeType.startsWith("video")) {
                    // First we need to resolve the actual video data from the CDN
                    val mediaFormat: MediaFormat?
                    if (mimeType.endsWith("webm")) {
                        mediaFormat = MediaFormat.WEBM
                    } else if (mimeType.endsWith("mp4")) {
                        mediaFormat = MediaFormat.MPEG_4
                    } else {
                        mediaFormat = null
                    }

                    // Not checking containsSimilarStream here, since MediaCCC does not provide enough
                    // information to decide whether two streams are similar. Hence that method would
                    // always return false, e.g. even for different language variations.
                    videoStreams.add(VideoStream.Builder()
                            .setId(recording.getString("filename", Stream.Companion.ID_UNKNOWN))
                            .setContent(recording.getString("recording_url"), true)
                            .setIsVideoOnly(false)
                            .setMediaFormat(mediaFormat)
                            .setResolution(recording.getInt("height").toString() + "p")
                            .build())
                }
            }
            return videoStreams
        }
    override val videoOnlyStreams: List<VideoStream?>
        get() {
            return emptyList<VideoStream>()
        }
    override val streamType: StreamType?
        get() {
            return StreamType.VIDEO_STREAM
        }

    @Throws(IOException::class, ExtractionException::class)
    public override fun onFetchPage(@Nonnull downloader: Downloader?) {
        val videoUrl: String = MediaCCCStreamLinkHandlerFactory.Companion.VIDEO_API_ENDPOINT + getId()
        try {
            data = JsonParser.`object`().from(downloader!!.get(videoUrl).responseBody())
            conferenceData = JsonParser.`object`()
                    .from(downloader.get(data.getString("conference_url")).responseBody())
        } catch (jpe: JsonParserException) {
            throw ExtractionException("Could not parse json returned by URL: " + videoUrl,
                    jpe)
        }
    }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    override val name: String?
        get() {
            return data!!.getString("title")
        }

    @get:Nonnull
    override val originalUrl: String?
        get() {
            return data!!.getString("frontend_link")
        }

    @get:Throws(ParsingException::class)
    override val languageInfo: Locale?
        get() {
            return Localization.Companion.getLocaleFromThreeLetterCode(data!!.getString("original_language"))
        }

    @get:Nonnull
    override val tags: List<String?>?
        get() {
            return JsonUtils.getStringListFromJsonArray(data!!.getArray("tags"))
        }
}
