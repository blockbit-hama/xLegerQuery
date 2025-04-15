 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.*
import blockbit.sql.parser.BooleanValue
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.*

internal class RelationalExpression(
  private val left: Expression?,
  private val operator: Parser.RelationalOperator,
  private val right: Expression?
) : Expression {
  @Throws(ParsingException::class)
  override fun evaluate(tables: Array<Cursor?>?): Value {
    var leftValue = left?.evaluate(tables)
    var rightValue = right?.evaluate(tables)
    if (leftValue is StringValue || rightValue is StringValue) {
      val isEqual = leftValue.toString() == rightValue.toString()
      return BooleanValue(if (operator === Parser.EQ) isEqual else !isEqual)
    }
    if (rightValue is NullValue || leftValue is NullValue) {
      val isEqual = leftValue?.javaClass === rightValue?.javaClass
      return BooleanValue(if (operator === Parser.EQ) isEqual else !isEqual)
    }

    if (leftValue is BooleanValue)
      leftValue = NumericValue(if (leftValue.value()) 1.0 else 0.0)

    if (rightValue is BooleanValue)
      rightValue = NumericValue(if (rightValue.value()) 1.0 else 0.0)

    val l = (leftValue as NumericValue).value()
    val r = (rightValue as NumericValue).value()
    return BooleanValue(
      if (operator === Parser.EQ)
        l == r
      else if (operator === Parser.NE)
        l != r
      else if (operator === Parser.LT)
        l < r
      else if (operator === Parser.GT)
        l > r
      else if (operator === Parser.LE)
        l <= r
      else
        l >= r
    )
  }

  override fun dispatch(context: TableDispatchContext): Value {
    var leftValue = left?.dispatch(context)
    var rightValue = right?.dispatch(context)

    if (leftValue is IdValue && (rightValue is StringValue || rightValue is NumericValue)) {
      val table = context.createScratchTable(leftValue.tableName?: context.primaryTable, false)
      if (operator === Parser.EQ && rightValue is StringValue){
        return table.addFilter(leftValue, rightValue).select()
      }
      return table.select()
    }
    else if (leftValue is IdValue && rightValue is IdValue) {
      val leftScratchTable = context.createScratchTable(leftValue.tableName?: context.requestedTableNames.get(0), false)
      val leftInterimTable = leftScratchTable.select()
      val rightScratchTable = context.createScratchTable(rightValue.tableName?: context.requestedTableNames.get(1), false)
      val rightInterimTable = rightScratchTable.select()

      return leftInterimTable.join(rightInterimTable)
    }
    return ScratchTable(null, null)
  }
}
