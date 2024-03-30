package org.schabi.newpipe.extractor.downloader

/**
 * A Data class used to hold the results from requests made by the Downloader implementation.
 */
class Response(private val responseCode: Int,
               private val responseMessage: String,
               responseHeaders: Map<String?, List<String?>?>?,
               responseBody: String?,
               private val latestUrl: String?) {
    private val responseHeaders: Map<String?, List<String>>
    private val responseBody: String

    init {
        this.responseHeaders = if (responseHeaders == null) emptyMap() else responseHeaders
        this.responseBody = if (responseBody == null) "" else responseBody
    }

    fun responseCode(): Int {
        return responseCode
    }

    fun responseMessage(): String {
        return responseMessage
    }

    fun responseHeaders(): Map<String?, List<String>> {
        return responseHeaders
    }

    fun responseBody(): String {
        return responseBody
    }

    /**
     * Used for detecting a possible redirection, limited to the latest one.
     *
     * @return latest url known right before this response object was created
     */
    fun latestUrl(): String? {
        return latestUrl
    }
    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////// */
    /**
     * For easy access to some header value that (usually) don't repeat itself.
     *
     * For getting all the values associated to the header, use [.responseHeaders] (e.g.
     * `Set-Cookie`).
     *
     * @param name the name of the header
     * @return the first value assigned to this header
     */
    fun getHeader(name: String?): String? {
        for (headerEntry: Map.Entry<String?, List<String>> in responseHeaders.entries) {
            val key: String? = headerEntry.key
            if ((key != null) && key.equals(name, ignoreCase = true) && !headerEntry.value.isEmpty()) {
                return headerEntry.value.get(0)
            }
        }
        return null
    }
}
