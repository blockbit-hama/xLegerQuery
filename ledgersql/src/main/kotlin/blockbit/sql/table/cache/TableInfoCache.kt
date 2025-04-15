package blockbit.sql.table.cache

import blockbit.sql.table.TableMetaData

class TableInfoCache {
  val tableInfo = mutableMapOf<String, TableMetaData>()
}