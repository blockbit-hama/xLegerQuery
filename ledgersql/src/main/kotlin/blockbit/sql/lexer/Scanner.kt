 /** Written by HAMA
 */

package blockbit.sql.lexer

import blockbit.sql.parser.exceptions.ParsingException
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader

class Scanner(tokens: TokenSet?, inputReader: Reader?) {
    private var currentToken: Token? = BeginToken()
    private var inputReader: BufferedReader? = null
    private var inputLineNumber = 0
    private var inputLine: String? = null
    private var inputPosition = 0
    private var tokens: TokenSet? = null

    constructor(tokens: TokenSet?, input: String?) : this(tokens, StringReader(input)) {}

    init {
        this.tokens = tokens
        this.inputReader = if (inputReader is BufferedReader)
                            inputReader  else BufferedReader(inputReader)
        loadLine()
    }

    private fun loadLine(): Boolean {
        return try {
            inputLine = inputReader?.readLine()
            if (inputLine != null) {
                ++inputLineNumber
                inputPosition = 0
            }
            inputLine != null
        } catch (e: IOException) {
            false
        }
    }

    fun match(candidate: Token): Boolean {
        return currentToken === candidate
    }

    @Throws(ParsingException::class)
    fun advance(): Token? {
        try {
            if (currentToken != null) {
                inputPosition += currentToken?.lexeme()?.length!!
                currentToken = null
                if (inputPosition == inputLine!!.length) if (!loadLine()) return null

                while (Character.isWhitespace(inputLine!!.get(inputPosition))) {
                    if (++inputPosition == inputLine!!.length)
                        if (!loadLine()) return null

                }
                val i = tokens?.iterator()
                while (i!!.hasNext()) {
                    val t: Token = i.next()
                    if (t.match(inputLine!!, inputPosition)) {
                        currentToken = t
                        break
                    }
                }
                if (currentToken == null) throw failure("Unrecognized Input")
            }
        } catch (e: IndexOutOfBoundsException) {
        }
        return currentToken
    }

    fun failure(message: String?): ParsingException {
        return ParsingException(message, inputLine!!, inputPosition, inputLineNumber)
    }

    @Throws(ParsingException::class)
    fun matchAdvance(candidate: Token): String? {
        if (match(candidate)) {
            val lexeme: String? = currentToken?.lexeme()
            advance()
            return lexeme
        }
        return null
    }

    @Throws(ParsingException::class)
    fun required(candidate: Token): String {
        return matchAdvance(candidate) ?: throw failure("\"" + candidate.toString().toString() + "\" expected.")
    }

}