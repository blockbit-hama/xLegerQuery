 /** Written by HAMA */

package blockbit.sql.table


interface Selector {
    fun approve(rows: List<Cursor?>): Boolean

    class Adapter : Selector {
        override fun approve(rows: List<Cursor?>): Boolean {
            return true
        }
    }

    companion object {
        val ALL: Selector = Adapter()
    }
}