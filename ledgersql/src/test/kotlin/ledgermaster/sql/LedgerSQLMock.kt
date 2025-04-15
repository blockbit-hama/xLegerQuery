package blockbit.sql

import blockbit.sql.storage.DataSource
import blockbit.sql.table.*
import org.junit.jupiter.api.*

class MockDataSource(): DataSource {
  val rowSet = mutableListOf<List<String>>()
  fun insert(values: List<String>): Int {
    rowSet.add(values)
    return 1
  }

  override fun data(tableName: String?, columns: List<String>?, filter: Map<String, String>?): List<List<String>> {
    val fillteredRowSet = rowSet.filter {
       var predicate = true
       filter?.forEach { t, u ->
         if(it.get(columns!!.indexOf(t)) != u ){
           predicate = false
         }
       }
      predicate
    }
    return fillteredRowSet
  }

  override fun data(
    tableName: String?,
    columns: List<String>?,
    column: String,
    start: String,
    end: String
  ): List<List<String>> {
    TODO("Not yet implemented")
  }
}

class MockTableFactory(override val source: DataSource) : TableFactory {
  override fun create(meta: TableMetaData): Table {
      return ConcreteTable(meta.tableName, meta.columnNames)
  }

  override fun createScratchTable(meta: TableMetaData?, empty: Boolean): ScratchTable {
    if (empty)
      return ScratchTable(meta, null)

    if (meta?.tableName == "client") {
      val name = "client"
      val columns = listOf("id", "name", "couponid", "count")
      val source = MockDataSource()
      source.insert(listOf("1", "john", "coupon_a", "10"))
      source.insert(listOf("2", "john", "coupon_b", "100"))
      source.insert(listOf("3", "paul", "coupon_c", "3"))
      source.insert(listOf("4", "alice", "coupon_c", "4"))
      source.insert(listOf("5", "tom", "coupon_d", "5"))
      source.insert(listOf("6", "john", "coupon_a", "8"))
      val table = ScratchTable(meta, source)
      return table
    } else if (meta?.tableName == "coupon") {
      val name = "coupon"
      val columns = listOf("couponid", "kind", "quantity")
      val source = MockDataSource()
      source.insert(listOf("coupon_a", "car", "1"))
      source.insert(listOf("coupon_b", "glassess", "2"))
      source.insert(listOf("coupon_c", "snack", "3"))
      source.insert(listOf("coupon_d", "department", "4"))
      val table = ScratchTable(meta, source)
      return table
    } else {
      return ScratchTable(null, null)
    }
  }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LedgerSQLTest: AbstractIT(){
  private lateinit var ledgerSQL: LedgerSQL

  @BeforeAll
  fun setUp() {
    val tablemetas = mutableListOf<TableMetaData>()
    tablemetas.add(TableMetaData("client", listOf("id", "name", "couponid","count"),"1"))
    tablemetas.add(TableMetaData("coupon", listOf("couponid","kind", "quantity"),"2"))
    ledgerSQL = LedgerSQL(
      endpoint,
      privateKey,
      "",
      tablemetas,
      MockTableFactory(MockDataSource()))
  }

  @Test
  fun `sql1_all(*) column and "and" operation`(){
    /***
     * client
     * -----------------------------------
     * id  |  name  |  couponid |   count
     * -----------------------------------
     * "1"  "john"    "coupon_a"   "10"
     * "2"  "john"    "coupon_b"   "100"
     * "3"  "paul"    "coupon_c"   "3"
     * "4"  "alice"   "coupon_c"   "4"
     * "5"  "tom"     "coupon_d"   "5"
     * "6"  "john"    "coupon_a"   "8"
     * -----------------------------------
     *
     * coupon
     * -----------------------------------
     * couponid  |  kind  |  quantity
     * -----------------------------------
     * "coupon_a"  "car"    "1"
     * "coupon_b"  "toy"    "2"
     * "coupon_c"  "meat"   "3"
     * "coupon_d"  "snack"  "4"
     *-------------------------------------
     */

    val cursor = ledgerSQL.sql("select * from client where name = 'john' and couponid = 'coupon_a'" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(2, count)
  }

  @Test
  fun `sql2_specific column and using "or" "brace" `(){
    /***
     * client
     * -----------------------------------
     * id  |  name  |  couponid |   count
     * -----------------------------------
     * "1"  "john"    "coupon_a"   "10"
     * "2"  "john"    "coupon_b"   "100"
     * "3"  "paul"    "coupon_c"   "3"
     * "4"  "alice"   "coupon_c"   "4"
     * "5"  "tom"     "coupon_d"   "5"
     * "6"  "john"    "coupon_a"   "8"
     * -----------------------------------
     *
     * coupon
     * -----------------------------------
     * couponid  |  kind  |  quantity
     * -----------------------------------
     * "coupon_a"  "car"    "1"
     * "coupon_b"  "toy"    "2"
     * "coupon_c"  "meat"   "3"
     * "coupon_d"  "snack"  "4"
     *-------------------------------------
     */

    val cursor = ledgerSQL.sql("select id from client where (name = 'john' and couponid = 'coupon_a') or name = 'alice'" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(3, count)
  }

  @Test
  fun `sql3_"compare" expresion `(){
    /***
     * client
     * -----------------------------------
     * id  |  name  |  couponid |   count
     * -----------------------------------
     * "1"  "john"    "coupon_a"   "10"
     * "2"  "john"    "coupon_b"   "100"
     * "3"  "paul"    "coupon_c"   "3"
     * "4"  "alice"   "coupon_c"   "4"
     * "5"  "tom"     "coupon_d"   "5"
     * "6"  "john"    "coupon_a"   "8"
     * -----------------------------------
     *
     * coupon
     * -----------------------------------
     * couponid  |  kind  |  quantity
     * -----------------------------------
     * "coupon_a"  "car"    "1"
     * "coupon_b"  "toy"    "2"
     * "coupon_c"  "meat"   "3"
     * "coupon_d"  "snack"  "4"
     *-------------------------------------
     */

    val cursor = ledgerSQL.sql("select * from coupon where quantity > 2" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(2, count)
  }

  @Test
  fun `sql4_"like" expresion `(){
    /***
     * client
     * -----------------------------------
     * id  |  name  |  couponid |   count
     * -----------------------------------
     * "1"  "john"    "coupon_a"   "10"
     * "2"  "john"    "coupon_b"   "100"
     * "3"  "paul"    "coupon_c"   "3"
     * "4"  "alice"   "coupon_c"   "4"
     * "5"  "tom"     "coupon_d"   "5"
     * "6"  "john"    "coupon_a"   "8"
     * -----------------------------------
     *
     * coupon
     * -----------------------------------
     * couponid  |  kind  |  quantity
     * -----------------------------------
     * "coupon_a"  "car"    "1"
     * "coupon_b"  "toy"    "2"
     * "coupon_c"  "meat"   "3"
     * "coupon_d"  "snack"  "4"
     *-------------------------------------
     */

    val cursor = ledgerSQL.sql("select * from client where name like 'p%'" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(1, count)
  }

  @Test
  fun `sql5_"not" expression`(){
    /***
     * client
     * -----------------------------------
     * id  |  name  |  couponid |   count
     * -----------------------------------
     * "1"  "john"    "coupon_a"   "10"
     * "2"  "john"    "coupon_b"   "100"
     * "3"  "paul"    "coupon_c"   "3"
     * "4"  "alice"   "coupon_c"   "4"
     * "5"  "tom"     "coupon_d"   "5"
     * "6"  "john"    "coupon_a"   "8"
     * -----------------------------------
     *
     * coupon
     * -----------------------------------
     * couponid  |  kind  |  quantity
     * -----------------------------------
     * "coupon_a"  "car"    "1"
     * "coupon_b"  "toy"    "2"
     * "coupon_c"  "meat"   "3"
     * "coupon_d"  "snack"  "4"
     *-------------------------------------
     */

    val cursor = ledgerSQL.sql("select * from client where NOT(name = 'paul')" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(5, count)
  }

}