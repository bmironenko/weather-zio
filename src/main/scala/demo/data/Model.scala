package demo.data

import java.time.Instant

object Model:

  /** Represents a measurement - a single value in a time series - annotated
    * with metadata.
    *
    * @param time
    *   measurement timestamp
    * @param label
    *   measurement "source" label (e.g. "living room", "beach house",
    *   "Baltimore, MD")
    * @param name
    *   semantic measurement descriptor (e.g. "temperature", "humidity")
    * @param unit
    *   measurement unit
    * @param value
    *   measurement value
    */
  final case class Measurement(
      time: Instant,
      label: String,
      name: String,
      unit: String,
      value: Double
  )
