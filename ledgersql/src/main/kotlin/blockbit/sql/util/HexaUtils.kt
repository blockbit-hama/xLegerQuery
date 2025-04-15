package blockbit.sql.util

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringWriter
import java.io.Writer


/**
 * LoggingUtils
 *
 * @author bylee
 */
object HexaUtils {
  /* Dump Format */
  internal const val CONTROL_CHARS_SHOWER = '.'
  internal val HEXA_CHARS = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  )
  internal const val N_INT_BY_BYTE = 4
  internal const val WIDTH_PER_LINE = 32
  /**
   *
   *
   * `ch`에 해당하는 16진수값을 `buffer`에 추가한다.
   *
   *
   * 이 때, 0을 왼쪽에 padding하여 두자리수를 유지한다.
   *
   *
   * @param buffer 16진수값을 쓸 버퍼
   * @param ch     변환할 10진수
   */
  fun appendHexa(buffer: StringBuilder, ch: Int) {
    if (ch < 16) { // 한자리인 경우
      buffer.append('0')
    } else { // 두자리인 경우
      buffer.append(HEXA_CHARS[0x0f and (ch shr 4)])
    }
    buffer.append(HEXA_CHARS[0x0f and ch])
  }
  /**
   * `buffer`에 `bytes`의 `offset` 바이트부터 `length` 개를
   *
   *
   * 16진수 문자열의 형태로 출력한다.
   *
   * @param buffer 출력할 버퍼
   * @param bytes  변환할 byte 배열
   * @param offset 출력을 시작할 지점
   * @param length 출력할 바이트의 길이
   */
  /**
   * `buffer`에 `bytes`를 16진수 문자열의 형태로 출력한다.
   *
   * @param buffer 출력할 버퍼
   * @param bytes  변환할 byte 배열
   * @see .appendHexa
   */
  @JvmOverloads
  fun appendHexa(
    buffer: StringBuilder,
    bytes: ByteArray,
    offset: Int = 0,
    length: Int = bytes.size
  ) {
    var i = offset
    var j = 0
    while (j < length) {
      appendHexa(buffer, bytes[i].toInt())
      ++i
      ++j
    }
  }

  internal fun lineEnd(
    hexPart: StringBuilder,
    textPart: StringBuilder,
    ret: StringBuilder
  ) { // 영역별 구분자를 넣는다.
    hexPart.append("     |")
    // 마지막 구분자를 넣는다.
    textPart.append("|\n")
    // 두영역을 결합한다.
    ret.append(hexPart)
    ret.append(textPart)
    // 영역 버퍼를 비운다.
    hexPart.delete(0, hexPart.capacity())
    textPart.delete(0, textPart.capacity())
  }

  /**
   * `data`를 Logger에 찍기 위한 16진수 포맷으로 변환한다.
   *
   *
   * 출력 레이아웃
   * <table summary="format-of-output">
   * <tr><td>주소</td><td>바이트</td><td>16진수값</td></tr>
  </table> *
   *
   *
   * 바이트 영역에서 화면에 출력할 수 없는 문자인 경우 '.'로 대체한다.
   *
   * @param data 변환할 byte배열
   * @return 변환된 문자열
   * @see .dump
   * @see .dump
   */
  fun dump(data: ByteArray?): String {
    return if (null == data) {
      "<<null>>"
    } else dump(data, 0, data.size)
  }

  /**
   * `data`의 내용 중 `offset`에서 길이가 `length`인
   *
   *
   * byte들을 Logger에 찍기 위한 16진수 포맷으로 변환하여 [String]으로 반환한다.
   *
   *
   * 출력 레이아웃
   * <table summary="format-of-output">
   * <tr><td>주소</td><td>바이트</td><td>16진수값</td></tr>
  </table> *
   *
   *
   * 바이트 영역에서 화면에 출력할 수 없는 문자인 경우 '.'로 대체한다.
   *
   * @param data   변환할 byte배열
   * @param offset 변환을 시작할 위치
   * @param length 변환할 길이
   * @return 변환된 문자열
   * @see .dump
   */
  fun dump(data: ByteArray?, offset: Int, length: Int): String {
    val writer = StringWriter()
    dump(data, offset, length, writer)
    return writer.toString()
  }

  /**
   * `data`의 내용 중 `offset`에서 길이가 `length`인
   *
   *
   * byte들을 Logger에 출력하기 위한 16진수 포맷으로 변환하여 `writer`에 기록한다.
   *
   *
   * 출력 레이아웃
   * <table summary="format-of-output">
   * <tr><td>주소</td><td>바이트</td><td>16진수값</td></tr>
  </table> *
   *
   *
   * 바이트 영역에서 화면에 출력할 수 없는 문자인 경우 '.'로 대체한다.
   *
   * @param data   변환할 byte배열
   * @param offset 변환을 시작할 위치
   * @param length 변환할 길이
   * @param writer 출력할 [Writer]
   */
  fun dump(data: ByteArray?, offset: Int, length: Int, writer: Writer) {
    try {
      if (null == data) {
        writer.write("<<null>>")
        return
      }
      if (data.isEmpty()) {
        writer.write("<<EMPTY BYTES>>")
        return
      }
      val reader = ByteArrayInputStream(data, offset, length)
      val ret = StringBuilder()
      val hexPart = StringBuilder()
      val textPart = StringBuilder()
      var address = 0
      var ch: Int
      var printByte: Int
      var cnt = 0
      // 주소 부분의 타이틀 부분은 빈 문자열( ' ' )로 채운다.
      hexPart.append("          ")
      // 눈금자 표식을 넣는다.
      repeat(WIDTH_PER_LINE / 4) {
        hexPart.append("+-------")
        textPart.append("+---")
      }
      lineEnd(hexPart, textPart, ret)
      while (0 <= reader.read().also { ch = it }) {
        if (0 == cnt) { // 시작 주소값을 계산하여 조립한다.
          for (i in N_INT_BY_BYTE - 1 downTo 0) {
            printByte = 0xFF and (address shr 8 * i)
            appendHexa(hexPart, printByte)
          }
          hexPart.append("  ")
          address += WIDTH_PER_LINE
        }
        appendHexa(hexPart, ch)
        if (ch < 32 || 127 <= ch) { // 제어 문자인 경우, 출력하면 안된다.
          textPart.append(CONTROL_CHARS_SHOWER)
        } else {
          textPart.append(ch.toChar())
        }
        cnt++
        if (WIDTH_PER_LINE == cnt) {
          lineEnd(hexPart, textPart, ret)
          cnt = 0
        }
      } // END of while ( 0 <= (ch = reader.read() ) )
      // 나머지 부분을 채워넣는다.
      if (0 != cnt) {
        while (cnt < WIDTH_PER_LINE) {
          hexPart.append("  ")
          textPart.append(' ')
          ++cnt
        }
        lineEnd(hexPart, textPart, ret)
      }
      writer.write(ret.toString())
      try {
        writer.flush()
      } catch (e: Throwable) {
      }
    } catch (e: IOException) {
      throw IllegalStateException(e)
    }
  }
}
