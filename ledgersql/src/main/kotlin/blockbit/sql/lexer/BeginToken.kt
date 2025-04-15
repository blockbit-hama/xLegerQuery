/*
 *
 * Copyright OpusM Corp. All Rights Reserved.
 *
 * Written by HAMA
 *
 */

package blockbit.sql.lexer

class BeginToken : Token {
    override fun match(input: String, offset: Int): Boolean {
        return false
    }

    override fun lexeme(): String {
        return ""
    }

    override fun toString(): String {
        return "BeginToken"
    }
}