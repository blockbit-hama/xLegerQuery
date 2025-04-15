 /** Written by HAMA */

package blockbit.sql.table
import kotlin.collections.Iterator

interface Cursor {
    @Throws(NoSuchElementException::class)
    fun advance(): Boolean
    fun columnCount(): Int?
    fun columnName(index: Int): String?
    fun column(columnName: String): String?

    fun columns(): Iterator<String>
    fun isTraversing(table: String): Boolean
}