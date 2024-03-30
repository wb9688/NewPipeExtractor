package org.schabi.newpipe.extractor.services.media_ccc.linkHandler

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException

class MediaCCCStreamLinkHandlerFactory private constructor() : LinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(url: String?): String? {
        var streamId: String? = null
        try {
            streamId = Parser.matchGroup1(LIVE_STREAM_ID_PATTERN, url)
        } catch (ignored: RegexException) {
        }
        if (streamId == null) {
            return Parser.matchGroup1(RECORDING_ID_PATTERN, url)
        }
        return streamId
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?): String? {
        if (MediaCCCParsingHelper.isLiveStreamId(id)) {
            return LIVE_STREAM_PATH + id
        }
        return VIDEO_PATH + id
    }

    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            return getId(url) != null
        } catch (e: ParsingException) {
            return false
        }
    }

    companion object {
        val instance: MediaCCCStreamLinkHandlerFactory = MediaCCCStreamLinkHandlerFactory()
        val VIDEO_API_ENDPOINT: String = "https://api.media.ccc.de/public/events/"
        private val VIDEO_PATH: String = "https://media.ccc.de/v/"
        private val RECORDING_ID_PATTERN: String = ("(?:(?:(?:api\\.)?media\\.ccc\\.de/public/events/)"
                + "|(?:media\\.ccc\\.de/v/))([^/?&#]*)")
        private val LIVE_STREAM_PATH: String = "https://streaming.media.ccc.de/"
        private val LIVE_STREAM_ID_PATTERN: String = "streaming\\.media\\.ccc\\.de\\/(\\w+\\/\\w+)"
    }
}
