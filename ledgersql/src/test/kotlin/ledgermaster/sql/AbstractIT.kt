package blockbit.sql

import abc.ethereum.EthereumPrivateKeyImporter.from

abstract class AbstractIT : AbstractTestCase() {
  val endpoint = "http://169.56.99.100:8545"
  val privateKey = "0x00b9550d1b6a455c9a7dd78f61c3fc18c7adbb516d059753e048fad094986fb8"
  val store_contract = "0x4b9fa78b63fcd623fd5264a04593c6e5684f6fb2"
  val client_contract =  "0xb4ad33ae1e5ced642562a5610efbd9b439a7534"
  val coupon_contract =  "0xe2f72675a485307af3aa4b5b2a30b706c4e0d5fb"

}