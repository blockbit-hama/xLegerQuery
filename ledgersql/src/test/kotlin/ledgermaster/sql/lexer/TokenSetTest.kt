package blockbit.sql.lexer

import blockbit.sql.parser.exceptions.ParsingException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TokenSetTest{
  private val tokens = TokenSet()
  private val COMMA = tokens.create("',")
  private val IN = tokens.create("'IN'")
  private val INPUT = tokens.create("INPUT")
  private val IDENTIFIER = tokens.create("[a-z_][a-z_0-9]*")

  @Test
  fun TokenTest() {
    Assertions.assertTrue(COMMA is SimpleToken) { "COMMA Failure 1" }
    Assertions.assertTrue(IN is WordToken) { "IN Failure 2" }
    Assertions.assertTrue(INPUT is WordToken) { "INPUT Failure 3" }
    Assertions.assertTrue(IDENTIFIER is RegexToken) { "IDENTIFIER Failure 4" }
  }

  @Test
  fun ScannerTest(){
    var analyzer = Scanner(tokens, ",aBc In input inputted")
    Assertions.assertTrue(analyzer.advance() === COMMA) { "COMMA unrecognized" }
    Assertions.assertTrue(analyzer.advance() === IDENTIFIER) { "ID unrecognized" }
    Assertions.assertTrue(analyzer.advance() === IN) { "IN unrecognized" }
    Assertions.assertTrue(analyzer.advance() === INPUT) { "INPUT unrecognized" }
    Assertions.assertTrue(analyzer.advance() === IDENTIFIER) { "ID unrecognized 1" }
    analyzer = Scanner(tokens, "Abc IN\nCde")
    analyzer.advance()
    Assertions.assertTrue(analyzer.matchAdvance(IDENTIFIER) == "Abc")
    Assertions.assertTrue(analyzer.matchAdvance(IN) == "in")
    Assertions.assertTrue(analyzer.matchAdvance(IDENTIFIER) == "Cde")

    analyzer = Scanner(tokens, "xyz\nabc + def")
    analyzer.advance()
    analyzer.advance()

    runCatching {
      analyzer.advance()
      Assertions.assertTrue(false) { "Error Detection Failure" }
    }.recoverCatching {
      when(it) {
        is ParsingException -> {
          Assertions.assertTrue(
            it.errorReport.equals(
              "Line 2:\n"
                      + "abc + def\n"
                      + "____^\n"
            )
          )
        }
        else -> throw it
      }
    }.getOrNull()
  }
}
