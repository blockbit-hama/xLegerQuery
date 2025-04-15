package blockbit.sql

import blockbit.sql.table.*
import org.junit.jupiter.api.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LedgerSQLIT: AbstractIT(){
  private lateinit var ledgerSQL: LedgerSQL

  @BeforeAll
  fun setUp() {
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
    val tableInfo = mutableListOf<TableMetaData>()
    tableInfo.add(TableMetaData("client", listOf("id", "name", "couponid","count"), client_contract))
    tableInfo.add(TableMetaData("coupon", listOf("couponid","kind", "quantity"), coupon_contract))

    ledgerSQL = LedgerSQL(
      endpoint,
      privateKey,
      store_contract,
      null,
      null
    )
  }

  @Test
  fun `sql1_all(*) column and "and" operation`(){

    val cursor = ledgerSQL.sql("select * from client where name = 'john' and couponid = 'coupon_a'" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(2, count)

    val cursor2 = ledgerSQL.sql("select * from client where name = 'john' and couponid = 'coupon_a'" )

  }

  @Test
  fun `sql2_specific column and using "or" "brace" `(){

    val cursor = ledgerSQL.sql("select id from client where (name = 'john' and couponid = 'coupon_a') or name = 'alice'" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(3, count)
  }

  @Test
  fun `sql3_"compare" expresion `(){

    val cursor = ledgerSQL.sql("select * from coupon where quantity > 2" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(2, count)
  }

  @Test
  fun `sql4_"like" expresion `(){

    val cursor = ledgerSQL.sql("select * from client where name like 'p%'" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(1, count)
  }


  @Test
  fun `sql_"between" expression`(){

    val cursor = ledgerSQL.sql("select * from client where count between '0' and '9' " )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(4, count)
  }

  @Test
  fun `sql5_"not" expression`(){

    val cursor = ledgerSQL.sql("select * from client where NOT(name = 'paul')" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(5, count)
  }

  @Test
  fun `sql5_"join"`(){

    val cursor = ledgerSQL.sql("select * from client , coupon where client.id = '1' and (client.couponid = coupon.couponid)" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(1, count)
  }

  @Test
  fun `sql5_"join"_with_specific column `(){

    val cursor = ledgerSQL.sql("select id, name, couponid from client , coupon where client.id = '1' and (client.couponid = coupon.couponid)" )
    var count = 0
    while(cursor?.advance()?: false){
      count++
    }

    Assertions.assertEquals(1, count)
  }
}