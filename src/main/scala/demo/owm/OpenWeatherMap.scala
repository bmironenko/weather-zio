package demo.owm

import demo.*
import demo.MeasUnits.*
import demo.MeasUnits.AngularMeasureUnits.*
import demo.MeasUnits.Dimensionless.*
import demo.MeasUnits.LengthUnits.*
import demo.MeasUnits.RelativeHumidityUnits.*
import demo.MeasUnits.SpeedUnits.*
import demo.MeasUnits.TemperatureUnits.*
import demo.owm.Model.*
import io.netty.handler.codec.CodecException
import zio.*
import zio.http.*
import zio.json.*
import zio.logging.loggerName
import zio.stream.ZStream

import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter

object OpenWeatherMap:

  /** Type of measurements units supported by this sensor.
    */
  type Units =
    TemperatureUnit | RelativeHumidityUnit | PressureUnit | LengthUnit |
      SpeedUnit | AngularMeasureUnit | Dimensionless

  sealed abstract class Error(message: String) extends Exception(message)

  case class ResponseDecodeError(message: String) extends Error(message)

  /** Sample weather data using the specified client.
    *
    * @param config
    *   OpenWeatherMap request configuration
    * @return
    *   `ZIO` returning the sampled weather data container
    */
  def sample(
      config: OpenWeatherMapRequest
  ): RIO[Client & Scope, CurrentWeather] =
    val request = createRequest(config)
    for
      response <- Client.request(request)
      body <- response.body.asString
      weather <- ZIO
        .fromEither(body.fromJson[CurrentWeather])
        .mapError(ResponseDecodeError.apply)
    yield weather

  /** Create a stream of weather samples.
    *
    * @param config
    *   OpenWeatherMap stream configuration
    * @return
    *   stream of weather samples
    */
  def stream(
      config: OpenWeatherMapStream
  ): ZStream[Client & Scope, Throwable, CurrentWeather] =
    val logger = loggerName(
      f"owm @ ${config.request.latitude}%.2f,${config.request.longitude}%.2f"
    )
    val dateTimeFormatter = DateTimeFormatter.ISO_INSTANT

    ZStream
      .repeatZIOWithSchedule(
        effect = sample(config.request),
        schedule = Schedule.spaced(config.sampleRate)
      )
      .tapError: e =>
        ZIO.logError(s"OWM API error: ${e.toString}") @@ logger
      .catchSome:
        case e: (IOException | CodecException) =>
          // Wait, then restart the stream
          ZStream
            .fromSchedule(Schedule.duration(config.sampleRate))
            .flatMap: _ =>
              stream(config)
      .scanZIO((Instant.EPOCH, None.asInstanceOf[Option[CurrentWeather]])):
        case ((mostRecentTimestamp, _), next) =>
          val formattedMRTimestamp =
            dateTimeFormatter.format(mostRecentTimestamp)
          val formattedNewTimestamp = dateTimeFormatter.format(next.timestamp)
          (ZIO.logDebug(
            s"Received data with timestamp $formattedNewTimestamp; prior timestamp $formattedMRTimestamp"
          ) @@ logger)
            .map: _ =>
              if next.timestamp.isAfter(mostRecentTimestamp) then
                (next.timestamp, Some(next))
              else (mostRecentTimestamp, None)
      .collect:
        case (_, Some(data)) =>
          data

  /** Create a generic stream of samples.
    *
    * @param config
    *   OpenWeatherMap stream configuration
    * @return
    *   stream of individual measurement samples
    */
  def streamGeneric(
      config: OpenWeatherMapStream
  ): ZStream[Client & Scope, Throwable, Sample[Units]] =
    stream(config)
      .flatMap: data =>
        ZStream(
          Sample(
            data.timestamp,
            TaggedMeas("temperature", Meas(data.main.temp, Fahrenheit))
          ),
          Sample(
            data.timestamp,
            TaggedMeas("humidity", Meas(data.main.humidity, PercentRH))
          ),
          Sample(
            data.timestamp,
            TaggedMeas("cloud_cover", Meas(data.clouds.cloudCover, Percent))
          ),
          Sample(
            data.timestamp,
            TaggedMeas("wind_speed", Meas(data.wind.speed, MilesPerHour))
          ),
          Sample(
            data.timestamp,
            TaggedMeas("wind_direction", Meas(data.wind.deg, DegreesOfArc))
          )
        ) ++
          ZStream(
            Seq(
              data.rain.map: rain =>
                Sample(
                  data.timestamp,
                  TaggedMeas("rain_1h", Meas(rain.oneHour, Millimeter))
                ),
              data.snow.map: snow =>
                Sample(
                  data.timestamp,
                  TaggedMeas("snow_1h", Meas(snow.oneHour, Millimeter))
                ),
              data.visibility.map: visibility =>
                Sample(
                  data.timestamp,
                  TaggedMeas("visibility", Meas(visibility, Meter))
                ),
            ).flatten*
          )

  /** Create a `Request` for a given state.
    *
    * @param config
    *   OpenWeatherMap request configuration
    */
  private def createRequest(config: OpenWeatherMapRequest): Request =
    Request
      .get(config.endpointUrl)
      .patch(
        Request.Patch(
          // FIXME: see https://github.com/zio/zio-http/issues/1025#issuecomment-1214427882
          addHeaders = Headers(Header.Connection.Close),
          addQueryParams = QueryParams(
            "appid" -> config.apiId,
            "lat" -> config.latitude.toString,
            "lon" -> config.longitude.toString,
            "units" -> "imperial"
          )
        )
      )
