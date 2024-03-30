package org.schabi.newpipe.extractor.services.youtube.extractors

import org.schabi.newpipe.extractor.services.youtube.ItagItem
import java.io.Serializable

/**
 * Class to build easier [org.schabi.newpipe.extractor.stream.Stream]s for
 * [YoutubeStreamExtractor].
 *
 *
 *
 * It stores, per stream:
 *
 *  * its content (the URL/the base URL of streams);
 *  * whether its content is the URL the content itself or the base URL;
 *  * its associated [ItagItem].
 *
 *
 */
internal class ItagInfo
/**
 * Creates a new `ItagInfo` instance.
 *
 * @param content  the content of the stream, which must be not null
 * @param itagItem the [ItagItem] associated with the stream, which must be not null
 */(@field:Nonnull
    /**
     * Gets the content stored in this `ItagInfo` instance, which is either the URL to the
     * content itself or the base URL.
     *
     * @return the content stored in this `ItagInfo` instance
     */
    val content: String?,
    @field:Nonnull
    /**
     * Gets the [ItagItem] associated with this `ItagInfo` instance.
     *
     * @return the [ItagItem] associated with this `ItagInfo` instance, which is not
     * null
     */
    val itagItem: ItagItem) : Serializable {

    /**
     * Gets whether the content stored is the URL to the content itself or the base URL of it.
     *
     * @return whether the content stored is the URL to the content itself or the base URL of it
     * @see .getContent
     */
    /**
     * Sets whether the stream is a URL.
     *
     * @param isUrl whether the content is a URL
     */
    var isUrl: Boolean = false

}
