package org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators

import org.schabi.newpipe.extractor.services.youtube.DeliveryType
import org.schabi.newpipe.extractor.services.youtube.ItagItem
import org.schabi.newpipe.extractor.services.youtube.dashmanifestcreators.CreationException
import org.schabi.newpipe.extractor.utils.ManifestCreatorCache
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.util.Objects

/**
 * Class which generates DASH manifests of [YouTube progressive][DeliveryType.PROGRESSIVE]
 * streams.
 */
object YoutubeProgressiveDashManifestCreator {
    /**
     * @return the cache of DASH manifests generated for progressive streams
     */
    /**
     * Cache of DASH manifests generated for progressive streams.
     */
    @get:Nonnull
    val cache: ManifestCreatorCache<String, String> = ManifestCreatorCache()

    /**
     * Create DASH manifests from a YouTube progressive stream.
     *
     *
     *
     * Progressive streams are YouTube DASH streams which work with range requests and without the
     * need to get a manifest.
     *
     *
     *
     *
     * They can be found on all videos, and for all streams for most of videos which come from a
     * YouTube partner, and on videos with a large number of views.
     *
     *
     *
     * This method needs:
     *
     *  * the base URL of the stream (which, if you try to access to it, returns the whole
     * stream, after redirects, and if the URL is valid);
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
     *  * the duration of the video (parameter `durationSecondsFallback`), which
     * will be used as the stream duration if the duration could not be parsed from the
     * [ItagItem].
     *
     *
     *
     * @param progressiveStreamingBaseUrl the base URL of the progressive stream, which must not be
     * null
     * @param itagItem                    the [ItagItem] corresponding to the stream, which
     * must not be null
     * @param durationSecondsFallback     the duration of the progressive stream which will be used
     * if the duration could not be extracted from the
     * [ItagItem]
     * @return the manifest generated into a string
     */
    @JvmStatic
    @Nonnull
    @Throws(CreationException::class)
    fun fromProgressiveStreamingUrl(
            progressiveStreamingBaseUrl: String?,
            itagItem: ItagItem,
            durationSecondsFallback: Long): String? {
        if (cache.containsKey((progressiveStreamingBaseUrl)!!)) {
            return Objects.requireNonNull(
                    cache.get((progressiveStreamingBaseUrl))).getSecond()
        }
        val itagItemDuration: Long = itagItem.getApproxDurationMs()
        val streamDuration: Long
        if (itagItemDuration != -1L) {
            streamDuration = itagItemDuration
        } else {
            if (durationSecondsFallback > 0) {
                streamDuration = durationSecondsFallback * 1000
            } else {
                throw CreationException.Companion.couldNotAddElement(YoutubeDashManifestCreatorsUtils.MPD, ("the duration of the stream "
                        + "could not be determined and durationSecondsFallback is <= 0"))
            }
        }
        val doc: Document? = YoutubeDashManifestCreatorsUtils.generateDocumentAndDoCommonElementsGeneration(itagItem,
                streamDuration)
        generateBaseUrlElement(doc, progressiveStreamingBaseUrl)
        generateSegmentBaseElement(doc, itagItem)
        generateInitializationElement(doc, itagItem)
        return YoutubeDashManifestCreatorsUtils.buildAndCacheResult(progressiveStreamingBaseUrl, doc,
                cache)
    }

    /**
     * Generate the `<BaseURL>` element, appended as a child of the
     * `<Representation>` element.
     *
     *
     *
     * The `<Representation>` element needs to be generated before this element with
     * [YoutubeDashManifestCreatorsUtils.generateRepresentationElement]).
     *
     *
     * @param doc the [Document] on which the `<BaseURL>` element will be appended
     * @param baseUrl  the base URL of the stream, which must not be null and will be set as the
     * content of the `<BaseURL>` element
     */
    @Throws(CreationException::class)
    private fun generateBaseUrlElement(doc: Document?,
                                       baseUrl: String?) {
        try {
            val representationElement: Element = doc!!.getElementsByTagName(
                    YoutubeDashManifestCreatorsUtils.REPRESENTATION).item(0) as Element
            val baseURLElement: Element = doc.createElement(YoutubeDashManifestCreatorsUtils.BASE_URL)
            baseURLElement.setTextContent(baseUrl)
            representationElement.appendChild(baseURLElement)
        } catch (e: DOMException) {
            throw CreationException.Companion.couldNotAddElement(YoutubeDashManifestCreatorsUtils.BASE_URL, e)
        }
    }

    /**
     * Generate the `<SegmentBase>` element, appended as a child of the
     * `<Representation>` element.
     *
     *
     *
     * It generates the following element:
     * <br></br>
     * `<SegmentBase indexRange="indexStart-indexEnd" />`
     * <br></br>
     * (where `indexStart` and `indexEnd` are gotten from the [ItagItem] passed
     * as the second parameter)
     *
     *
     *
     *
     * The `<Representation>` element needs to be generated before this element with
     * [YoutubeDashManifestCreatorsUtils.generateRepresentationElement]),
     * and the `BaseURL` element with [.generateBaseUrlElement]
     * should be generated too.
     *
     *
     * @param doc the [Document] on which the `<SegmentBase>` element will be appended
     * @param itagItem the [ItagItem] to use, which must not be null
     */
    @Throws(CreationException::class)
    private fun generateSegmentBaseElement(doc: Document?,
                                           itagItem: ItagItem) {
        try {
            val representationElement: Element = doc!!.getElementsByTagName(
                    YoutubeDashManifestCreatorsUtils.REPRESENTATION).item(0) as Element
            val segmentBaseElement: Element = doc.createElement(YoutubeDashManifestCreatorsUtils.SEGMENT_BASE)
            val range: String = itagItem.getIndexStart().toString() + "-" + itagItem.getIndexEnd()
            if (itagItem.getIndexStart() < 0 || itagItem.getIndexEnd() < 0) {
                throw CreationException.Companion.couldNotAddElement(YoutubeDashManifestCreatorsUtils.SEGMENT_BASE,
                        "ItagItem's indexStart or " + "indexEnd are < 0: " + range)
            }
            YoutubeDashManifestCreatorsUtils.setAttribute(segmentBaseElement, doc, "indexRange", range)
            representationElement.appendChild(segmentBaseElement)
        } catch (e: DOMException) {
            throw CreationException.Companion.couldNotAddElement(YoutubeDashManifestCreatorsUtils.SEGMENT_BASE, e)
        }
    }

    /**
     * Generate the `<Initialization>` element, appended as a child of the
     * `<SegmentBase>` element.
     *
     *
     *
     * It generates the following element:
     * <br></br>
     * `<Initialization range="initStart-initEnd"/>`
     * <br></br>
     * (where `indexStart` and `indexEnd` are gotten from the [ItagItem] passed
     * as the second parameter)
     *
     *
     *
     *
     * The `<SegmentBase>` element needs to be generated before this element with
     * [.generateSegmentBaseElement]).
     *
     *
     * @param doc the [Document] on which the `<Initialization>` element will be
     * appended
     * @param itagItem the [ItagItem] to use, which must not be null
     */
    @Throws(CreationException::class)
    private fun generateInitializationElement(doc: Document?,
                                              itagItem: ItagItem) {
        try {
            val segmentBaseElement: Element = doc!!.getElementsByTagName(
                    YoutubeDashManifestCreatorsUtils.SEGMENT_BASE).item(0) as Element
            val initializationElement: Element = doc.createElement(YoutubeDashManifestCreatorsUtils.INITIALIZATION)
            val range: String = itagItem.getInitStart().toString() + "-" + itagItem.getInitEnd()
            if (itagItem.getInitStart() < 0 || itagItem.getInitEnd() < 0) {
                throw CreationException.Companion.couldNotAddElement(YoutubeDashManifestCreatorsUtils.INITIALIZATION,
                        "ItagItem's initStart and/or " + "initEnd are/is < 0: " + range)
            }
            YoutubeDashManifestCreatorsUtils.setAttribute(initializationElement, doc, "range", range)
            segmentBaseElement.appendChild(initializationElement)
        } catch (e: DOMException) {
            throw CreationException.Companion.couldNotAddElement(YoutubeDashManifestCreatorsUtils.INITIALIZATION, e)
        }
    }
}
