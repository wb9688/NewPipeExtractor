package org.schabi.newpipe.extractor.stream

import java.io.Serializable
import kotlin.math.min

/**
 * Class to handle framesets / storyboards which summarize the stream content.
 */
class Frameset
/**
 * Creates a new Frameset or set of storyboards.
 * @param urls the URLs to the images with frames / storyboards
 * @param frameWidth the width of a single frame, in pixels
 * @param frameHeight the height of a single frame, in pixels
 * @param totalCount the total count of frames
 * @param durationPerFrame the duration per frame in milliseconds
 * @param framesPerPageX the maximum count of frames per page by x / over the width of the image
 * @param framesPerPageY the maximum count of frames per page by y / over the height
 * of the image
 */(
        /**
         * @return list of URLs to images with frames
         */
        @JvmField val urls: List<String>,
        /**
         * @return width of a one frame, in pixels
         */
        @JvmField val frameWidth: Int,
        /**
         * @return height of a one frame, in pixels
         */
        val frameHeight: Int,
        /**
         * @return total count of frames
         */
        val totalCount: Int,
        /**
         * @return duration per frame in milliseconds
         */
        @JvmField val durationPerFrame: Int,
        /**
         * @return maximum frames count by x
         */
        val framesPerPageX: Int,
        /**
         * @return maximum frames count by y
         */
        val framesPerPageY: Int) : Serializable {

    /**
     * Returns the information for the frame at stream position.
     *
     * @param position Position in milliseconds
     * @return An `int`-array containing the bounds and URL where the indexes are
     * specified as follows:
     *
     *
     *  * `0`: Index of the URL
     *  * `1`: Left bound
     *  * `2`: Top bound
     *  * `3`: Right bound
     *  * `4`: Bottom bound
     *
     */
    fun getFrameBoundsAt(position: Long): IntArray {
        if (position < 0 || position > ((totalCount + 1).toLong() * durationPerFrame)) {
            // Return the first frame as fallback
            return intArrayOf(0, 0, 0, frameWidth, frameHeight)
        }
        val framesPerStoryboard: Int = framesPerPageX * framesPerPageY
        val absoluteFrameNumber: Int = min(((position / durationPerFrame).toInt()).toDouble(), totalCount.toDouble()).toInt()
        val relativeFrameNumber: Int = absoluteFrameNumber % framesPerStoryboard
        val rowIndex: Int = Math.floorDiv(relativeFrameNumber, framesPerPageX)
        val columnIndex: Int = relativeFrameNumber % framesPerPageY
        return intArrayOf( /* storyboardIndex */
                Math.floorDiv(absoluteFrameNumber, framesPerStoryboard),  /* left */
                columnIndex * frameWidth,  /* top */
                rowIndex * frameHeight,  /* right */
                columnIndex * frameWidth + frameWidth,  /* bottom */
                rowIndex * frameHeight + frameHeight)
    }
}
