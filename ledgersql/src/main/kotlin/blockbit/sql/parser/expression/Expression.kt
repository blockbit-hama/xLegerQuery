 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.Value
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.Cursor
import blockbit.sql.table.TableDispatchContext

interface Expression {
  @Throws(ParsingException::class)
  fun evaluate(tables: Array<Cursor?>?): Value
  fun dispatch(context: TableDispatchContext): Value
}
