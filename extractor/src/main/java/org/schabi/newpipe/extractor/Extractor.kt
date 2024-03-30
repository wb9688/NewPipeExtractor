package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.localization.TimeAgoParser
import java.io.IOException
import java.util.Objects

abstract class Extractor protected constructor(service: StreamingService, linkHandler: LinkHandler?) {
    /**
     * [StreamingService] currently related to this extractor.<br></br>
     * Useful for getting other things from a service (like the url handlers for
     * cleaning/accepting/get id from urls).
     */
    @JvmField
    @get:Nonnull
    val service: StreamingService

    /**
     * @return The [LinkHandler] of the current extractor object (e.g. a ChannelExtractor
     * should return a channel url handler).
     */
    @get:Nonnull
    open val linkHandler: LinkHandler?
    private var forcedLocalization: Localization? = null
    private var forcedContentCountry: ContentCountry? = null
    protected var isPageFetched: Boolean = false
        private set

    // called like this to prevent checkstyle errors about "hiding a field"
    val downloader: Downloader?

    init {
        this.service = Objects.requireNonNull(service, "service is null")
        this.linkHandler = Objects.requireNonNull(linkHandler, "LinkHandler is null")
        downloader = Objects.requireNonNull(NewPipe.getDownloader(), "downloader is null")
    }

    /**
     * Fetch the current page.
     *
     * @throws IOException         if the page can not be loaded
     * @throws ExtractionException if the pages content is not understood
     */
    @Throws(IOException::class, ExtractionException::class)
    fun fetchPage() {
        if (isPageFetched) {
            return
        }
        onFetchPage(downloader)
        isPageFetched = true
    }

    protected fun assertPageFetched() {
        if (!isPageFetched) {
            throw IllegalStateException("Page is not fetched. Make sure you call fetchPage()")
        }
    }

    /**
     * Fetch the current page.
     *
     * @param downloader the downloader to use
     * @throws IOException         if the page can not be loaded
     * @throws ExtractionException if the pages content is not understood
     */
    @Throws(IOException::class, ExtractionException::class)
    abstract fun onFetchPage(@Nonnull downloader: Downloader?)

    @get:Throws(ParsingException::class)
    @get:Nonnull
    open val id: String?
        get() {
            return linkHandler.getId()
        }

    @JvmField
    @get:Throws(ParsingException::class)
    @get:Nonnull
    abstract val name: String?

    @get:Throws(ParsingException::class)
    @get:Nonnull
    open val originalUrl: String?
        get() {
            return linkHandler.getOriginalUrl()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    open val url: String?
        get() {
            return linkHandler.getUrl()
        }

    @get:Throws(ParsingException::class)
    @get:Nonnull
    val baseUrl: String?
        get() {
            return linkHandler.getBaseUrl()
        }
    val serviceId: Int
        get() {
            return service.getServiceId()
        }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    ////////////////////////////////////////////////////////////////////////// */
    fun forceLocalization(localization: Localization?) {
        forcedLocalization = localization
    }

    fun forceContentCountry(contentCountry: ContentCountry?) {
        forcedContentCountry = contentCountry
    }

    @get:Nonnull
    val extractorLocalization: Localization?
        get() {
            return if (forcedLocalization == null) service.getLocalization() else forcedLocalization
        }

    @get:Nonnull
    val extractorContentCountry: ContentCountry?
        get() {
            return if (forcedContentCountry == null) service.getContentCountry() else forcedContentCountry
        }

    @get:Nonnull
    val timeAgoParser: TimeAgoParser?
        get() {
            return service.getTimeAgoParser(extractorLocalization)
        }
}
