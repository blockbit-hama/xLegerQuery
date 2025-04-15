/*
 *
 * Copyright OpusM Corp. All Rights Reserved.
 *
 * Written by HAMA
 *
 */

package blockbit.sql.lexer

import java.util.regex.*

class RegexToken(private val id: String) : Token {

    private var matcher: Matcher? = null
    private val pattern: Pattern

    override fun match(input: String, offset: Int): Boolean {
        matcher = pattern.matcher(input.substring(offset))
        return matcher!!.lookingAt()
    }

    override fun lexeme(): String {
        return matcher!!.group()
    }

    override fun toString(): String {
        return id
    }

    init {
        pattern = Pattern.compile(id, Pattern.CASE_INSENSITIVE)
    }
}