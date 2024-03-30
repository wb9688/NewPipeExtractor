package org.schabi.newpipe.extractor.utils

import java.util.Locale
import java.util.Optional

/**
 * This class contains a simple implementation of [Locale.forLanguageTag] for Android
 * API levels below 21 (Lollipop). This is needed as core library desugaring does not backport that
 * method as of this writing.
 * <br></br>
 * Relevant issue: https://issuetracker.google.com/issues/171182330
 */
object LocaleCompat {
    // Source: The AndroidX LocaleListCompat class's private forLanguageTagCompat() method.
    // Use Locale.forLanguageTag() on Android API level >= 21 / Java instead.
    fun forLanguageTag(str: String): Optional<Locale?> {
        if (str.contains("-")) {
            val args = str.split("-".toRegex()).toTypedArray()
            if (args.size > 2) {
                return Optional.of(Locale(args[0], args[1], args[2]))
            } else if (args.size > 1) {
                return Optional.of(Locale(args[0], args[1]))
            } else if (args.size == 1) {
                return Optional.of(Locale(args[0]))
            }
        } else if (str.contains("_")) {
            val args = str.split("_".toRegex()).toTypedArray()
            if (args.size > 2) {
                return Optional.of(Locale(args[0], args[1], args[2]))
            } else if (args.size > 1) {
                return Optional.of(Locale(args[0], args[1]))
            } else if (args.size == 1) {
                return Optional.of(Locale(args[0]))
            }
        } else {
            return Optional.of(Locale(str))
        }
        return Optional.empty()
    }
}
