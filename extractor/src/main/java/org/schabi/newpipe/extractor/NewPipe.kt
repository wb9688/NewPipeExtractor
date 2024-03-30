package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.StreamingService.LinkType
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.exceptions.ExtractionException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import java.util.function.Predicate
import java.util.function.Supplier

/*
* Created by Christian Schabesberger on 23.08.15.
*
* Copyright (C) 2015 Christian Schabesberger <chris.schabesberger@mailbox.org>
* NewPipe Extractor.java is part of NewPipe Extractor.
*
* NewPipe Extractor is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* NewPipe Extractor is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
*/ /**
 * Provides access to streaming services supported by NewPipe.
 */
object NewPipe {
    var downloader: Downloader? = null
        private set
    private var preferredLocalization: Localization? = null
    private var preferredContentCountry: ContentCountry? = null
    @JvmOverloads
    fun init(d: Downloader?, l: Localization = Localization.Companion.DEFAULT, c: ContentCountry? = if (l.getCountryCode().isEmpty()) ContentCountry.Companion.DEFAULT else ContentCountry(l.getCountryCode())) {
        downloader = d
        preferredLocalization = l
        preferredContentCountry = c
    }

    @JvmStatic
    val services: List<StreamingService?>?
        /*//////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////// */
        get() {
            return ServiceList.all()
        }

    @JvmStatic
    @Throws(ExtractionException::class)
    fun getService(serviceId: Int): StreamingService? {
        return ServiceList.all().stream()
                .filter(Predicate({ service: StreamingService? -> service.getServiceId() == serviceId }))
                .findFirst()
                .orElseThrow(Supplier({
                    ExtractionException(
                            "There's no service with the id = \"" + serviceId + "\"")
                }))
    }

    @Throws(ExtractionException::class)
    fun getService(serviceName: String): StreamingService? {
        return ServiceList.all().stream()
                .filter(Predicate({ service: StreamingService? -> (service.getServiceInfo().getName() == serviceName) }))
                .findFirst()
                .orElseThrow(Supplier({
                    ExtractionException(
                            "There's no service with the name = \"" + serviceName + "\"")
                }))
    }

    @JvmStatic
    @Throws(ExtractionException::class)
    fun getServiceByUrl(url: String): StreamingService? {
        for (service: StreamingService? in ServiceList.all()) {
            if (service!!.getLinkTypeByUrl(url) != LinkType.NONE) {
                return service
            }
        }
        throw ExtractionException("No service can handle the url = \"" + url + "\"")
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    ////////////////////////////////////////////////////////////////////////// */
    @JvmOverloads
    fun setupLocalization(
            thePreferredLocalization: Localization,
            thePreferredContentCountry: ContentCountry? = null) {
        preferredLocalization = thePreferredLocalization
        if (thePreferredContentCountry != null) {
            preferredContentCountry = thePreferredContentCountry
        } else {
            preferredContentCountry = if (thePreferredLocalization.getCountryCode().isEmpty()) ContentCountry.Companion.DEFAULT else ContentCountry(thePreferredLocalization.getCountryCode())
        }
    }

    @JvmStatic
    @Nonnull
    fun getPreferredLocalization(): Localization {
        return if (preferredLocalization == null) Localization.Companion.DEFAULT else preferredLocalization!!
    }

    fun setPreferredLocalization(preferredLocalization: Localization?) {
        NewPipe.preferredLocalization = preferredLocalization
    }

    @JvmStatic
    @Nonnull
    fun getPreferredContentCountry(): ContentCountry {
        return if (preferredContentCountry == null) ContentCountry.Companion.DEFAULT else preferredContentCountry!!
    }

    fun setPreferredContentCountry(preferredContentCountry: ContentCountry?) {
        NewPipe.preferredContentCountry = preferredContentCountry
    }
}
