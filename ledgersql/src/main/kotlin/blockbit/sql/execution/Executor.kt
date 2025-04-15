/*
 *
 *  *
 *  * Copyright OpusM Corp. All Rights Reserved.
 *  *
 *  * Written by HAMA
 *  *
 *
 */

package blockbit.sql.execution

import abc.Address
import blockbit.sql.parser.BooleanValue
import blockbit.sql.parser.Parser
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.parser.expression.Expression
import blockbit.sql.table.*
import java.io.IOException

class Executor {
  @Throws(IOException::class, ParsingException::class)
  fun execute(
    context: TableDispatchContext,
    parser: Parser): Table? {

    try {
      val stat = parser.statement()
      context.initTableMetaDataAndTableFactory(stat.requestedTableNames, stat.requestedColumns)

      val interimTable = doDispatch(context, stat.where)
      if (interimTable.isJoined) {
        context.addIntrimTable(interimTable)
        context.addIntrimTable(interimTable.joinTable!!)
      } else {
        context.addIntrimTable(interimTable)
      }
      return doSelect(context, stat.requestedColumns, stat.requestedTableNames, stat.where)
    } catch (e: ParsingException) {
      throw e
    } catch (e: IOException) {
      throw e
    }
    return null
  }

  @Throws(ParsingException::class)
  private fun doSelect(
    context: TableDispatchContext,
    columns: List<String>?,
    requestedTableNames: List<String?>?,
    where: Expression?
  ): Table {
    val tableNames= requestedTableNames?.iterator()
    assert(tableNames!!.hasNext()) { "No tables to use in select!" }

    val primary: Table = context.tables.get(tableNames.next()) as Table
    val participantsInJoin = mutableListOf<Table?>()
    while (tableNames.hasNext()) {
      val participant = tableNames.next()
      participantsInJoin.add(context.tables.get(participant))
    }

    val selector: Selector = if (where == null)
      Selector.ALL
    else
      object : Selector {
        override fun approve(rows: List<Cursor?>): Boolean {
          return try {
            val result = where.evaluate(rows.toTypedArray())
            (result as BooleanValue).value()
          } catch (e: ParsingException) {
            throw e
          }
        }
      }

    return primary.select(selector, columns?.toList(), participantsInJoin)
  }

  @Throws(ParsingException::class)
  private fun doDispatch(
    context: TableDispatchContext,
    where: Expression?
  ): ScratchTable {

    return if(where == null) {
      context.createScratchTable(context.primaryTable, false).select()
    }
    else {
      where.dispatch(context) as ScratchTable
    }
  }
}