package blockbit.sql

import blockbit.util.ListTablePrinter
import blockbit.util.WithLogger
import java.util.*


abstract class AbstractTestCase : WithLogger {
  fun uuid(): String {
    return UUID.randomUUID().toString().substring(0, 8)
  }
}