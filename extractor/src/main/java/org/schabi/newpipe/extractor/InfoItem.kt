/*
 * Created by Christian Schabesberger on 11.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * InfoItem.java is part of NewPipe Extractor.
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor

import java.io.Serializable

abstract class InfoItem(@JvmField val infoType: InfoType,
                        @JvmField val serviceId: Int,
                        @JvmField val url: String?,
                        @JvmField val name: String?) : Serializable {

    @JvmField
    @Nonnull
    var thumbnails: List<Image> = listOf()

    public override fun toString(): String {
        return javaClass.getSimpleName() + "[url=\"" + url + "\", name=\"" + name + "\"]"
    }

    enum class InfoType {
        STREAM,
        PLAYLIST,
        CHANNEL,
        COMMENT
    }
}
