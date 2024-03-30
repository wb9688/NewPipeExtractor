package org.schabi.newpipe.extractor.utils

import org.schabi.newpipe.extractor.Image.ResolutionLevel
import org.schabi.newpipe.extractor.utils.ImageSuffix
import java.io.Serializable
import java.util.Objects

/**
 * Serializable class representing a suffix (which may include its format extension, such as
 * `.jpg`) which needs to be added to get an image/thumbnail URL with its corresponding
 * height, width and estimated resolution level.
 *
 *
 *
 * This class is used to construct [Image][org.schabi.newpipe.extractor.Image]
 * instances from a single base URL/path, in order to get all or most image resolutions provided,
 * depending of the service and the resolutions provided.
 *
 *
 *
 *
 * Note that this class is not intended to be used externally and so should only be used when
 * interfacing with the extractor.
 *
 */
class ImageSuffix(@field:Nonnull
                  /**
                   * @return the suffix which needs to be appended to get the full image URL
                   */
                  @param:Nonnull val suffix: String,
                  /**
                   * @return the height corresponding to the image suffix, which may be unknown
                   */
                  val height: Int,
                  /**
                   * @return the width corresponding to the image suffix, which may be unknown
                   */
                  val width: Int,
                  estimatedResolutionLevel: ResolutionLevel) : Serializable {

    /**
     * @return the estimated [ResolutionLevel] of the suffix, which is never null.
     */
    val resolutionLevel: ResolutionLevel

    /**
     * Create a new [ImageSuffix] instance.
     *
     * @param suffix                   the suffix string
     * @param height                   the height corresponding to the image suffix
     * @param width                    the width corresponding to the image suffix
     * @param estimatedResolutionLevel the [ResolutionLevel] of the image suffix, which must
     * not be null
     * @throws NullPointerException if `estimatedResolutionLevel` is `null`
     */
    init {
        resolutionLevel = Objects.requireNonNull(estimatedResolutionLevel,
                "estimatedResolutionLevel is null")
    }

    /**
     * Get a string representation of this [ImageSuffix] instance.
     *
     *
     *
     * The representation will be in the following format, where `suffix`, `height`,
     * `width` and `resolutionLevel` represent the corresponding properties:
     * <br></br>
     * <br></br>
     * `ImageSuffix {url=url, height=height, width=width, resolutionLevel=resolutionLevel}'`
     *
     *
     * @return a string representation of this [ImageSuffix] instance
     */
    override fun toString(): String {
        return ("ImageSuffix {" + "suffix=" + suffix + ", height=" + height + ", width="
                + width + ", resolutionLevel=" + resolutionLevel + "}")
    }
}
