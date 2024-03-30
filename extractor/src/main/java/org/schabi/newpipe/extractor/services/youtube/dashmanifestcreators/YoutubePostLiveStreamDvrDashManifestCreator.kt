package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators

import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.services.youtube.DeliveryType
import org.schabi.newpipe.extractor.services.youtube.ItagItem
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.CreationException
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache
import org.schabi.newpipe.extractor.utils.Utils
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.Objects

/**
 * Class which generates DASH manifests of YouTube post-live DVR streams (which use the
 * [LIVE delivery type][DeliveryType.LIVE]).
 */
object YoutubePostLiveStreamDvrDashManifestCreator {
    /**
     * @return the cache of DASH manifests generated for post-live-DVR streams
     */
    /**
     * Cache of DASH manifests generated for post-live-DVR streams.
     */
    @get:Nonnull
    val cache: ManifestCreatorCache<String, String> = ManifestCreatorCache()

    /**
     * Create DASH manifests from a YouTube post-live-DVR stream/ended livestream.
     *
     *
     *
     * Post-live-DVR streams/ended livestreams are one of the YouTube DASH specific streams which
     * works with sequences and without the need to get a manifest (even if one is provided but not
     * used by main clients (and is not complete for big ended livestreams because it doesn't
     * return the full stream)).
     *
     *
     *
     *
     * They can be found only on livestreams which have ended very recently (a few hours, most of
     * the time)
     *
     *
     *
     * This method needs:
     *
     *  * the base URL of the stream (which, if you try to access to it, returns HTTP
     * status code 404 after redirects, and if the URL is valid);
     *  * an [ItagItem], which needs to contain the following information:
     *
     *  * its type (see [ItagItem.ItagType]), to identify if the content is
     * an audio or a video stream;
     *  * its bitrate;
     *  * its mime type;
     *  * its codec(s);
     *  * for an audio stream: its audio channels;
     *  * for a video stream: its width and height.
     *
     *
     *  * the duration of the video, which will be used if the duration could not be
     * parsed from the first sequence of the stream.
     *
     *
     *
     *
     * In order to generate the DASH manifest, this method will:
     *
     *  * request the first sequence of the stream (the base URL on which the first
     * sequence parameter is appended (see [YoutubeDashManifestCreatorsUtils.SQ_0]))
     * with a `POST` or `GET` request (depending of the client on which the
     * streaming URL comes from is a mobile one (`POST`) or not (`GET`));
     *  * follow its redirection(s), if any;
     *  * save the last URL, remove the first sequence parameters;
     *  * use the information provided in the [ItagItem] to generate all elements
     * of the DASH manifest.
     *
     *
     *
     *
     *
     * If the duration cannot be extracted, the `durationSecondsFallback` value will be used
     * as the stream duration.
     *
     *
     * @param postLiveStreamDvrStreamingUrl the base URL of the post-live-DVR stream/ended
     * livestream, which must not be null
     * @param itagItem                      the [ItagItem] corresponding to the stream, which
     * must not be null
     * @param targetDurationSec             the target duration of each sequence, in seconds (this
     * value is returned with the `targetDurationSec`
     * field for each stream in YouTube's player response)
     * @param durationSecondsFallback       the duration of the ended livestream, which will be
     * used if the duration could not be extracted from the
     * first sequence
     * @return the manifest generated into a string
     */
    @Nonnull
    @Throws(CreationException::class)
    fun fromPostLiveStreamDvrStreamingUrl(
            @Nonnull postLiveStreamDvrStreamingUrl: String,
            @Nonnull itagItem: ItagItem,
            targetDurationSec: Int,
            durationSecondsFallback: Long): String? {
        if (cache.containsKey(postLiveStreamDvrStreamingUrl)) {
            return Objects.requireNonNull(
                    cache.get(postLiveStreamDvrStreamingUrl)).getSecond()
        }
        var realPostLiveStreamDvrStreamingUrl: String = postLiveStreamDvrStreamingUrl
        val streamDurationString: String?
        val segmentCount: String?
        if (targetDurationSec <= 0) {
            throw CreationException("targetDurationSec value is <= 0: " + targetDurationSec)
        }
        try {
            // Try to avoid redirects when streaming the content by saving the latest URL we get
            // from video servers.
            val response: Response? = YoutubeDashManifestCreatorsUtils.getInitializationResponse(realPostLiveStreamDvrStreamingUrl,
                    itagItem, DeliveryType.LIVE)
            realPostLiveStreamDvrStreamingUrl = response!!.latestUrl()!!.replace(YoutubeDashManifestCreatorsUtils.SQ_0, "")
                    .replace(YoutubeDashManifestCreatorsUtils.RN_0, "").replace(YoutubeDashManifestCreatorsUtils.ALR_YES, "")
            val responseCode: Int = response.responseCode()
            if (responseCode != 200) {
                throw CreationException(
                        "Could not get the initialization sequence: response code " + responseCode)
            }
            val responseHeaders: Map<String?, List<String?>?>? = response.responseHeaders()
            streamDurationString = responseHeaders!!.get("X-Head-Time-Millis")!!.get(0)
            segmentCount = responseHeaders.get("X-Head-Seqnum")!!.get(0)
        } catch (e: IndexOutOfBoundsException) {
            throw CreationException(
                    "Could not get the value of the X-Head-Time-Millis or the X-Head-Seqnum header",
                    e)
        }
        if (Utils.isNullOrEmpty(segmentCount)) {
            throw CreationException("Could not get the number of segments")
        }
        var streamDuration: Long
        try {
            streamDuration = streamDurationString!!.toLong()
        } catch (e: NumberFormatException) {
            streamDuration = durationSecondsFallback
        }
        val doc: Document? = YoutubeDashManifestCreatorsUtils.generateDocumentAndDoCommonElementsGeneration(itagItem,
                streamDuration)
        YoutubeDashManifestCreatorsUtils.generateSegmentTemplateElement(doc, realPostLiveStreamDvrStreamingUrl,
                DeliveryType.LIVE)
        YoutubeDashManifestCreatorsUtils.generateSegmentTimelineElement(doc)
        generateSegmentElementForPostLiveDvrStreams(doc, targetDurationSec, segmentCount)
        return YoutubeDashManifestCreatorsUtils.buildAndCacheResult(postLiveStreamDvrStreamingUrl, doc,
                cache)
    }

    /**
     * Generate the segment (`<S>`) element.
     *
     *
     *
     * We don't know the exact duration of segments for post-live-DVR streams but an
     * average instead (which is the `targetDurationSec` value), so we can use the following
     * structure to generate the segment timeline for DASH manifests of ended livestreams:
     * <br></br>
     * `<S d="targetDurationSecValue" r="segmentCount" />`
     *
     *
     * @param doc                   the [Document] on which the `<S>` element will
     * be appended
     * @param targetDurationSeconds the `targetDurationSec` value from YouTube player
     * response's stream
     * @param segmentCount          the number of segments, extracted by [                              ][.fromPostLiveStreamDvrStreamingUrl]
     */
    @Throws(CreationException::class)
    private fun generateSegmentElementForPostLiveDvrStreams(
            @Nonnull doc: Document?,
            targetDurationSeconds: Int,
            @Nonnull segmentCount: String?) {
        try {
            val segmentTimelineElement: Element = doc!!.getElementsByTagName(
                    YoutubeDashManifestCreatorsUtils.SEGMENT_TIMELINE).item(0) as Element
            val sElement: Element = doc.createElement("S")
            YoutubeDashManifestCreatorsUtils.setAttribute(sElement, doc, "d", (targetDurationSeconds * 1000).toString())
            YoutubeDashManifestCreatorsUtils.setAttribute(sElement, doc, "r", segmentCount)
            segmentTimelineElement.appendChild(sElement)
        } catch (e: DOMException) {
            throw CreationException.Companion.couldNotAddElement("segment (S)", e)
        }
    }
}
