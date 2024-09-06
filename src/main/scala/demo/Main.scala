package demo

import demo.data.DataService
import demo.data.Model.Measurement
import demo.data.doobie.DoobieDataService
import demo.data.doobie.DriverManagerTransactor
import demo.owm.OpenWeatherMap
import zio.*
import zio.config.*
import zio.config.typesafe.TypesafeConfigProvider
import zio.http.Client
import zio.http.DnsResolver
import zio.http.ZClient
import zio.http.netty.NettyConfig
import zio.http.netty.client.NettyClientDriver
import zio.logging.consoleLogger
import zio.logging.loggerName
import zio.stream.ZStream

object Main extends ZIOAppDefault:

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(
      TypesafeConfigProvider.fromResourcePath()
    ) >>>
      Runtime.removeDefaultLoggers >>>
      consoleLogger()

  private val httpClientConfigLayer: ULayer[ZClient.Config] =
    ZLayer.succeed(Client.Config.default)

  private val nettyConfigLayer: ULayer[NettyConfig] =
    ZLayer.succeed(NettyConfig.default)

  private val httpClientLayer: Layer[Throwable, Client] =
    ZLayer.make[Client](
      nettyConfigLayer,
      DnsResolver.default,
      NettyClientDriver.live,
      httpClientConfigLayer,
      Client.customized
    )

  def run: RIO[ZIOAppArgs & Scope, ExitCode] =
    for
      config <- ZIO.config(AppConfig.config)
      result <-
        config.streams
          .map:
            createOpenWeatherMapStream
          .fold(ZStream.empty):
            _ merge _
          .tap: s =>
            ZIO.logInfo(s.toString)
          .groupedWithin(100, 30.seconds)
          .mapZIO:
            writeSamples
          .foreach: n =>
            ZIO.logInfo(s"Wrote $n samples to the database")
          .foldCauseZIO(
            cause => ZIO.logErrorCause(cause) *> ZIO.succeed(ExitCode.failure),
            _ => ZIO.succeed(ExitCode.success)
          )
          .provide(
            httpClientLayer,
            DoobieDataService.live,
            DriverManagerTransactor.fromPrefix("doobie"),
            ZLayer.fromZIO(Scope.make)
            // QuillDataService.live
            // QuillPostgresContext.live,
            // Quill.DataSource.fromPrefix("quill"),
          ) @@ loggerName("weather-zio")
    yield result

  /** Create an OpenWeatherMap sample stream.
    *
    * @param config
    *   stream configuration
    * @return
    *   stream producing (stream name, sample) tuples
    */
  private def createOpenWeatherMapStream(
      config: NamedOpenWeatherMapStream
  ): ZStream[
    Client & Scope,
    Throwable,
    (String, Sample[OpenWeatherMap.Units])
  ] =
    OpenWeatherMap
      .streamGeneric(config.stream)
      .map: s =>
        (config.name, s)

  /** Write samples to persistent storage.
    *
    * @param samples
    *   (label, sample) tuples
    * @return
    *   persistence I/O
    */
  private def writeSamples(
      samples: Iterable[(String, Sample[?])]
  ): RIO[DataService, Int] =
    DataService.addMeasurements(
      samples.view.map:
        case (label, sample) =>
          Measurement(
            sample.time,
            label = label,
            name = sample.tagged.name,
            unit = sample.tagged.meas.unit.toString,
            value = sample.tagged.meas.value
          )
    )
