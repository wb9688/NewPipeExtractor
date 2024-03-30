package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector
import java.util.Collections

/*
* Created by Christian Schabesberger on 12.02.17.
*
* Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
* InfoItemsSearchCollector.java is part of NewPipe Extractor.
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
/**
 * A collector that can handle many extractor types, to be used when a list contains items of
 * different types (e.g. search)
 *
 *
 * This collector can handle the following extractor types:
 *
 *  * [StreamInfoItemExtractor]
 *  * [ChannelInfoItemExtractor]
 *  * [PlaylistInfoItemExtractor]
 *
 * Calling [.extract] or [.commit] with any
 * other extractor type will raise an exception.
 */
class MultiInfoItemsCollector(serviceId: Int) : InfoItemsCollector<InfoItem?, InfoItemExtractor?>(serviceId) {
    private val streamCollector: StreamInfoItemsCollector
    private val userCollector: ChannelInfoItemsCollector
    private val playlistCollector: PlaylistInfoItemsCollector

    init {
        streamCollector = StreamInfoItemsCollector(serviceId)
        userCollector = ChannelInfoItemsCollector(serviceId)
        playlistCollector = PlaylistInfoItemsCollector(serviceId)
    }

    public override fun getErrors(): List<Throwable?> {
        val errors: MutableList<Throwable?> = ArrayList(super.getErrors())
        errors.addAll(streamCollector.getErrors())
        errors.addAll(userCollector.getErrors())
        errors.addAll(playlistCollector.getErrors())
        return Collections.unmodifiableList(errors)
    }

    public override fun reset() {
        super.reset()
        streamCollector.reset()
        userCollector.reset()
        playlistCollector.reset()
    }

    @Throws(ParsingException::class)
    public override fun extract(extractor: InfoItemExtractor): InfoItem? {
        // Use the corresponding collector for each item extractor type
        if (extractor is StreamInfoItemExtractor) {
            return streamCollector.extract(extractor)
        } else if (extractor is ChannelInfoItemExtractor) {
            return userCollector.extract(extractor)
        } else if (extractor is PlaylistInfoItemExtractor) {
            return playlistCollector.extract(extractor)
        } else {
            throw IllegalArgumentException("Invalid extractor type: " + extractor)
        }
    }
}
