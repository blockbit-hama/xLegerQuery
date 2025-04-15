 /** Written by HAMA
 */

package blockbit.sql.lexer

interface Token {
    fun match(input: String, offset: Int): Boolean
    fun lexeme(): String?
}