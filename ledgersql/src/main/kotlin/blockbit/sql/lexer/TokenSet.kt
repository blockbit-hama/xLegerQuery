 /** Written by HAMA
 */

package blockbit.sql.lexer

import java.util.regex.Pattern
import kotlin.collections.Iterator

class TokenSet {
    private val members = mutableListOf<Token>()

    operator fun iterator(): Iterator<Token> {
        return members.iterator()
    }

    fun create(spec: String): Token {
        val token: Token
        var start = 1
        if (!spec.startsWith("'")) {
            if (containsRegexMetacharacters(spec)) {
                token = RegexToken(spec)
                members.add(token)
                return token
            }
            --start
        }
        var end: Int = spec.length
        if (start == 1 && spec.endsWith("'")) // saw leading '
            --end
        token = if (Character.isJavaIdentifierPart(spec.get(end - 1))) {
            WordToken(spec.substring(start, end))
        }
        else {
            (SimpleToken(spec.substring(start, end)) as Token?)!!
        }
        members.add(token)
        return token
    }

    companion object {
        private fun containsRegexMetacharacters(s: String): Boolean {
            val m = metacharacters.matcher(s)
            return m.find()
        }

        private val metacharacters = Pattern.compile("[\\\\\\[\\]{}$\\^*+?|()]")
    }
}