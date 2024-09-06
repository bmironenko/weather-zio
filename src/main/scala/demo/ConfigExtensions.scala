package demo

import cats.implicits.*
import zio.Config
import zio.http.URL

/** Extensions for `zio.Config`.
  */
object ConfigExtensions:

  /** Configuration value that resolves to a URL.
    */
  def url: Config[URL] =
    Config.string.mapOrFail: s =>
      URL
        .decode(s)
        .bimap[Config.Error, URL](
          _ => Config.Error.InvalidData(message = "invalid endpoint URI"),
          identity
        )

  /** Configuration for a nested value that resolves to a URL.
    *
    * @param name
    *   nested value name
    */
  def url(name: String): Config[URL] = url.nested(name)
