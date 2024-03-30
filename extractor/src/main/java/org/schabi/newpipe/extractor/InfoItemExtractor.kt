package org.schabi.newpipe.extractor

import org.schabi.newpipe.extractor.exceptions.ParsingException

open interface InfoItemExtractor {
    @get:Throws(ParsingException::class)
    val name: String?

    @get:Throws(ParsingException::class)
    val url: String?

    @get:Throws(ParsingException::class)
    val thumbnails: List<Image?>?
}
