package org.schabi.newpipe.extractor.localization

import org.schabi.newpipe.extractor.timeago.PatternsHolder
import org.schabi.newpipe.extractor.timeago.PatternsManager

object TimeAgoPatternsManager {
    private fun getPatternsFor(localization: Localization?): PatternsHolder? {
        return PatternsManager.getPatterns(localization.getLanguageCode(),
                localization!!.getCountryCode())
    }

    @JvmStatic
    fun getTimeAgoParserFor(localization: Localization?): TimeAgoParser? {
        val holder: PatternsHolder? = getPatternsFor(localization)
        if (holder == null) {
            return null
        }
        return TimeAgoParser(holder)
    }
}