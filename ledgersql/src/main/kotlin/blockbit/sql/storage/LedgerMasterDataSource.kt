package blockbit.sql.storage

import abc.Address
import abc.ethereum.EthereumWallet
import abc.operation.WalletContractOperation
import blockbit.SortOrder
import blockbit.ethereum.EthereumDataTable
import blockbit.ValuePoint

class blockbitDataSource(
  private val wallet: EthereumWallet,
  val contract: Map<String,Address>) : DataSource {
  
  val cachedTable = mutableMapOf<String, EthereumDataTable>()

  override fun data(tableName: String?, columns: List<String>?, filter: Map<String, String>?): List<List<String>> {
    if(tableName == null) return arrayListOf()

    val dataTable = cachedTable.getOrPut(tableName) {
      wallet.adapt(WalletContractOperation::class.java)
        ?.create(contract.get(tableName)!!, EthereumDataTable::class.java)!!
    }
    return if(filter == null || filter.isEmpty()){
      dataTable.findBy(columns!![0], null, null, SortOrder.None)
    }
    else {
      var primaryFilter = filter.toList()[0]
      dataTable.findBy(
        primaryFilter.first,
        ValuePoint(primaryFilter.second, true),
        ValuePoint(primaryFilter.second, true), SortOrder.None)
    }

  }

  override fun data(tableName: String?, columns: List<String>?, column: String, start: String, end: String): List<List<String>> {
    if(tableName == null) return arrayListOf()

    val dataTable = cachedTable.getOrPut(tableName) {
      wallet.adapt(WalletContractOperation::class.java)
        ?.create(contract.get(tableName)!!, EthereumDataTable::class.java)!!
    }

    return dataTable.findBy(column,
      ValuePoint(start, true),
      ValuePoint(end,true), SortOrder.None)
  }
}