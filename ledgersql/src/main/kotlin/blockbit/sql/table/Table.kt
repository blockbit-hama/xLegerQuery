 /** Written by HAMA */

package blockbit.sql.table

import blockbit.sql.parser.IdValue
import blockbit.sql.parser.StringValue
import blockbit.sql.parser.Value

interface Table : Cloneable, Value {
    fun insert(values: List<String>): Int
    fun select(requestedColumns :  List<String>) : Table
    fun select(where: Selector): Table
    fun select(where: Selector, requestedColumns: List<String>?): Table
    fun select(where: Selector, requestedColumns: List<String>?, other: List<Table?>): Table

    fun rows(): Cursor

    interface Importer //{=Table.Importer}
    {
        fun loadRow(): Table
    }
}