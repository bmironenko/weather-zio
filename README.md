# weather-zio

This is a technology demo for [ZIO](https://zio.dev) and its ecosystem, as well as integration with [Cats](https://typelevel.org/cats/)-based libraries. 

The utility uses the [OpenWeatherMap API](https://openweathermap.org/current) to stream weather data for configured locations, perform some mildly interesting transformations like batching, and record it in a Postgres database.

There are [doobie](https://typelevel.org/doobie/index.html)- and [quill](https://zio.dev/zio-quill/)-based data layer implementations. The layer of choice can be injected with `.provide()` in `Main.run()`, but `doobie` (default) is highly recommended due to much smaller binary size, as well as the ability to insert a dynamically-sized list of records in one SQL statement (i.e. `INSERT... VALUES (???), (???), (???), ...`). `Quill`'s compile-time queries are impressive, but not without limitations in practical usage.

## Usage

### Database

By default, a `postgres` database is needed to run the demo. The schema can be found [here](src/main/resources/schema.sql). 

Alternatively, to simply log data to console, one could comment out the database stage of the stream in `Main.run()`:

```scala3 
//.mapZIO:
//  writeSamples
```

### Configuration

An example configuration file is provided in the main resource directory. It is fairly self-explanatory, and allows configuring the data layers, as well as the OpenWeatherMap client.

### Run 

You can compile code with `sbt compile`, run it with `sbt run`.

### Debian package

The project can also be built as a Debian package. It installs to `/opt/weather-zio` as a `systemd` service. This is, of course, configurable in `build.sbt`. See [SBT Native Packager](https://sbt-native-packager.readthedocs.io/en/latest/) docs for reference.

```bash
sbt debian:packageBin
find target -name *.deb
```
