package org.schabi.newpipe.extractor.services.youtube

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.JavaScript
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Utility class to get the throttling parameter decryption code and check if a streaming has the
 * throttling parameter.
 */
internal object YoutubeThrottlingParameterUtils {
    private val THROTTLING_PARAM_PATTERN: Pattern = Pattern.compile("[&?]n=([^&]+)")
    private val DEOBFUSCATION_FUNCTION_NAME_PATTERN: Pattern = Pattern.compile( // CHECKSTYLE:OFF
            "\\.get\\(\"n\"\\)\\)&&\\([a-zA-Z0-9$_]=([a-zA-Z0-9$_]+)(?:\\[(\\d+)])?\\([a-zA-Z0-9$_]\\)")

    // CHECKSTYLE:ON
    // Escape the curly end brace to allow compatibility with Android's regex engine
    // See https://stackoverflow.com/q/45074813
    private val DEOBFUSCATION_FUNCTION_BODY_REGEX: String = "=\\s*function([\\S\\s]*?\\}\\s*return [\\w$]+?\\.join\\(\"\"\\)\\s*\\};)"
    private val DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX: String = "var "
    private val FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX: String = "\\s*=\\s*\\[(.+?)][;,]"

    /**
     * Get the throttling parameter deobfuscation function name of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the name of the throttling parameter deobfuscation function
     * @throws ParsingException if the name of the throttling parameter deobfuscation function
     * could not be extracted
     */
    @Nonnull
    @Throws(ParsingException::class)
    fun getDeobfuscationFunctionName(@Nonnull javaScriptPlayerCode: String?): String {
        val matcher: Matcher = DEOBFUSCATION_FUNCTION_NAME_PATTERN.matcher(javaScriptPlayerCode)
        if (!matcher.find()) {
            throw ParsingException(("Failed to find deobfuscation function name pattern \""
                    + DEOBFUSCATION_FUNCTION_NAME_PATTERN
                    + "\" in the base JavaScript player code"))
        }
        val functionName: String = matcher.group(1)
        if (matcher.groupCount() == 1) {
            return functionName
        }
        val arrayNum: Int = matcher.group(2).toInt()
        val arrayPattern: Pattern = Pattern.compile(
                (DEOBFUSCATION_FUNCTION_ARRAY_OBJECT_TYPE_DECLARATION_REGEX
                        + Pattern.quote(functionName)
                        + FUNCTION_NAMES_IN_DEOBFUSCATION_ARRAY_REGEX))
        val arrayStr: String? = Parser.matchGroup1(arrayPattern, javaScriptPlayerCode)
        val names: Array<String> = arrayStr!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        return names.get(arrayNum)
    }

    /**
     * Get the throttling parameter deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the throttling parameter deobfuscation function name
     * @throws ParsingException if the throttling parameter deobfuscation code couldn't be
     * extracted
     */
    @Nonnull
    @Throws(ParsingException::class)
    fun getDeobfuscationFunction(@Nonnull javaScriptPlayerCode: String?,
                                 @Nonnull functionName: String?): String {
        try {
            return parseFunctionWithLexer(javaScriptPlayerCode, functionName)
        } catch (e: Exception) {
            return parseFunctionWithRegex(javaScriptPlayerCode, functionName)
        }
    }

    /**
     * Get the throttling parameter of a streaming URL if it exists.
     *
     * @param streamingUrl a streaming URL
     * @return the throttling parameter of the streaming URL or `null` if no parameter has
     * been found
     */
    fun getThrottlingParameterFromStreamingUrl(@Nonnull streamingUrl: String?): String? {
        try {
            return Parser.matchGroup1(THROTTLING_PARAM_PATTERN, streamingUrl)
        } catch (e: RegexException) {
            // If the throttling parameter could not be parsed from the URL, it means that there is
            // no throttling parameter
            // Return null in this case
            return null
        }
    }

    @Nonnull
    @Throws(ParsingException::class)
    private fun parseFunctionWithLexer(@Nonnull javaScriptPlayerCode: String?,
                                       @Nonnull functionName: String?): String {
        val functionBase: String = functionName + "=function"
        return functionBase + JavaScriptExtractor.matchToClosingBrace(
                javaScriptPlayerCode, functionBase) + ";"
    }

    @Nonnull
    @Throws(RegexException::class)
    private fun parseFunctionWithRegex(@Nonnull javaScriptPlayerCode: String?,
                                       @Nonnull functionName: String?): String {
        // Quote the function name, as it may contain special regex characters such as dollar
        val functionPattern: Pattern = Pattern.compile(
                Pattern.quote(functionName) + DEOBFUSCATION_FUNCTION_BODY_REGEX,
                Pattern.DOTALL)
        return validateFunction(("function " + functionName
                + Parser.matchGroup1(functionPattern, javaScriptPlayerCode)))
    }

    @Nonnull
    private fun validateFunction(@Nonnull function: String): String {
        JavaScript.compileOrThrow(function)
        return function
    }
}
