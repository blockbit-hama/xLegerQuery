 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.BooleanValue
import blockbit.sql.parser.StringValue
import blockbit.sql.parser.Value
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.Cursor
import blockbit.sql.table.TableDispatchContext

internal class LikeExpression(private val left: Expression?, private val right: Expression?) : Expression {
  @Throws(ParsingException::class)
  override fun evaluate(tables: Array<Cursor?>?): Value {
    val leftValue = left?.evaluate(tables)
    val rightValue = right?.evaluate(tables)
    val compareTo = (leftValue as StringValue).value()
    var regex = (rightValue as StringValue).value()
    return BooleanValue(compareTo.matches(Regex(regex.replace("%", ".*"))))
  }

  override fun dispatch(context: TableDispatchContext): Value {
    val table = context.createScratchTable(context.primaryTable, false)
    return table.select()
  }
}
