 /** Written by HAMA */

package blockbit.sql.parser

import blockbit.sql.lexer.Scanner
import blockbit.sql.lexer.TokenSet
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.parser.expression.*
import blockbit.sql.parser.expression.LikeExpression
import blockbit.sql.parser.expression.LogicalExpression
import blockbit.sql.parser.expression.RelationalExpression
import blockbit.sql.table.*
import java.io.IOException

class Parser(val expression: String) {

  private val scanner = Scanner(tokens, expression)

  class RelationalOperator
  class MathOperator

  @Throws(IOException::class, ParsingException::class)
  fun statement(): Statement {
    scanner.advance()
    if (scanner.matchAdvance(SELECT) != null) {
      val requestedColumns = idList()
      scanner.required(FROM)
      val requestedTableNames = idList()

      val where = if (scanner.matchAdvance(WHERE) == null)
        null
      else
        expr()
      return Statement(requestedColumns, requestedTableNames, where)
    } else {
      error(
        "Expected select!! insert, create, drop, use, update, delete not supported yet "
      )
    }
  }

  @Throws(ParsingException::class)
  private fun idList(): List<String> {
    var identifiers = mutableListOf<String>()
    if (scanner.matchAdvance(STAR) == null) {
      var id: String?
      while (scanner.required(IDENTIFIER).also { id = it } != null) {
        identifiers.add(id!!)
        if (scanner.matchAdvance(COMMA) == null) break
      }
    }
    else {
      identifiers.add("*")
    }
    return identifiers
  }

  @Throws(ParsingException::class)
  private fun declarations(): List<String> {
    val identifiers = mutableListOf<String>()
    var id: String
    while (true) {
      if (scanner.matchAdvance(PRIMARY) != null) {
        scanner.required(KEY)
        scanner.required(LP)
        scanner.required(IDENTIFIER)
        scanner.required(RP)
      } else {
        id = scanner.required(IDENTIFIER)
        identifiers.add(id)

        if (scanner.matchAdvance(INTEGER) != null
          || scanner.matchAdvance(CHAR) != null
        ) {
          if (scanner.matchAdvance(LP) != null) {
            expr()
            scanner.required(RP)
          }
        } else if (scanner.matchAdvance(NUMERIC) != null) {
          if (scanner.matchAdvance(LP) != null) {
            expr()
            scanner.required(COMMA)
            expr()
            scanner.required(RP)
          }
        } else if (scanner.matchAdvance(DATE) != null) {
          // do nothing
        }
        scanner.matchAdvance(NOT)
        scanner.matchAdvance(NULL)
      }
      if (scanner.matchAdvance(COMMA) == null) // no more columns
        break
    }
    return identifiers
  }

  @Throws(ParsingException::class)
  private fun exprList(): List<Expression?> {
    val expressions = mutableListOf<Expression?>()
    expressions.add(expr())
    while (scanner.matchAdvance(COMMA) != null) {
      expressions.add(expr())
    }
    return expressions
  }

  @Throws(ParsingException::class)
  private fun expr(): Expression? {
    var left = andExpr()
    return expr_prime(left)
  }

  @Throws(ParsingException::class)
  private tailrec fun expr_prime(left: Expression?): Expression? {
    if (scanner.matchAdvance(OR) != null) {
      return expr_prime(LogicalExpression(left, OR, andExpr()))
    }
    return left
  }

  @Throws(ParsingException::class)
  private fun andExpr(): Expression? {
    var left = relationalExpr()
    return andExpr_prime(left)
  }

  @Throws(ParsingException::class)
  private tailrec fun andExpr_prime(left: Expression?): Expression? {
    if(scanner.matchAdvance(AND) != null){
      return andExpr_prime(LogicalExpression(left, AND, relationalExpr()))
    }
    return left
  }

  @Throws(ParsingException::class)
  private fun relationalExpr(): Expression? {
    var left = additiveExpr()
    while (true) {
      var lexeme: String?
      if (scanner.matchAdvance(RELOP).also { lexeme = it } != null) {
        var op: RelationalOperator
        op = if (lexeme?.length === 1)
          if (lexeme?.get(0) === '<')
            LT
          else
            GT
        else {
          if (lexeme?.get(0) === '<' && lexeme?.get(1) === '>')
            NE
          else if (lexeme?.get(0) === '<')
            LE
          else
            GE
        }
        left = RelationalExpression(left, op, additiveExpr())
      } else if (scanner.matchAdvance(EQUAL) != null) {
        left = RelationalExpression(left, EQ, additiveExpr())
      } else if (scanner.matchAdvance(LIKE) != null) {
        left = LikeExpression(left, additiveExpr())
      } else if (scanner.matchAdvance(BETWEEN) != null) {
        val column = (left as AtomicExpression).atom as IdValue
        left = BetweenExpression(column.columnName, betweenExpr())
      }else break
    }
    return left
  }

  @Throws(ParsingException::class)
  private fun additiveExpr(): Expression? {
    var lexeme: String?
    var left = multiplicativeExpr()
    while (scanner.matchAdvance(ADDITIVE).also { lexeme = it } != null) {
      val op = if (lexeme?.get(0) === '+') PLUS else MINUS
      left = ArithmeticExpression(left, multiplicativeExpr(), op)
    }
    return left
  }

  @Throws(ParsingException::class)
  private fun betweenExpr(): Pair<String, String>{
    var start = term()?.evaluate(null).toString()
    scanner.matchAdvance(AND)
    var end = term()?.evaluate(null).toString()

    return Pair(start,end)
  }

  @Throws(ParsingException::class)
  private fun multiplicativeExpr(): Expression? {
    var left = term()
    while (true) {
      if (scanner.matchAdvance(STAR) != null)
        left = ArithmeticExpression(left, term(), TIMES)
      else if (scanner.matchAdvance(SLASH) != null)
        left = ArithmeticExpression(left, term(), DIVIDE)
      else
        break
    }
    return left
  }

  @Throws(ParsingException::class)
  private fun term(): Expression? {
    return if (scanner.matchAdvance(NOT) != null) {
      NotExpression(expr())
    } else if (scanner.matchAdvance(LP) != null) {
      val toReturn = expr()
      scanner.required(RP)
      toReturn
    } else
      factor()
  }

  @Throws(ParsingException::class)
  private fun factor(): Expression? {
    try {
      var lexeme: String?
      val result: Value?
      if (scanner.matchAdvance(STRING).also { lexeme = it } != null)
        result = StringValue(lexeme)
      else if (scanner.matchAdvance(NUMBER).also { lexeme = it } != null)
        result = NumericValue(lexeme!!.toDouble())
      else if (scanner.matchAdvance(NULL).also { lexeme = it } != null)
        result = NullValue()
      else {
        var columnName: String = scanner.required(IDENTIFIER)
        var tableName: String? = null
        if (scanner.matchAdvance(DOT) != null) {
          tableName = columnName
          columnName = scanner.required(IDENTIFIER)
        }
        result = IdValue(tableName, columnName)
      }
      return AtomicExpression(result)
    } catch (e: java.text.ParseException) {
    }
    error("Couldn't parse Number")
    return null
  }

  companion object {

    val tokens = TokenSet()
    val COMMA = tokens.create("',")

    val EQUAL = tokens.create("'=")
    val LP = tokens.create("'(")
    val RP = tokens.create("')")
    val DOT = tokens.create("'.")
    val STAR = tokens.create("'*")
    val SLASH = tokens.create("'/")
    val AND = tokens.create("'AND")
    val FROM = tokens.create("'FROM")
    val KEY = tokens.create("'KEY")
    val LIKE = tokens.create("'LIKE")
    val BETWEEN = tokens.create("'BETWEEN")
    val NOT = tokens.create("'NOT")
    val NULL = tokens.create("'NULL")
    val OR = tokens.create("'OR")
    val PRIMARY = tokens.create("'PRIMARY")
    val SELECT = tokens.create("'SELECT")
    val WHERE = tokens.create("'WHERE")
    val ADDITIVE = tokens.create("\\+|-")
    val STRING = tokens.create("(\".*?\")|('.*?')")
    val RELOP = tokens.create("[<>][=>]?")
    val NUMBER = tokens.create("[0-9]+(\\.[0-9]+)?")
    val INTEGER = tokens.create("(small|tiny|big)?int(eger)?")
    val NUMERIC = tokens.create("decimal|numeric|real|double")
    val CHAR = tokens.create("(var)?char")
    val DATE = tokens.create("date(\\s*\\(.*?\\))?")
    val IDENTIFIER = tokens.create("[a-zA-Z_0-9/\\\\:~]+")
    val EQ = RelationalOperator()
    val LT = RelationalOperator()
    val GT = RelationalOperator()
    val LE = RelationalOperator()
    val GE = RelationalOperator()
    val NE = RelationalOperator()
    val PLUS = MathOperator()
    val MINUS = MathOperator()
    val TIMES = MathOperator()
    val DIVIDE = MathOperator()
  }
}