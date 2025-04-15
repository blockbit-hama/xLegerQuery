 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.BooleanValue
import blockbit.sql.parser.Value
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.Cursor
import blockbit.sql.table.TableDispatchContext

class NotExpression(private val operand: Expression?) : Expression {
  @Throws(ParsingException::class)
  override fun evaluate(tables: Array<Cursor?>?): Value {
    val value = operand?.evaluate(tables)
    return BooleanValue(!(value as BooleanValue).value())
  }

  override fun dispatch(context: TableDispatchContext): Value {
    val table = context.createScratchTable(context.primaryTable, false)
    return table.select()
  }
}
