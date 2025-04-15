 /** Written by HAMA
 */

package blockbit.sql.lexer

class SimpleToken(pattern: String) : Token {
    private val pattern: String

    init {
        this.pattern = pattern.toLowerCase()
    }

    override fun match(input: String, offset: Int): Boolean {
        return input.toLowerCase().startsWith(pattern, offset)
    }

    override fun lexeme(): String {
        return pattern
    }

    override fun toString(): String {
        return pattern
    }
}