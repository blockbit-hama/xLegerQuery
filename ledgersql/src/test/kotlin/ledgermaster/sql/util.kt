package blockbit.sql

import blockbit.util.ListTablePrinter

class RowTablePrinter(list: List<List<String>>)
  : ListTablePrinter<List<String>>(list, "id") {
  init {
    labelProvider = { ctx, index ->
      when (index) {
        0 -> ctx.get(0)
        else -> ""
      }
    }
  }
}
