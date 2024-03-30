package org.schabi.newpipe.extractor.kiosk

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.utils.Utils
import java.io.IOException

class KioskList(private val service: StreamingService) {
    interface KioskExtractorFactory {
        @Throws(ExtractionException::class, IOException::class)
        fun createNewKiosk(streamingService: StreamingService?,
                           url: String?,
                           kioskId: String?): KioskExtractor<*>
    }

    private val kioskList = HashMap<String?, KioskEntry?>()
    var defaultKioskId: String? = null
        private set
    private var forcedLocalization: Localization? = null
    private var forcedContentCountry: ContentCountry? = null

    private class KioskEntry internal constructor(val extractorFactory: KioskExtractorFactory, val handlerFactory: ListLinkHandlerFactory)

    @Throws(Exception::class)
    fun addKioskEntry(extractorFactory: KioskExtractorFactory,
                      handlerFactory: ListLinkHandlerFactory,
                      id: String) {
        if (kioskList[id] != null) {
            throw Exception("Kiosk with type $id already exists.")
        }
        kioskList[id] = KioskEntry(extractorFactory, handlerFactory)
    }

    fun setDefaultKiosk(kioskType: String?) {
        defaultKioskId = kioskType
    }

    @get:Throws(ExtractionException::class, IOException::class)
    val defaultKioskExtractor: KioskExtractor<*>?
        get() = getDefaultKioskExtractor(null)

    @Throws(ExtractionException::class, IOException::class)
    fun getDefaultKioskExtractor(nextPage: Page?): KioskExtractor<*>? {
        return getDefaultKioskExtractor(nextPage, NewPipe.getPreferredLocalization())
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getDefaultKioskExtractor(nextPage: Page?,
                                 localization: Localization?): KioskExtractor<*>? {
        return if (!Utils.isNullOrEmpty(defaultKioskId)) {
            getExtractorById(defaultKioskId, nextPage, localization)
        } else {
            val first = kioskList.keys.stream().findAny().orElse(null)
            first?.let { getExtractorById(it, nextPage, localization) }
        }
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorById(kioskId: String?, nextPage: Page?): KioskExtractor<*> {
        return getExtractorById(kioskId, nextPage, NewPipe.getPreferredLocalization())
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorById(kioskId: String?,
                         nextPage: Page?,
                         localization: Localization?): KioskExtractor<*> {
        val ke = kioskList[kioskId]
        return if (ke == null) {
            throw ExtractionException("No kiosk found with the type: $kioskId")
        } else {
            val kioskExtractor = ke.extractorFactory.createNewKiosk(service,
                    ke.handlerFactory.fromId(kioskId).url, kioskId)
            if (forcedLocalization != null) {
                kioskExtractor.forceLocalization(forcedLocalization)
            }
            if (forcedContentCountry != null) {
                kioskExtractor.forceContentCountry(forcedContentCountry)
            }
            kioskExtractor
        }
    }

    val availableKiosks: Set<String?>
        get() = kioskList.keys

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorByUrl(url: String, nextPage: Page?): KioskExtractor<*> {
        return getExtractorByUrl(url, nextPage, NewPipe.getPreferredLocalization())
    }

    @Throws(ExtractionException::class, IOException::class)
    fun getExtractorByUrl(url: String,
                          nextPage: Page?,
                          localization: Localization?): KioskExtractor<*> {
        for ((_, ke) in kioskList) {
            if (ke!!.handlerFactory.acceptUrl(url)) {
                return getExtractorById(ke.handlerFactory.getId(url), nextPage, localization)
            }
        }
        throw ExtractionException("Could not find a kiosk that fits to the url: $url")
    }

    fun getListLinkHandlerFactoryByType(type: String?): ListLinkHandlerFactory {
        return kioskList[type]!!.handlerFactory
    }

    fun forceLocalization(localization: Localization?) {
        forcedLocalization = localization
    }

    fun forceContentCountry(contentCountry: ContentCountry?) {
        forcedContentCountry = contentCountry
    }
}
