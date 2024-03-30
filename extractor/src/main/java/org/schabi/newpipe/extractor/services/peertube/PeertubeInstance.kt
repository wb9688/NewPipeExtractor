package org.schabi.newpipe.extractor.services.peertube

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import com.grack.nanojson.JsonParserException
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.utils.JsonUtils
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class PeertubeInstance {
    val url: String
    var name: String?
        private set

    constructor(url: String) {
        this.url = url
        name = "PeerTube"
    }

    constructor(url: String, name: String?) {
        this.url = url
        this.name = name
    }

    @Throws(Exception::class)
    fun fetchInstanceMetaData() {
        val response: Response?
        try {
            response = NewPipe.getDownloader().get(url + "/api/v1/config")
        } catch (e: ReCaptchaException) {
            throw Exception("unable to configure instance " + url, e)
        } catch (e: IOException) {
            throw Exception("unable to configure instance " + url, e)
        }
        if (response == null || Utils.isBlank(response.responseBody())) {
            throw Exception("unable to configure instance " + url)
        }
        try {
            val json: JsonObject = JsonParser.`object`().from(response.responseBody())
            name = JsonUtils.getString(json, "instance.name")
        } catch (e: JsonParserException) {
            throw Exception("unable to parse instance config", e)
        } catch (e: ParsingException) {
            throw Exception("unable to parse instance config", e)
        }
    }

    companion object {
        val DEFAULT_INSTANCE: PeertubeInstance = PeertubeInstance("https://framatube.org", "FramaTube")
    }
}
