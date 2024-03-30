package org.schabi.newpipe.downloader

import org.schabi.newpipe.extractor.downloader.Downloader
import java.io.IOException

object DownloaderFactory {
    const val RESOURCE_PATH = "src/test/resources/org/schabi/newpipe/extractor/"
    private val DEFAULT_DOWNLOADER = DownloaderType.REAL
    val downloaderType: DownloaderType
        get() = try {
            DownloaderType.valueOf(System.getProperty("downloader"))
        } catch (e: Exception) {
            DEFAULT_DOWNLOADER
        }

    /**
     *
     *
     * Returns a implementation of a [Downloader].
     *
     *
     *
     * If the system property "downloader" is set and is one of [DownloaderType],
     * then a downloader of that type is returned.
     * It can be passed in with gradle by adding the argument -Ddownloader=abcd,
     * where abcd is one of [DownloaderType]
     *
     *
     *
     * Otherwise it falls back to [DownloaderFactory.DEFAULT_DOWNLOADER].
     * Change this during development on the local machine to use a different downloader.
     *
     *
     * @param path The path to the folder where mocks are saved/retrieved.
     * Preferably starting with [DownloaderFactory.RESOURCE_PATH]
     */
    @Throws(IOException::class)
    fun getDownloader(path: String): Downloader? {
        val type = downloaderType
        return when (type) {
            DownloaderType.REAL -> DownloaderTestImpl.Companion.getInstance()
            DownloaderType.MOCK -> MockDownloader(path)
            DownloaderType.RECORDING -> RecordingDownloader(path)
            else -> throw UnsupportedOperationException("Unknown downloader type: $type")
        }
    }
}
