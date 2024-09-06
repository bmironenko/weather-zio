package demo.data

import zio.*

import java.time.Instant

import Model.Measurement

trait DataService:

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
    *   `Task` returning the matching measurements
    */
  def getMeasurements(
      label: String,
      name: String,
      from: Instant,
      to: Instant
  ): Task[List[Measurement]]

  /** Add measurements to the data store.
    *
    * @param measurements
    *   measurements to add
    * @return
    *   `Task` returning the number of inserted rows
    */
  def addMeasurements(
      measurements: Iterable[Measurement]
  ): Task[Int]

object DataService:

  def getMeasurements(
      label: String,
      name: String,
      from: Instant,
      to: Instant
  ): RIO[DataService, List[Measurement]] =
    ZIO.serviceWithZIO[DataService](
      _.getMeasurements(label, name, from, to)
    )

  def addMeasurements(
      measurements: Iterable[Measurement]
  ): RIO[DataService, Int] =
    ZIO.serviceWithZIO[DataService](_.addMeasurements(measurements))
