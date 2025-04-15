 /** Written by HAMA */

package blockbit.sql.parser.expression
import blockbit.sql.lexer.Token
import blockbit.sql.parser.BooleanValue
import blockbit.sql.parser.Parser
import blockbit.sql.parser.Value
import blockbit.sql.parser.exceptions.ParsingException
import blockbit.sql.table.*

internal class LogicalExpression(
  left: Expression?, op: Token,
  right: Expression?
) : Expression {
  private val isAnd: Boolean
  private val left: Expression?
  private val right: Expression?

  init {
    require(op === Parser.AND || op === Parser.OR)
    isAnd = op === Parser.AND
    this.left = left
    this.right = right
  }

  @Throws(ParsingException::class)
  override fun evaluate(tables: Array<Cursor?>?): Value {
    val leftValue = left?.evaluate(tables)
    val rightValue = right?.evaluate(tables)
    val l = (leftValue as BooleanValue).value()
    val r = (rightValue as BooleanValue).value()
    return BooleanValue(if (isAnd) l && r else l || r)
  }

  override fun dispatch(context: TableDispatchContext): Value {
    val leftValue = left?.dispatch(context) as ScratchTable
    val rightValue = right?.dispatch(context) as ScratchTable
    if(rightValue.isJoined){
       return leftValue.join(rightValue)
    }
    else {
      val intrimTable = context.createScratchTable(leftValue.meta?.tableName, false)
      leftValue.rowSet.union(rightValue.rowSet).forEach {
        intrimTable.insert(it)
      }
      return intrimTable
    }
  }
}
