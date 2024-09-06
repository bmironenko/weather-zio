package demo.data.doobie

import doobie.*
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*

object DriverManagerTransactor:

  def fromPrefix(prefix: String): Layer[Config.Error, Transactor[Task]] =
    ZLayer.fromZIO(
      ZIO
        .config(DriverManagerTransactorConfig.config.nested(prefix))
        .map: config =>
          Transactor.fromDriverManager[Task](
            driver = config.driver,
            url = config.url,
            user = config.username,
            password = config.password,
            logHandler = None
          )
    )
