 * Written by HAMA
 */

package blockbit.sql.table

import blockbit.sql.parser.IdValue
import blockbit.sql.parser.StringValue
import blockbit.sql.table.exceptions.NoSuchColumnExceptions
import java.util.*
import kotlin.collections.Iterator

/* ConcreteTable : Top-down style table
Starting from a full table, the data is gradually filtered by individual evaluation of rows
 */
internal class ConcreteTable (
    val tableName: String?,
    val columnNames: List<String>?
): Table {

    val rowSet = mutableListOf<List<String>>()

    val currentFilters  = mutableMapOf<IdValue, StringValue>()

    @Throws(NoSuchColumnExceptions::class)
    private fun indexOf(columnName: String): Int {
        return columnNames?.indexOf(columnName)?: throw NoSuchColumnExceptions(
            "Column ($columnName) doesn't exist in $tableName"
        )
    }

    override fun insert(values: List<String>): Int {
//        require(values.size == width()) {
//            ("Values-array length ($values.size) is not the same as table width (" + width() + ")")
//        }
        rowSet.add(values)
        return 1
    }

    override fun select(requestedColumns: List<String>): Table {
        val resultTable = ConcreteTable(tableName, requestedColumns)
        val requestedColumnsIndex = requestedColumns.map{
            indexOf(it)
        }
        rowSet.forEach { row ->
            var approved = true
            currentFilters.forEach { t, u ->
                if(row[indexOf(t.columnName)] != u.toString()){
                    approved = false
                }
            }
            if(approved) {
                val requestedRow = requestedColumnsIndex.map {
                    row[it]
                }
                resultTable.insert(requestedRow)
            }
        }
        return resultTable
    }

    override fun select(where: Selector): Table {
        val resultTable: Table = ConcreteTable(
            tableName,
            columnNames
        )
        val currentRow = rows() as Results
        val envelope: List<Cursor?> = listOf(currentRow)
        while (currentRow.advance()) {
            if (where.approve(envelope))
                resultTable.insert(currentRow.cloneRow())
        }
        return resultTable
    }

    override fun select(where: Selector, requestedColumns: List<String>?): Table {
        if (requestedColumns == null || requestedColumns[0] == "*")
            return select(where)
        val resultTable: Table = ConcreteTable(
            tableName,
            columnNames,
        )
        val currentRow = rows() as Results
        val envelope: List<Cursor?> = listOf(currentRow)
        while (currentRow.advance()) {
            if (where.approve(envelope)) {
                val newRow = kotlin.Array(requestedColumns.size){""}
                for (column in requestedColumns.indices) {
                    newRow[column] = currentRow.column(requestedColumns[column])!!
                }
                resultTable.insert(newRow.toList())
            }
        }
        return resultTable
    }


    override fun select(where: Selector, requestedColumns: List<String>?, other: List<Table?>): Table {
        if (other.size == 0) {
            if (requestedColumns == null)
                return select(where)
            return select(where, requestedColumns)
        }

        val allTables: Array<Table?> = arrayOfNulls<Table>(other.size + 1)
        allTables[0] = this
        other.forEachIndexed{idx, table ->  allTables[idx+1] = table }

        val resultTable: Table = ConcreteTable(null, requestedColumns!!)
        val envelope: Array<Cursor?> = arrayOfNulls<Cursor>(allTables.size)

        cartesianProduct(0, where, requestedColumns, allTables.toList(), envelope, resultTable)
        return resultTable
    }

    override fun rows(): Cursor {
        return Results()
    }

    private inner class Results : Cursor {
        private val rowIterator = rowSet.iterator()
        private lateinit var row: List<String>
        override fun advance(): Boolean {
            if (rowIterator.hasNext()) {
                row = rowIterator.next()
                return true
            }
            return false
        }

        override fun columnCount(): Int? {
            return columnNames?.size
        }

        override fun columnName(index: Int): String? {
            return columnNames?.get(index)
        }

        override fun column(columnName: String): String? {
            return runCatching { row[indexOf(columnName)] }.getOrNull()
        }

        override fun columns(): Iterator<String> {
            return row.iterator()
        }

        override fun isTraversing(table: String): Boolean {
            if(table == tableName)
                return true
            return false
        }

        fun cloneRow(): List<String>{
            return row
        }
    }

    private fun width(): Int? {
        return columnNames?.size
    }

    private fun cartesianProduct(
        level: Int,
        where: Selector,
        requestedColumns: List<String>,
        allTables: List<Table?>,
        allIterators: Array<Cursor?>,
        resultTable: Table
    ) {
        allIterators[level] = allTables[level]?.rows()
        while (allIterators[level]!!.advance()) {
            if (level < allIterators.size - 1) {
                cartesianProduct(
                    level + 1,
                    where,
                    requestedColumns,
                    allTables,
                    allIterators,
                    resultTable
                )
            }

            if (level == allIterators.size - 1) {
                if (where.approve(allIterators.toList())) {
                    insertApprovedRows(resultTable, requestedColumns, allIterators.toList())
                }
            }
        }
    }

    fun <T> Iterator<T>.toList(): List<T> =
        LinkedList<T>().apply {
            while (hasNext())
                this += next()
        }.toMutableList()

    private fun insertApprovedRows(
        resultTable: Table,
        requestedColumns: List<String>,
        allTables: List<Cursor?>
    ) {
        if(requestedColumns[0] == "*"){

            val resultRow = allTables.flatMap {
                it!!.columns().toList()
            }

            resultTable.insert(resultRow)
        }
        else {
            val resultRow = Array(requestedColumns.size){""}
            for (colum_idx in requestedColumns.indices) {
                for (table_idx in allTables.indices) {
                    try {
                        resultRow[colum_idx] = allTables[table_idx]!!.column(requestedColumns[colum_idx])!!
                        break
                    } catch (e: Exception) {
                    }
                }
            }
            resultTable.insert(resultRow.toList())
        }
    }
}
