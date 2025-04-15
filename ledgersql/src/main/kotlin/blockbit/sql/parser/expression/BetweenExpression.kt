 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.*
import blockbit.sql.parser.BooleanValue
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.Cursor
import blockbit.sql.table.TableDispatchContext

internal class BetweenExpression(private val column: String, private val between: Pair<String,String>) : Expression {

  @Throws(ParsingException::class)
  override fun evaluate(tables: Array<Cursor?>?): Value {
    return BooleanValue(true)
  }

  override fun dispatch(context: TableDispatchContext): Value {
    val table = context.createScratchTable(context.primaryTable, false)
    return table.addRange(IdValue(null, column), between).select()
  }
}
