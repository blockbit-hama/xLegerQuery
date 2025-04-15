 /** Written by HAMA */

package blockbit.sql.lexer

class WordToken(pattern: String) : Token {

    private var pattern: String

    init {
        this.pattern = pattern.toLowerCase()
    }

    override fun match(input: String, offset: Int): Boolean {
        if (input.length - offset < pattern.length) return false
        val candidate: String = input.substring(offset, offset + pattern.length)
        return if (!candidate.equals(pattern, ignoreCase = true)){
                    false
        }
        else {
            input.length - offset === pattern.length || !Character.isLetterOrDigit(input.get(offset + pattern.length))
        }
    }

    override fun lexeme(): String {
        return pattern
    }

    override fun toString(): String {
        return pattern
    }
}