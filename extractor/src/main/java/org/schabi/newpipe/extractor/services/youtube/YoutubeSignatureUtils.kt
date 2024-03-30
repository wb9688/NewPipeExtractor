package org.schabi.newpipe.extractor.services.youtube

import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.JavaScript
import org.schabi.newpipe.extractor.utils.Parser
import org.schabi.newpipe.extractor.utils.Parser.RegexException
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor
import java.util.regex.Pattern

/**
 * Utility class to get the signature timestamp of YouTube's base JavaScript player and deobfuscate
 * signature of streaming URLs from HTML5 clients.
 */
internal object YoutubeSignatureUtils {
    /**
     * The name of the deobfuscation function which needs to be called inside the deobfuscation
     * code.
     */
    val DEOBFUSCATION_FUNCTION_NAME: String = "deobfuscate"
    private val FUNCTION_REGEXES: Array<String> = arrayOf(
            "\\bm=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(h\\.s\\)\\)",
            "\\bc&&\\(c=([a-zA-Z0-9$]{2,})\\(decodeURIComponent\\(c\\)\\)",  // CHECKSTYLE:OFF
            "(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2,})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",  // CHECKSTYLE:ON
            "([\\w$]+)\\s*=\\s*function\\((\\w+)\\)\\{\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;"
    )
    private val STS_REGEX: String = "signatureTimestamp[=:](\\d+)"
    private val DEOBF_FUNC_REGEX_START: String = "("
    private val DEOBF_FUNC_REGEX_END: String = "=function\\([a-zA-Z0-9_]+\\)\\{.+?\\})"
    private val SIG_DEOBF_HELPER_OBJ_NAME_REGEX: String = ";([A-Za-z0-9_\\$]{2,})\\...\\("
    private val SIG_DEOBF_HELPER_OBJ_REGEX_START: String = "(var "
    private val SIG_DEOBF_HELPER_OBJ_REGEX_END: String = "=\\{(?>.|\\n)+?\\}\\};)"

    /**
     * Get the signature timestamp property of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the signature timestamp
     * @throws ParsingException if the signature timestamp couldn't be extracted
     */
    @Throws(ParsingException::class)
    fun getSignatureTimestamp(javaScriptPlayerCode: String?): String? {
        try {
            return Parser.matchGroup1(STS_REGEX, javaScriptPlayerCode)
        } catch (e: ParsingException) {
            throw ParsingException(
                    "Could not extract signature timestamp from JavaScript code", e)
        }
    }

    /**
     * Get the signature deobfuscation code of YouTube's base JavaScript file.
     *
     * @param javaScriptPlayerCode the complete JavaScript base player code
     * @return the signature deobfuscation code
     * @throws ParsingException if the signature deobfuscation code couldn't be extracted
     */
    @Throws(ParsingException::class)
    fun getDeobfuscationCode(javaScriptPlayerCode: String?): String {
        try {
            val deobfuscationFunctionName: String? = getDeobfuscationFunctionName(
                    javaScriptPlayerCode)
            var deobfuscationFunction: String
            try {
                deobfuscationFunction = getDeobfuscateFunctionWithLexer(
                        javaScriptPlayerCode, deobfuscationFunctionName)
            } catch (e: Exception) {
                deobfuscationFunction = getDeobfuscateFunctionWithRegex(
                        javaScriptPlayerCode, deobfuscationFunctionName)
            }

            // Assert the extracted deobfuscation function is valid
            JavaScript.compileOrThrow(deobfuscationFunction)
            val helperObjectName: String? = Parser.matchGroup1(SIG_DEOBF_HELPER_OBJ_NAME_REGEX, deobfuscationFunction)
            val helperObject: String = getHelperObject(javaScriptPlayerCode, helperObjectName)
            val callerFunction: String = ("function " + DEOBFUSCATION_FUNCTION_NAME
                    + "(a){return "
                    + deobfuscationFunctionName
                    + "(a);}")
            return helperObject + deobfuscationFunction + ";" + callerFunction
        } catch (e: Exception) {
            throw ParsingException("Could not parse deobfuscation function", e)
        }
    }

    @Throws(ParsingException::class)
    private fun getDeobfuscationFunctionName(javaScriptPlayerCode: String?): String? {
        var exception: RegexException? = null
        for (regex: String in FUNCTION_REGEXES) {
            try {
                return Parser.matchGroup1(regex, javaScriptPlayerCode)
            } catch (e: RegexException) {
                if (exception == null) {
                    exception = e
                }
            }
        }
        throw ParsingException(
                "Could not find deobfuscation function with any of the known patterns", exception)
    }

    @Throws(ParsingException::class)
    private fun getDeobfuscateFunctionWithLexer(
            javaScriptPlayerCode: String?,
            deobfuscationFunctionName: String?): String {
        val functionBase: String = deobfuscationFunctionName + "=function"
        return functionBase + JavaScriptExtractor.matchToClosingBrace(
                javaScriptPlayerCode, functionBase)
    }

    @Throws(ParsingException::class)
    private fun getDeobfuscateFunctionWithRegex(
            javaScriptPlayerCode: String?,
            deobfuscationFunctionName: String?): String {
        val functionPattern: String = (DEOBF_FUNC_REGEX_START
                + Pattern.quote(deobfuscationFunctionName)
                + DEOBF_FUNC_REGEX_END)
        return "var " + Parser.matchGroup1(functionPattern, javaScriptPlayerCode)
    }

    @Throws(ParsingException::class)
    private fun getHelperObject(javaScriptPlayerCode: String?,
                                helperObjectName: String?): String {
        val helperPattern: String = (SIG_DEOBF_HELPER_OBJ_REGEX_START
                + Pattern.quote(helperObjectName)
                + SIG_DEOBF_HELPER_OBJ_REGEX_END)
        return Parser.matchGroup1(helperPattern, javaScriptPlayerCode)
                .replace("\n", "")
    }
}
