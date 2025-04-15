 /** Written by HAMA */

package blockbit.sql.table

import blockbit.sql.parser.IdValue
import blockbit.sql.parser.StringValue
import blockbit.sql.parser.Value
import blockbit.sql.storage.DataSource
import blockbit.sql.table.exceptions.NoSuchColumnExceptions

/* ScratchTable : Bottom-up style table
Starting from an empty table, the Table is gradually filled by individual computation .
 */

data class RangeData(val columnName: String, val start: String, val end: String)

class ScratchTable (
  val meta: TableMetaData?,
  val source: DataSource?) : Value {

  val rowSet = mutableListOf<List<String>>()

  val relationalFilters  = mutableMapOf<IdValue, StringValue>()
  var rangeFilter: RangeData? = null

  var joinTable : ScratchTable? = null
  var isJoined : Boolean = false

  fun addFilter(left : IdValue, right: StringValue): ScratchTable{
    relationalFilters.put(left, right)
    return this
  }

  fun addRange(left : IdValue, between: Pair<String,String>): ScratchTable{
    rangeFilter = RangeData(left.columnName, between.first, between.second)
    return this
  }

  fun select(): ScratchTable {
    val resultTable = ScratchTable(meta, null)
    if(rangeFilter != null) {
      source?.data(meta?.tableName,
        meta?.columnNames,
        rangeFilter!!.columnName,
        rangeFilter!!.start,
        rangeFilter!!.end)?.forEach {
        resultTable.insert(it)
      }
    }
    else {
      source?.data(meta?.tableName, meta?.columnNames,  relationalFilters.entries.associate {
        it.key.columnName to it.value.value()
      })?.forEach {
        resultTable.insert(it)
      }
    }
    return resultTable
  }

  @Throws(NoSuchColumnExceptions::class)
  private fun indexOf(columnName: String): Int {
    return meta?.columnNames?.indexOf(columnName)?: throw NoSuchColumnExceptions(
      "Column ($columnName) doesn't exist in ${meta?.tableName}"
    )
  }

  fun insert(values: List<String>): Int {
    rowSet.add(values)
    return 1
  }

  private fun width(): Int? {
    return meta?.columnNames?.size
  }

  fun join(rightTable: ScratchTable): ScratchTable {
    if(rightTable.isJoined) {
      rowSet.union(rightTable.rowSet)
      isJoined = true
      joinTable = rightTable.joinTable
      return this
    }
    else {
      isJoined = true
      joinTable = rightTable
      return this
    }
  }

  fun toTable(): Table {
    val concreteTable = ConcreteTable(meta?.tableName, meta?.columnNames)
    rowSet.forEach {
      concreteTable.insert(it)
    }
    return concreteTable
  }
}
