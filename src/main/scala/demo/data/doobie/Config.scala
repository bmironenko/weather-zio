package demo.data.doobie

import zio.*
import zio.config.*

/** Transactor configuration for using JDBC DriverManager.
  *
  * @param driver
  *   driver class
  * @param url
  *   connection URL
  * @param username
  *   database username
  * @param password
  *   database password
  */
case class DriverManagerTransactorConfig(
    driver: String,
    url: String,
    username: String,
    password: String
)

object DriverManagerTransactorConfig:
  val config: Config[DriverManagerTransactorConfig] =
    (Config.string("driver") ++
      Config.string("url") ++
      Config.string("username") ++
      Config.string("password"))
      .to[DriverManagerTransactorConfig]
