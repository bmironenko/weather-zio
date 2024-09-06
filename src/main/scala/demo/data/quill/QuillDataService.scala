package demo.data.quill

import demo.data.DataService
import demo.data.Model.*
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

import java.sql.SQLException
import java.time.Instant

class QuillDataService(context: QuillPostgresContext) extends DataService:
  import context.*

  private inline def measurements = quote(query[Measurement])

  /** Load measurements from the data store.
    *
    * @param label
    *   measurement label
    * @param name
    *   measurement name
    * @param from
    *   start timestamp
    * @param to
    *   end timestamp
    * @return
    *   `ZIO` containing the matching measurements
    */
  def getMeasurements(
      label: String,
      name: String,
      from: Instant,
      to: Instant
  ): Task[List[Measurement]] =
    context.run(
      measurements.filter: m =>
        m.label == lift(label) &&
          m.name == lift(name) &&
          m.time >= lift(from) &&
          m.time <= lift(to)
    )

  /** Add measurements to the data store.
    *
    * @param measurements
    *   measurements to add
    * @return
    *   `ZIO` containing the number of inserted rows
    */
  def addMeasurements(
      measurements: Iterable[Measurement]
  ): Task[Int] =
    context
      .run(
        liftQuery(measurements)
          .foreach: m =>
            query[Measurement]
              .insertValue(m)
              .returningGenerated(_ => 1),
        100
      )
      .map:
        _.sum

object QuillDataService:
  val live: URLayer[QuillPostgresContext, QuillDataService] =
    ZLayer.fromFunction(new QuillDataService(_))
