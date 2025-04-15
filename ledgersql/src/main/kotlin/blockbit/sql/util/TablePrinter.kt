package blockbit.sql.util

import blockbit.Row
import blockbit.util.HexaUtils.dump
import blockbit.util.ListTablePrinter
import blockbit.util.TableAlignment.*
import java.io.BufferedReader
import java.io.StringReader
import kotlin.math.ceil
import kotlin.math.max
import kotlin.streams.toList

typealias ContentProvider<T> = () -> Collection<T>

typealias LabelProvider<T> = (row: T, index: Int)-> String

enum class TableAlignment {
  Left, Right, Center
}

open class TablePrinter<T>(private vararg val headers: String) : WithLogger {
  lateinit var labelProvider: LabelProvider<T>
  lateinit var contentProvider: ContentProvider<T>
  var alignment = Left

  override fun toString(): String {
    val columnLengths = Array(headers.size) { i -> WCWidth.calculate(headers[i]) }
    val iterable = contentProvider()

    fun String.splitLines(): List<String> {
      return BufferedReader(StringReader(this)).use {
        it.lines().toList()
      }
    }
    // 각 칼럼의 폭을 계산한다.
    for (row in iterable.iterator()) {
      for (i in headers.indices) {
        columnLengths[i] = max(columnLengths[i],
          labelProvider(row, i).splitLines().map { WCWidth.calculate(it) }.maxOrNull()?:1
        )
      }
    }

    // 수평선
    val line = columnLengths.joinToString("+", "+", "+") { "-".repeat(it) }
    /**
     * 수평선을 그린다.
     */
    fun StringBuilder.appendHorizontalLine(): StringBuilder {
      append(line + "\n")
      return this
    }

    fun printCell(width: Int, str: String): String {
      logger.trace("Str: ...\n{}", dump(str.toByteArray()))
      val space = width - WCWidth.calculate(str)
      return when (alignment) {
        Left -> str + " ".repeat(space)
        Center -> " ".repeat(ceil(space / 2.0).toInt()) + str + " ".repeat(space / 2)
        Right -> " ".repeat(space) + str
      }
    }

    /**
     * 헤더부를 출력한디.
     */
    fun StringBuilder.appendHeader(): StringBuilder {
      headers.zip(columnLengths)
        .joinToString ("|", "|", "|") { (header, width) -> printCell(width, header) }
        .also { str -> append(str + "\n") }
      return this
    }

    fun StringBuilder.appendData(): StringBuilder {
      for (row in iterable.iterator()) {
        val values = headers.indices.map { labelProvider(row, it) }
        val rows = values.map { it.splitLines().size }.maxOrNull()?:1
        (0 until Math.max(1, rows)).map { r ->
          val l = values.map { it.splitLines().elementAtOrElse(r) { "" } }
            .zip(columnLengths)
          l.joinToString ("|", "|", "|") { (str, width) -> printCell(width, str) }
            .also { str -> append(str + "\n") }
        }
      }
      return this
    }

    return if (iterable.isEmpty()) {
      "None"
    } else {
      StringBuilder()
        .appendHorizontalLine()
        .appendHeader()
        .appendHorizontalLine()
        .appendData()
        .appendHorizontalLine()
        .toString()
    }
  }
}

class MapTablePrinter(map: Map<*, *>) : TablePrinter<Map.Entry<*, *>>("key", "value") {
  init {
    labelProvider = { entry, index -> when(index) {
      0 -> entry.key.toString()
      1 -> entry.value.toString()
      else -> ""
    } }
    contentProvider = map::entries
  }
}

fun Map<*, *>.toPrettyString() : String = MapTablePrinter(this).toString()

open class ListTablePrinter<T>(list: List<T>, vararg headers: String) : TablePrinter<T>(*headers) {
  init {
    contentProvider = { list }
  }
}