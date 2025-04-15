package blockbit.sql

import abc.ethereum.EthereumPrivateKeyImporter.from
import abc.ethereum.EthereumWallet
import blockbit.DataTableStore
import blockbit.ethereum.EthereumDataTableStore
import blockbit.sql.parser.Parser
import blockbit.sql.table.*
import blockbit.sql.table.cache.TableInfoCache

class LedgerSQL(
  val endpoint: String,
  val privateKey: String,
  val tableStoreContract: String?,
  private var tablesMetaData: List<TableMetaData>?,
  private var tableFactory: TableFactory?) {

  val wallet = EthereumWallet(endpoint, from(privateKey))
  var tableStore : EthereumDataTableStore? = null
  val tableInfoCache = TableInfoCache()

  init {
    if (tableStoreContract != null){
      tableStore = wallet.create(tableStoreContract, EthereumDataTableStore::class.java)
    }
  }

  fun sql(expression: String): Cursor? {
    val context = TableDispatchContext(wallet, tableStore, tablesMetaData, tableFactory, tableInfoCache)
    val executor = blockbit.sql.execution.Executor()
    return executor.execute(context,  Parser(expression))?.rows()
  }
}