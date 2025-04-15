package blockbit.sql.storage

interface DataSource {
  fun data(tableName: String?, columns: List<String>?, filter: Map<String, String>?): List<List<String>>
  fun data(tableName: String?, columns: List<String>?, column: String, start: String, end: String): List<List<String>>
}

