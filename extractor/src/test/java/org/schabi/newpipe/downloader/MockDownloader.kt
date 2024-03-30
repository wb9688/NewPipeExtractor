package org.schabi.newpipe.downloader

import com.google.gson.GsonBuilder
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 *
 *
 * Mocks requests by using json files created by [RecordingDownloader]
 *
 */
internal class MockDownloader(@param:Nonnull private val path: String) : Downloader() {
    private val mocks: MutableMap<Request?, Response?>

    init {
        mocks = HashMap()
        val files = File(path).listFiles()
        if (files != null) {
            for (file in files) {
                if (file.name.startsWith(RecordingDownloader.Companion.FILE_NAME_PREFIX)) {
                    val reader = InputStreamReader(FileInputStream(
                            file), StandardCharsets.UTF_8)
                    val response = GsonBuilder()
                            .create()
                            .fromJson(reader, TestRequestResponse::class.java)
                    reader.close()
                    mocks[response.request] = response.response
                }
            }
        }
    }

    override fun execute(request: Request?): Response {
        return mocks[request]
                ?: throw NullPointerException("No mock response for request with url '" + request
                        .url() + "' exists in path '" + path + "'.\nPlease make sure to run the tests "
                        + "with the RecordingDownloader first after changes.")
    }
}
