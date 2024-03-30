package org.schabi.newpipe.extractor.utils.jsextractor

import org.mozilla.javascript.Context
import org.schabi.newpipe.extractor.exceptions.ParsingException
import java.util.Stack

/**
 * JavaScript lexer that is able to parse JavaScript code and return its
 * tokens.
 *
 *
 *
 * The algorithm for distinguishing between division operators and regex literals
 * was taken from the [RESS lexer](https://github.com/rusty-ecma/RESS/).
 *
 */
class Lexer @JvmOverloads constructor(js: String, languageVersion: Int = Context.VERSION_DEFAULT) {
    private class Paren internal constructor(val funcExpr: Boolean, val conditional: Boolean)
    private class Brace internal constructor(val isBlock: Boolean, val paren: Paren?)
    private open class MetaToken internal constructor(val token: Token?, val lineno: Int)
    private class BraceMetaToken internal constructor(token: Token?, lineno: Int, val brace: Brace) : MetaToken(token, lineno)
    private class ParenMetaToken internal constructor(token: Token?, lineno: Int, val paren: Paren) : MetaToken(token, lineno)
    private class LookBehind internal constructor() {
        private val list: Array<MetaToken?>

        init {
            list = arrayOfNulls(3)
        }

        fun push(t: MetaToken?) {
            var toShift = t
            for (i in 0..2) {
                val tmp = list[i]
                list[i] = toShift
                toShift = tmp
            }
        }

        fun one(): MetaToken? {
            return list[0]
        }

        fun two(): MetaToken? {
            return list[1]
        }

        fun three(): MetaToken? {
            return list[2]
        }

        fun oneIs(token: Token): Boolean {
            return list[0] != null && list[0]!!.token == token
        }

        fun twoIs(token: Token): Boolean {
            return list[1] != null && list[1]!!.token == token
        }

        fun threeIs(token: Token): Boolean {
            return list[2] != null && list[2]!!.token == token
        }
    }

    /**
     * Parsed token, containing the token and its position in the input string
     */
    class ParsedToken internal constructor(@JvmField val token: Token?, @JvmField val start: Int, @JvmField val end: Int)

    private val stream: TokenStream
    private val lastThree: LookBehind
    private val braceStack: Stack<Brace>
    private val parenStack: Stack<Paren>
    /**
     * Create a new JavaScript lexer with the given source code
     *
     * @param js JavaScript code
     * @param languageVersion JavaScript version (from Rhino)
     */
    /**
     * Create a new JavaScript lexer with the given source code
     *
     * @param js JavaScript code
     */
    init {
        stream = TokenStream(js, 0, languageVersion)
        lastThree = LookBehind()
        braceStack = Stack()
        parenStack = Stack()
    }

    @get:Throws(ParsingException::class)
    val nextToken: ParsedToken
        /**
         * Continue parsing and return the next token
         * @return next token
         * @throws ParsingException
         */
        get() {
            var token = stream.nextToken()
            if ((token == Token.DIV || token == Token.ASSIGN_DIV) && isRegexStart) {
                stream.readRegExp(token)
                token = Token.REGEXP
            }
            val parsedToken = ParsedToken(token, stream.tokenBeg, stream.tokenEnd)
            keepBooks(parsedToken)
            return parsedToken
        }
    val isBalanced: Boolean
        /**
         * Check if the parser is balanced (equal amount of open and closed parentheses and braces)
         * @return true if balanced
         */
        get() = braceStack.isEmpty() && parenStack.isEmpty()

    /**
     * Evaluate the token for possible regex start and handle updating the
     * `self.last_three`, `self.paren_stack` and `self.brace_stack`
     */
    @Throws(ParsingException::class)
    fun keepBooks(parsedToken: ParsedToken) {
        if (parsedToken.token!!.isPunct) {
            when (parsedToken.token) {
                Token.LP -> {
                    handleOpenParenBooks()
                    return
                }

                Token.LC -> {
                    handleOpenBraceBooks()
                    return
                }

                Token.RP -> {
                    handleCloseParenBooks(parsedToken.start)
                    return
                }

                Token.RC -> {
                    handleCloseBraceBooks(parsedToken.start)
                    return
                }
            }
        }
        if (parsedToken.token != Token.COMMENT) {
            lastThree.push(MetaToken(parsedToken.token, stream.lineno))
        }
    }

    /**
     * Handle the book keeping when we find an `(`
     */
    fun handleOpenParenBooks() {
        var funcExpr = false
        if (lastThree.oneIs(Token.FUNCTION)) {
            funcExpr = lastThree.two() != null && checkForExpression(lastThree.two()!!.token)
        } else if (lastThree.twoIs(Token.FUNCTION)) {
            funcExpr = lastThree.three() != null && checkForExpression(lastThree.three()!!.token)
        }
        val conditional = (lastThree.one() != null
                && lastThree.one()!!.token!!.isConditional)
        val paren = Paren(funcExpr, conditional)
        parenStack.push(paren)
        lastThree.push(ParenMetaToken(Token.LP, stream.lineno, paren))
    }

    /**
     * Handle the book keeping when we find an `{`
     */
    fun handleOpenBraceBooks() {
        var isBlock = true
        if (lastThree.one() != null) {
            isBlock = when (lastThree.one()!!.token) {
                Token.LP, Token.LC, Token.CASE -> false
                Token.COLON -> !braceStack.isEmpty() && braceStack.lastElement().isBlock
                Token.RETURN, Token.YIELD, Token.YIELD_STAR -> lastThree.two() != null && lastThree.two()!!.lineno != stream.lineno
                else -> !lastThree.one()!!.token!!.isOp
            }
        }
        var paren: Paren? = null
        if (lastThree.one() is ParenMetaToken && lastThree.one().token == Token.RP) {
            paren = (lastThree.one() as ParenMetaToken?)!!.paren
        }
        val brace = Brace(isBlock, paren)
        braceStack.push(brace)
        lastThree.push(BraceMetaToken(Token.LC, stream.lineno, brace))
    }

    /**
     * Handle the book keeping when we find an `)`
     */
    @Throws(ParsingException::class)
    fun handleCloseParenBooks(start: Int) {
        if (parenStack.isEmpty()) {
            throw ParsingException("unmached closing paren at $start")
        }
        lastThree.push(ParenMetaToken(Token.RP, stream.lineno, parenStack.pop()))
    }

    /**
     * Handle the book keeping when we find an `}`
     */
    @Throws(ParsingException::class)
    fun handleCloseBraceBooks(start: Int) {
        if (braceStack.isEmpty()) {
            throw ParsingException("unmatched closing brace at $start")
        }
        lastThree.push(BraceMetaToken(Token.RC, stream.lineno, braceStack.pop()))
    }

    fun checkForExpression(token: Token?): Boolean {
        return token!!.isOp || token == Token.RETURN || token == Token.CASE
    }

    val isRegexStart: Boolean
        /**
         * Detect if the `/` is the beginning of a regex or is division
         * [see this for more details](https://github.com/sweet-js/sweet-core/wiki/design)
         *
         * @return isRegexStart
         */
        get() {
            if (lastThree.one() != null) {
                val t = lastThree.one()!!.token
                return if (t!!.isKeyw) {
                    t != Token.THIS
                } else if (t == Token.RP && lastThree.one() is ParenMetaToken) {
                    (lastThree.one() as ParenMetaToken?)!!.paren.conditional
                } else if (t == Token.RC && lastThree.one() is BraceMetaToken) {
                    val mt = lastThree.one() as BraceMetaToken?
                    if (mt!!.brace.isBlock) {
                        if (mt.brace.paren != null) {
                            !mt.brace.paren.funcExpr
                        } else {
                            true
                        }
                    } else {
                        false
                    }
                } else if (t.isPunct) {
                    t != Token.RB
                } else {
                    false
                }
            }
            return true
        }
}
