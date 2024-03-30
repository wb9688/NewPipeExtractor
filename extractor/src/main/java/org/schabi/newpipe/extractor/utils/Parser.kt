/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) 2016 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * Parser.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */
package org.schabi.newpipe.extractor.utils

import org.schabi.newpipe.extractor.exceptions.ParsingException
import java.io.UnsupportedEncodingException
import java.util.regex.Pattern

/**
 * Avoid using regex !!!
 */
object Parser {
    @Throws(RegexException::class)
    fun matchGroup1(pattern: String?, input: String?): String {
        return matchGroup(pattern, input, 1)
    }

    @Throws(RegexException::class)
    fun matchGroup1(pattern: Pattern,
                    input: String?): String {
        return matchGroup(pattern, input, 1)
    }

    @Throws(RegexException::class)
    fun matchGroup(pattern: String?,
                   input: String?,
                   group: Int): String {
        return matchGroup(Pattern.compile(pattern), input, group)
    }

    @Throws(RegexException::class)
    fun matchGroup(@Nonnull pat: Pattern,
                   input: String?,
                   group: Int): String {
        val matcher = pat.matcher(input)
        val foundMatch = matcher.find()
        return if (foundMatch) {
            matcher.group(group)
        } else {
            // only pass input to exception message when it is not too long
            if (input!!.length > 1024) {
                throw RegexException("Failed to find pattern \"" + pat.pattern() + "\"")
            } else {
                throw RegexException("Failed to find pattern \"" + pat.pattern()
                        + "\" inside of \"" + input + "\"")
            }
        }
    }

    fun isMatch(pattern: String?, input: String?): Boolean {
        val pat = Pattern.compile(pattern)
        val mat = pat.matcher(input)
        return mat.find()
    }

    fun isMatch(@Nonnull pattern: Pattern, input: String?): Boolean {
        val mat = pattern.matcher(input)
        return mat.find()
    }

    @Nonnull
    @Throws(UnsupportedEncodingException::class)
    fun compatParseMap(@Nonnull input: String): Map<String?, String?> {
        val map: MutableMap<String?, String?> = HashMap()
        for (arg in input.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val splitArg = arg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (splitArg.size > 1) {
                map[splitArg[0]] = Utils.decodeUrlUtf8(splitArg[1])
            } else {
                map[splitArg[0]] = ""
            }
        }
        return map
    }

    class RegexException(message: String?) : ParsingException(message)
}
