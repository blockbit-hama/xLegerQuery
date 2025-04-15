/*
 *
 *  *
 *  * Copyright OpusM Corp. All Rights Reserved.
 *  *
 *  * Written by HAMA
 *  *
 *
 */

package blockbit.sql.parser

import blockbit.sql.parser.expression.Expression

data class Statement(
  val requestedColumns: List<String>,
  val requestedTableNames: List<String>,
  val where : Expression?)