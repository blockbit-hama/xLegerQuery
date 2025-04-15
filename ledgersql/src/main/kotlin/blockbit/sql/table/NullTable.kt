 /** Written by HAMA */

package blockbit.sql.table

import blockbit.sql.parser.IdValue
import blockbit.sql.parser.StringValue

internal class NullTable: Table {
  override fun insert(values: List<String>): Int {
    TODO("Not yet implemented")
  }
  override fun select(requestedColumns: List<String>): Table {
    TODO("Not yet implemented")
  }
  override fun select(where: Selector): Table {
    TODO("Not yet implemented")
  }
  override fun select(where: Selector, requestedColumns: List<String>?): Table {
    TODO("Not yet implemented")
  }
  override fun select(where: Selector, requestedColumns: List<String>?, other: List<Table?>): Table {
    TODO("Not yet implemented")
  }
  override fun rows(): Cursor {
    TODO("Not yet implemented")
  }
}