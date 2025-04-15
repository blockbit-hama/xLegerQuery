 /** Written by HAMA */

package blockbit.sql.parser.exceptions

class ParsingException(
    message: String?,
    private val inputLine: String,
    private val inputPosition: Int,
    private val inputLineNumber: Int
) : Exception(message) {

    val errorReport: String
        get() {
            val b = StringBuffer()
            b.append("Line ")
            b.append("$inputLineNumber:\n")
            b.append(inputLine)
            b.append("\n")
            for (i in 0 until inputPosition) b.append("_")
            b.append("^\n")
            return b.toString()
        }
}