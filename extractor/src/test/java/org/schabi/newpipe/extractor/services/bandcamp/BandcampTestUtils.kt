package org.schabi.newpipe.extractor.services.bandcamp

import org.junit.jupiter.api.Assertions
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.services.DefaultTests

/**
 * Utility class for Bandcamp tests.
 */
object BandcampTestUtils {
    /**
     * Test that Bandcamp images of a [Collection] respect
     * [default requirements][DefaultTests.defaultTestImageCollection], contain
     * the string `f4.bcbits.com/img` in their URL and end with `.jpg` or `.png`.
     *
     * @param images a Bandcamp [Image] [Collection]
     */
    fun testImages(images: Collection<Image?>?) {
        DefaultTests.defaultTestImageCollection(images)
        // Disable NPE warning because if the collection is null, an AssertionError would be thrown
        // by DefaultTests.defaultTestImageCollection
        Assertions.assertTrue(images!!.stream()
                .allMatch { image: Image? ->
                    (image!!.url!!.contains("f4.bcbits.com/img")
                            && (image.url!!.endsWith(".jpg") || image.url!!.endsWith(".png")))
                })
    }
}
