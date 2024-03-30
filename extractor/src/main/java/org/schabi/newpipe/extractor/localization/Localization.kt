package org.schabi.newpipe.extractor.localization

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.LocaleCompat
import java.io.Serializable
import java.util.Collections
import java.util.Locale
import java.util.Objects
import java.util.Optional
import java.util.function.Function
import java.util.function.Supplier

class Localization @JvmOverloads constructor(@field:Nonnull @get:Nonnull
                                             @param:Nonnull val languageCode: String?, private val countryCode: String? = null) : Serializable {
    @Nonnull
    fun getCountryCode(): String {
        return if (countryCode == null) "" else countryCode
    }

    val localizationCode: String
        /**
         * Return a formatted string in the form of: `language-Country`, or
         * just `language` if country is `null`.
         *
         * @return A correctly formatted localizationCode for this localization.
         */
        get() {
            return languageCode + (if (countryCode == null) "" else "-" + countryCode)
        }

    public override fun toString(): String {
        return "Localization[" + localizationCode + "]"
    }

    public override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (!(o is Localization)) {
            return false
        }
        val that: Localization = o
        return ((languageCode == that.languageCode) && Objects.equals(countryCode, that.countryCode))
    }

    public override fun hashCode(): Int {
        var result: Int = languageCode.hashCode()
        result = 31 * result + Objects.hashCode(countryCode)
        return result
    }

    companion object {
        @JvmField
        val DEFAULT: Localization = Localization("en", "GB")

        /**
         * @param localizationCodeList a list of localization code, formatted like [                             ][.getLocalizationCode]
         * @throws IllegalArgumentException If any of the localizationCodeList is formatted incorrectly
         * @return list of Localization objects
         */
        @Nonnull
        fun listFrom(vararg localizationCodeList: String): List<Localization?> {
            val toReturn: MutableList<Localization?> = ArrayList()
            for (localizationCode: String in localizationCodeList) {
                toReturn.add(fromLocalizationCode(localizationCode)
                        .orElseThrow(Supplier({
                            IllegalArgumentException(
                                    "Not a localization code: " + localizationCode
                            )
                        })))
            }
            return Collections.unmodifiableList(toReturn)
        }

        /**
         * @param localizationCode a localization code, formatted like [.getLocalizationCode]
         * @return A Localization, if the code was valid.
         */
        @Nonnull
        fun fromLocalizationCode(localizationCode: String): Optional<Localization> {
            return LocaleCompat.forLanguageTag(localizationCode).map(Function({ locale: Locale? -> fromLocale(locale) }))
        }

        @JvmStatic
        fun fromLocale(locale: Locale?): Localization {
            return Localization(locale!!.getLanguage(), locale.getCountry())
        }

        /**
         * Converts a three letter language code (ISO 639-2/T) to a Locale
         * because limits of Java Locale class.
         *
         * @param code a three letter language code
         * @return the Locale corresponding
         */
        @Throws(ParsingException::class)
        fun getLocaleFromThreeLetterCode(code: String): Locale? {
            val languages: Array<String> = Locale.getISOLanguages()
            val localeMap: MutableMap<String, Locale> = HashMap(languages.size)
            for (language: String? in languages) {
                val locale: Locale = Locale(language)
                localeMap.put(locale.getISO3Language(), locale)
            }
            if (localeMap.containsKey(code)) {
                return localeMap.get(code)
            } else {
                throw ParsingException(
                        "Could not get Locale from this three letter language code" + code)
            }
        }
    }
}
