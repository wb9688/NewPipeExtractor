package org.schabi.newpipe.extractor.localization

import java.io.Serializable
import java.util.Collections

/**
 * Represents a country that should be used when fetching content.
 *
 *
 * YouTube, for example, give different results in their feed depending on which country is
 * selected.
 *
 */
class ContentCountry(@field:Nonnull @get:Nonnull
                     @param:Nonnull val countryCode: String) : Serializable {

    public override fun toString(): String {
        return countryCode
    }

    public override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (!(o is ContentCountry)) {
            return false
        }
        return (countryCode == o.countryCode)
    }

    public override fun hashCode(): Int {
        return countryCode.hashCode()
    }

    companion object {
        val DEFAULT: ContentCountry = ContentCountry(Localization.Companion.DEFAULT.getCountryCode())
        fun listFrom(vararg countryCodeList: String): List<ContentCountry?> {
            val toReturn: MutableList<ContentCountry?> = ArrayList()
            for (countryCode: String in countryCodeList) {
                toReturn.add(ContentCountry(countryCode))
            }
            return Collections.unmodifiableList(toReturn)
        }
    }
}
