 /** Written by HAMA */

package blockbit.sql.table

import abc.Address
import abc.Endpoint
import abc.PrivateKey
import abc.ethereum.EthereumPrivateKeyImporter
import abc.ethereum.EthereumWallet
import blockbit.DataTableStore
import blockbit.ethereum.EthereumDataTableStore
import blockbit.sql.storage.blockbitDataSource
import blockbit.sql.table.cache.TableInfoCache

class TableDispatchContext(
  private val wallet: EthereumWallet,
  private val tableStore: EthereumDataTableStore?,
  private var tablesMetaData: List<TableMetaData>?,
  private var tableFactory: TableFactory?,
  private var cache: TableInfoCache)
{

  val tables  = mutableMapOf<String,Table>()

  lateinit var primaryTable: String
  lateinit var requestedColumns :  List<String>
  lateinit var requestedTableNames : List<String>

  fun initTableMetaDataAndTableFactory(tables: List<String>, columns: List<String>) {
    requestedColumns = columns
    requestedTableNames = tables
    primaryTable = tables.get(0)

    if(tablesMetaData == null){
      require(tableStore != null) {"tablestore contract is needed for filling tables infomation automatically"}
      tablesMetaData = requestedTableNames.map {
        cache.tableInfo.getOrPut(it, {
          val table = tableStore.getTable(it)
          val metadata = table?.getMetadata()!!
          metadata.let { meta ->
            TableMetaData(meta.name, meta.columns.map { column ->
              column.name
            }, table.address)
          }
        })
      }
    }

    val contracts = tablesMetaData?.associateBy({it.tableName!!}, {it.address})
    if(tableFactory == null)
      tableFactory =  DefaultTableFactory(blockbitDataSource(wallet , contracts!!))

  }

  fun createScratchTable(tableName: String?, empty: Boolean): ScratchTable {
    return tableFactory!!.createScratchTable(tablesMetaData?.find { it.tableName == tableName }, empty)
  }

  fun addIntrimTable(interimTable: ScratchTable) {
    tables.put(interimTable.meta?.tableName!!, interimTable.toTable())
  }
}