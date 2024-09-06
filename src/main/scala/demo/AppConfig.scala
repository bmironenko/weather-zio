package demo

import demo.owm.OpenWeatherMapStream
import zio.Config
import zio.config.*

/** Top-level application configuration container.
  *
  * @param streams
  *   OpenWeatherMap stream configuration
  */
case class AppConfig(
    streams: List[NamedOpenWeatherMapStream]
)

object AppConfig:
  val config: Config[AppConfig] =
    Config
      .listOf(NamedOpenWeatherMapStream.config)
      .nested("streams")
      .nested("owm")
      .map(AppConfig.apply)

/** Associates a label with an OpenWeatherMap stream.
  *
  * @param name
  *   stream name
  * @param stream
  *   stream configuration
  */
case class NamedOpenWeatherMapStream(
    name: String,
    stream: OpenWeatherMapStream
)

object NamedOpenWeatherMapStream:
  val config: Config[NamedOpenWeatherMapStream] =
    (Config.string("name") ++ OpenWeatherMapStream.config.nested("stream"))
      .to[NamedOpenWeatherMapStream]
