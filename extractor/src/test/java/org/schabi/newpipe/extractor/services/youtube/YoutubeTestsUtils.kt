package org.schabi.newpipe.extractor.services.youtube

import org.schabi.newpipe.extractor.ExtractorAsserts
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.services.DefaultTests
import org.schabi.newpipe.extractor.services.youtube.YoutubeJavaScriptPlayerManager.clearAllCaches
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isConsentAccepted
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.resetClientVersionAndKey
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.setNumberGenerator
import java.util.Random
import java.util.function.Consumer

/**
 * Utility class for YouTube tests.
 */
object YoutubeTestsUtils {
    /**
     * Clears static YT states.
     *
     *
     *
     * This method needs to be called to generate all mocks of a test when running different tests
     * at the same time.
     *
     */
    fun ensureStateless() {
        isConsentAccepted = false
        resetClientVersionAndKey()
        setNumberGenerator(Random(1))
        clearAllCaches()
    }

    /**
     * Test that YouTube images of a [Collection] respect
     * [default requirements][DefaultTests.defaultTestImageCollection] and contain
     * the string `yt` in their URL.
     *
     * @param images a YouTube [Image] [Collection]
     */
    fun testImages(images: Collection<Image?>?) {
        DefaultTests.defaultTestImageCollection(images)
        // Disable NPE warning because if the collection is null, an AssertionError would be thrown
        // by DefaultTests.defaultTestImageCollection
        images!!.forEach(Consumer { image: Image? -> ExtractorAsserts.assertContains("yt", image!!.url) })
    }
}
