 /** Written by HAMA */

package blockbit.sql.table

import abc.Address
import blockbit.sql.table.types.DataType

data class StructField(
  val name: String,
  val dataType: DataType
)

data class TableMetaData(
  var tableName: String?,
  var columnNames: List<String>?, //[TODO] change string type to StructField type later
  var address: Address
)