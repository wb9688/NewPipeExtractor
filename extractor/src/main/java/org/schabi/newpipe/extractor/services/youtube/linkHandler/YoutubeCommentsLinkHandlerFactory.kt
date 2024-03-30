package org.schabi.newpipe.extractor.services.youtube.linkHandler

import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory

class YoutubeCommentsLinkHandlerFactory private constructor() : ListLinkHandlerFactory() {
    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?): String? {
        return "https://www.youtube.com/watch?v=" + id
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getId(urlString: String?): String? {
        // We need the same id, avoids duplicate code
        return YoutubeStreamLinkHandlerFactory.Companion.getInstance().getId(urlString)
    }

    @Throws(FoundAdException::class)
    public override fun onAcceptUrl(url: String?): Boolean {
        try {
            getId(url)
            return true
        } catch (fe: FoundAdException) {
            throw fe
        } catch (e: ParsingException) {
            return false
        }
    }

    @Throws(ParsingException::class, UnsupportedOperationException::class)
    public override fun getUrl(id: String?,
                               contentFilter: List<String?>?,
                               sortFilter: String?): String? {
        return getUrl(id)
    }

    companion object {
        val instance: YoutubeCommentsLinkHandlerFactory = YoutubeCommentsLinkHandlerFactory()
    }
}
