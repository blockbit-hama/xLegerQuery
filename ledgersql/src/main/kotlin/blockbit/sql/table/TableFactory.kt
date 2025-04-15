 /** Written by HAMA */

package blockbit.sql.table

import blockbit.sql.storage.DataSource


interface TableFactory {
    val source: DataSource
    fun create(meta: TableMetaData): Table
    fun createScratchTable(meta: TableMetaData?, empty: Boolean): ScratchTable
}

internal class DefaultTableFactory(override val source: DataSource) : TableFactory {
    override fun create(meta: TableMetaData): Table {
        return ConcreteTable(meta.tableName, meta.columnNames)
    }

    override fun createScratchTable(meta: TableMetaData?, empty: Boolean): ScratchTable {
        return ScratchTable(meta, if(empty) null else source )
    }
}
