package demo.owm

import zio.json.*

import java.time.Instant

/** OpenWeatherMap Current Weather Data API v2.5
  *
  * Implemented using zio-json automatic decoder derivation.
  */
object Model:

  @jsonHint("coord")
  case class Coordinates(
      @jsonField("lon") longitude: Double,
      @jsonField("lat") latitude: Double
  )

  object Coordinates:
    given JsonDecoder[Coordinates] =
      DeriveJsonDecoder.gen[Coordinates]

  @jsonHint("weather")
  case class Weather(id: Int, main: String, description: String, icon: String)

  object Weather:
    given JsonDecoder[Weather] =
      DeriveJsonDecoder.gen[Weather]

  @jsonHint("main")
  case class Main(
      @jsonField("temp") temp: Double,
      @jsonField("feels_like") tempFeelsLike: Double,
      @jsonField("temp_min") tempMin: Double,
      @jsonField("temp_max") tempMax: Double,
      @jsonField("pressure") pressure: Double,
      @jsonField("humidity") humidity: Double,
      @jsonField("sea_level") pressureAtSeaLevel: Option[Double],
      @jsonField("grnd_level") pressureAtGroundLevel: Option[Double]
  )

  object Main:
    given JsonDecoder[Main] =
      DeriveJsonDecoder.gen[Main]

  @jsonHint("wind")
  case class Wind(
      @jsonField("speed") speed: Double,
      @jsonField("deg") deg: Double,
      @jsonField("gust") gust: Option[Double]
  )

  object Wind:
    given JsonDecoder[Wind] =
      DeriveJsonDecoder.gen[Wind]

  @jsonHint("rain")
  case class Rain(
      @jsonField("1h") oneHour: Double,
      @jsonField("3h") threeHours: Option[Double]
  )

  object Rain:
    given JsonDecoder[Rain] =
      DeriveJsonDecoder.gen[Rain]

  @jsonHint("snow")
  case class Snow(
      @jsonField("1h") oneHour: Double,
      @jsonField("3h") threeHours: Option[Double]
  )

  object Snow:
    given JsonDecoder[Snow] =
      DeriveJsonDecoder.gen[Snow]

  @jsonHint("clouds")
  case class Clouds(
      @jsonField("all") cloudCover: Double
  )

  object Clouds:
    given JsonDecoder[Clouds] =
      DeriveJsonDecoder.gen[Clouds]

  case class CurrentWeather(
      @jsonField("dt") timestamp: Instant,
      coord: Coordinates,
      weather: List[Weather],
      main: Main,
      wind: Wind,
      clouds: Clouds,
      rain: Option[Rain],
      snow: Option[Snow],
      visibility: Option[Double]
  )

  object CurrentWeather:
    given JsonDecoder[Instant] =
      JsonDecoder[Long].map(Instant.ofEpochSecond)

    given JsonDecoder[CurrentWeather] =
      DeriveJsonDecoder.gen[CurrentWeather]

  /*
  // Alternative cursor-base decoding:

  case class WeatherData(
      timestamp: Instant,
      temperature: Meas[Fahrenheit],
      humidity: Meas[PercentRH],
      pressure: Meas[Hectopascal],
      visibility: Meas[Meter],
      cloudCover: Meas[Percent],
      windSpeed: Meas[MilesPerHour],
      windDirection: Meas[DegreesOfArc],
      trailingHourRain: Option[Meas[Millimeter]],
      trailingHourSnow: Option[Meas[Millimeter]]
  )

  object WeatherData {
    import JsonCursor._
    def decode(input: String): Either[String, WeatherData] = {
      for {
        ast <- input.fromJson[Json]

        timestamp <- ast
          .get(field("dt"))
          .flatMap(_.as[Long].map(Instant.ofEpochSecond))

        mainAst <- ast.get(field("main").isObject)
        temp <- mainAst
          .get(field("temp"))
          .flatMap(_.as[Double].map(Meas(_, Fahrenheit)))
        humidity <- mainAst
          .get(field("humidity"))
          .flatMap(_.as[Double].map(Meas(_, PercentRH)))
        pressure <- mainAst
          .get(field("pressure"))
          .flatMap(_.as[Double].map(Meas(_, Hectopascal)))

        visibility <- ast
          .get(field("visibility"))
          .flatMap(_.as[Double].map(Meas(_, Meter)))

        clouds <- ast
          .get(field("clouds").isObject.field("all"))
          .flatMap(_.as[Double].map(Meas(_, Percent)))

        windAst <- ast.get(field("wind").isObject)
        windSpeed <- windAst
          .get(field("speed"))
          .flatMap(_.as[Double].map(Meas(_, MilesPerHour)))
        windDeg <- windAst
          .get(field("deg"))
          .flatMap(_.as[Double].map(Meas(_, DegreesOfArc)))

        rain <- ast
          .get(field("rain").isObject)
          .fold[Either[String, Option[Double]]](
            _ => Either.right(None),
            _.get(field("1h")).flatMap(_.as[Option[Double]])
          )
          .map(_.map(Meas(_, Millimeter)))

        snow <- ast
          .get(field("snow").isObject)
          .fold[Either[String, Option[Double]]](
            _ => Either.right(None),
            _.get(field("1h")).flatMap(_.as[Option[Double]])
          )
          .map(_.map(Meas(_, Millimeter)))

      } yield WeatherData(
        timestamp,
        temp,
        humidity,
        pressure,
        visibility,
        clouds,
        windSpeed,
        windDeg,
        rain,
        snow
      )
    }
  }
   */
