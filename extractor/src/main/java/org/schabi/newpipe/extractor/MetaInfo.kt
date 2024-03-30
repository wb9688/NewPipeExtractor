package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.stream.Description
import java.io.Serializable
import java.net.URL

class MetaInfo : Serializable {
    /**
     * @return Title of the info. Can be empty.
     */
    @JvmField
    @get:Nonnull
    var title: String? = ""

    @JvmField
    @get:Nonnull
    var content: Description? = null
    private var urls: MutableList<URL> = ArrayList()
    private var urlTexts: MutableList<String?> = ArrayList()

    constructor(@Nonnull title: String?,
                @Nonnull content: Description?,
                @Nonnull urls: MutableList<URL>,
                @Nonnull urlTexts: MutableList<String?>) {
        this.title = title
        this.content = content
        this.urls = urls
        this.urlTexts = urlTexts
    }

    constructor()

    @Nonnull
    fun getUrls(): List<URL> {
        return urls
    }

    fun setUrls(@Nonnull urls: MutableList<URL>) {
        this.urls = urls
    }

    fun addUrl(@Nonnull url: URL) {
        urls.add(url)
    }

    @Nonnull
    fun getUrlTexts(): List<String?> {
        return urlTexts
    }

    fun setUrlTexts(@Nonnull urlTexts: MutableList<String?>) {
        this.urlTexts = urlTexts
    }

    fun addUrlText(@Nonnull urlText: String?) {
        urlTexts.add(urlText)
    }
}
