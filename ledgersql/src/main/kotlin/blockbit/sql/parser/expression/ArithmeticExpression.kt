 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.NumericValue
import blockbit.sql.parser.Parser
import blockbit.sql.parser.Value
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.Cursor
import blockbit.sql.table.TableDispatchContext

class ArithmeticExpression(
  private val left: Expression?, private val right: Expression?,
  private val operator: Parser.MathOperator
) : Expression {
  @Throws(ParsingException::class)
  override fun evaluate(tables: Array<Cursor?>?): Value {
    val leftValue = left?.evaluate(tables)
    val rightValue = right?.evaluate(tables)
    val l = (leftValue as NumericValue).value()
    val r = (rightValue as NumericValue).value()
    return NumericValue(
      if (operator === Parser.PLUS)
        l + r
      else if (operator === Parser.MINUS)
        l - r
      else if (operator === Parser.TIMES)
        l * r
      else
        l / r
    )
  }

  override fun dispatch(context: TableDispatchContext): Value  {
    val table = context.createScratchTable(context.primaryTable, false)
    return table.select()
  }
}
