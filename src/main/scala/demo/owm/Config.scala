package demo.owm

import demo.ConfigExtensions
import zio.Config
import zio.Duration
import zio.config.*
import zio.http.URL

/** OpenWeatherMap request configuration.
  *
  * @param endpointUrl
  *   Current Weather v2.5 API endpoint
  * @param apiId
  *   API key
  * @param latitude
  *   query location latitude
  * @param longitude
  *   query location longitude
  */
case class OpenWeatherMapRequest(
    endpointUrl: URL,
    apiId: String,
    latitude: Double,
    longitude: Double
)

object OpenWeatherMapRequest:
  val config: Config[OpenWeatherMapRequest] =
    (ConfigExtensions.url("endpointUrl") ++
      Config.string("apiId") ++
      Config.double("latitude") ++
      Config.double("longitude"))
      .to[OpenWeatherMapRequest]

/** OpenWeatherMap stream configuration.
  *
  * @param request
  *   request configuration
  * @param sampleRate
  *   sample rate
  */
case class OpenWeatherMapStream(
    request: OpenWeatherMapRequest,
    sampleRate: Duration
)

object OpenWeatherMapStream:
  val config: Config[OpenWeatherMapStream] =
    (OpenWeatherMapRequest.config.nested("request") ++ Config.duration(
      "sampleRate"
    ))
      .to[OpenWeatherMapStream]
