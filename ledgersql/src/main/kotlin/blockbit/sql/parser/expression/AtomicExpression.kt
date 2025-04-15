 /** Written by HAMA */

package blockbit.sql.parser.expression

import blockbit.sql.parser.IdValue
import blockbit.sql.parser.Value
import blockbit.sql.table.Cursor
import blockbit.sql.table.TableDispatchContext

internal class AtomicExpression(val atom: Value) : Expression {
  override fun evaluate(tables: Array<Cursor?>?): Value {
    return if (atom is IdValue)
      atom.value(tables)
    else
      atom
  }

  override fun dispatch(context: TableDispatchContext): Value {
    return if (atom is IdValue)
      atom
    else
      atom
  }
}