package blockbit.sql.util

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface WithLogger {
  val logger: Logger
    @JsonIgnore
    get() = LoggerFactory.getLogger(javaClass)!!
}