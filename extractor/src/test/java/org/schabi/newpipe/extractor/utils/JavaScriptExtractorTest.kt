package org.schabi.newpipe.extractor.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.schabi.newpipe.FileUtils
import org.schabi.newpipe.extractor.exceptions.ParsingException
import org.schabi.newpipe.extractor.utils.jsextractor.JavaScriptExtractor.matchToClosingBrace
import org.schabi.newpipe.extractor.utils.jsextractor.Lexer
import org.schabi.newpipe.extractor.utils.jsextractor.Lexer.ParsedToken
import org.schabi.newpipe.extractor.utils.jsextractor.Token
import java.io.IOException
import java.nio.file.Files
import kotlin.math.max

class JavaScriptExtractorTest {
    @Test
    @Throws(ParsingException::class)
    fun testJsExtractor() {
        val src = "Wka=function(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}//some={}random-padding+;"
        val result = matchToClosingBrace(src, "Wka=function")
        Assertions.assertEquals("(d){var x = [/,,/,913,/(,)}/g,\"abcdef}\\\"\",];var y = 10/2/1;return x[1][y];}", result)
    }

    @Test
    @Throws(ParsingException::class, IOException::class)
    fun testEverythingJs() {
        val jsFile = FileUtils.resolveTestResource("es5.js")
        val contentBuilder = StringBuilder()
        Files.lines(jsFile!!.toPath()).forEach { line: String? -> contentBuilder.append(line).append("\n") }
        val js = contentBuilder.toString()
        val lexer = Lexer(js)
        var parsedToken: ParsedToken? = null
        try {
            while (true) {
                parsedToken = lexer.nextToken
                if (parsedToken.token == Token.EOF) {
                    break
                }
            }
        } catch (e: Exception) {
            if (parsedToken != null) {
                throw ParsingException("""
    Issue occured at pos ${parsedToken.end}, after
    ${js.substring(max(0.0, (parsedToken.start - 50).toDouble()).toInt(), parsedToken.end)}
    """.trimIndent(), e)
            }
            throw e
        }
        Assertions.assertTrue(lexer.isBalanced)
    }
}
