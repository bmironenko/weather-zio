package demo.data.doobie

import demo.data.DataService
import demo.data.Model.Measurement
import doobie.*
import doobie.implicits.*
import doobie.implicits.javasql.*
import zio.*
import zio.interop.catz.*

import java.sql.Timestamp
import java.time.Instant

class DoobieDataService(transactor: Transactor[Task]) extends DataService:

  private given Put[Instant] =
    Put[Timestamp].contramap: instant =>
      Option(instant)
        .map: inst =>
          new Timestamp(inst.toEpochMilli)
        .orNull

  override def getMeasurements(
      label: String,
      name: String,
      from: Instant,
      to: Instant
  ): Task[List[Measurement]] = ???

  override def addMeasurements(
      measurements: Iterable[Measurement]
  ): Task[Int] =
    if measurements.nonEmpty then
      val valuesFrag =
        measurements
          .map: m =>
            fr"(${m.time}, ${m.label}, ${m.name}, ${m.unit}, ${m.value})"
          .reduce:
            _ ++ fr", " ++ _
      val stmt =
        sql"insert into measurement (time, label, name, unit, value) values " ++ valuesFrag ++
          fr" on conflict do nothing"
      stmt.update.run.transact(transactor)
    else ZIO.succeed(0)

object DoobieDataService:
  val live: URLayer[Transactor[Task], DoobieDataService] =
    ZLayer.fromFunction(new DoobieDataService(_))
