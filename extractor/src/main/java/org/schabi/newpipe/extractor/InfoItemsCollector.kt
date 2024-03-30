package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.FoundAdException
import org.schabi.newpipe.extractor.exceptions.ParsingException
import java.util.Collections

/*
* Created by Christian Schabesberger on 12.02.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* InfoItemsCollector.java is part of NewPipe Extractor.
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
*/
abstract class InfoItemsCollector<I : InfoItem?, E : InfoItemExtractor?>
/**
 * Create a new collector with no comparator / sorting function
 * @param serviceId the service id
 */ @JvmOverloads constructor(
        /**
         * Get the service id
         * @return the service id
         */
        val serviceId: Int, private val comparator: Comparator<I>? = null) : Collector<I?, E> {
    private val itemList: MutableList<I?> = ArrayList()
    private override val errors: MutableList<Throwable?> = ArrayList()

    /**
     * Create a new collector
     * @param serviceId the service id
     */
    override val items: List<I>
        get() {
            if (comparator != null) {
                itemList.sort(comparator)
            }
            return Collections.unmodifiableList(itemList)
        }

    public override fun getErrors(): List<Throwable?> {
        return Collections.unmodifiableList(errors)
    }

    public override fun reset() {
        itemList.clear()
        errors.clear()
    }

    /**
     * Add an error
     * @param error the error
     */
    protected fun addError(error: Exception?) {
        errors.add(error)
    }

    /**
     * Add an item
     * @param item the item
     */
    protected fun addItem(item: I?) {
        itemList.add(item)
    }

    public override fun commit(extractor: E) {
        try {
            addItem(extract(extractor))
        } catch (ae: FoundAdException) {
            // found an ad. Maybe a debug line could be placed here
        } catch (e: ParsingException) {
            addError(e)
        }
    }
}