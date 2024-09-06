package demo.data.quill

import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.URLayer
import zio.ZLayer

import java.sql.Timestamp as SqlTimestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.sql.DataSource
import scala.annotation.targetName

class QuillPostgresContext(ds: DataSource)
    extends Quill.Postgres(SnakeCase, ds):

  private val UTC = ZoneId.of("UTC")

  given MappedEncoding[Instant, SqlTimestamp] =
    MappedEncoding[Instant, SqlTimestamp]: instant =>
      SqlTimestamp.valueOf(LocalDateTime.ofInstant(instant, UTC))

  given MappedEncoding[SqlTimestamp, Instant] =
    MappedEncoding[SqlTimestamp, Instant]: timestamp =>
      timestamp.toLocalDateTime.atZone(UTC).toInstant

  extension (inline left: Instant)
    @targetName("gt")
    inline def >(right: Instant): Quoted[Boolean] = quote(
      infix"$left > $right".as[Boolean]
    )

    @targetName("lt")
    inline def <(right: Instant): Quoted[Boolean] = quote(
      infix"$left < $right".as[Boolean]
    )

    @targetName("gte")
    inline def >=(right: Instant): Quoted[Boolean] = quote(
      infix"$left >= $right".as[Boolean]
    )

    @targetName("lte")
    inline def <=(right: Instant): Quoted[Boolean] = quote(
      infix"$left <= $right".as[Boolean]
    )

object QuillPostgresContext:
  val live: URLayer[DataSource, QuillPostgresContext] =
    ZLayer.fromFunction(new QuillPostgresContext(_))
