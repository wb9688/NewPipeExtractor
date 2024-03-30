package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.LinkHandler
import java.io.Serializable

abstract class Info(val serviceId: Int,
                    /**
                     * Id of this Info object <br></br>
                     * e.g. Youtube:  https://www.youtube.com/watch?v=RER5qCTzZ7     &gt;    RER5qCTzZ7
                     */
                    @JvmField val id: String?,
                    /**
                     * Different than the [.originalUrl] in the sense that it *may* be set as a cleaned
                     * url.
                     *
                     * @see LinkHandler.getUrl
                     * @see Extractor.getOriginalUrl
                     */
                    val url: String?,
                    /**
                     * The url used to start the extraction of this [Info] object.
                     *
                     * @see Extractor.getOriginalUrl
                     */
                    var originalUrl: String?,
        // if you use an api and want to handle the website url
        // overriding original url is essential
                    @JvmField val name: String?) : Serializable {

    private val errors: MutableList<Throwable?> = ArrayList()
    fun addError(throwable: Throwable?) {
        errors.add(throwable)
    }

    fun addAllErrors(throwables: Collection<Throwable?>?) {
        errors.addAll((throwables)!!)
    }

    constructor(serviceId: Int, linkHandler: LinkHandler?, name: String?) : this(serviceId,
            linkHandler.getId(),
            linkHandler.getUrl(),
            linkHandler.getOriginalUrl(),
            name)

    public override fun toString(): String {
        val ifDifferentString: String = if ((url == originalUrl)) "" else " (originalUrl=\"" + originalUrl + "\")"
        return (javaClass.getSimpleName() + "[url=\"" + url + "\"" + ifDifferentString
                + ", name=\"" + name + "\"]")
    }

    val service: StreamingService?
        get() {
            try {
                return NewPipe.getService(serviceId)
            } catch (e: ExtractionException) {
                // this should be unreachable, as serviceId certainly refers to a valid service
                throw RuntimeException("Info object has invalid service id", e)
            }
        }

    fun getErrors(): List<Throwable?> {
        return errors
    }
}
