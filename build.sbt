ThisBuild / version := "0.3.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.2"

lazy val root = (project in file("."))
  .enablePlugins(
    JavaServerAppPackaging,
    SystemdPlugin,
    DebianPlugin,
    DebianDeployPlugin,
    SystemloaderPlugin
  )
  .settings(
    name := "weather-zio",
    resolvers += "jitpack" at "https://jitpack.io",
    defaultLinuxInstallLocation := "/opt",
    debianPackageDependencies := Seq("openjdk-17-jre-headless"),
    daemonUser := "weather-zio",
    daemonGroup := "weather-zio",
    maintainer := "Basil Mironenko",
    linuxPackageMappings ++= Seq(
      packageTemplateMapping("var/lib/weather-zio")()
        .withUser(daemonUser.value)
        .withGroup(daemonGroup.value),
      packageTemplateMapping("var/log//weather-zio")()
        .withUser(daemonUser.value)
        .withGroup(daemonGroup.value),
      packageMapping(
        (
          file("src/main/resources/application.conf"),
          "/opt/weather-zio/conf/application.conf"
        )
      )
        .withConfig("noreplace")
        .withUser(daemonUser.value)
        .withGroup(daemonGroup.value)
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC10",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC10",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC10",
      "dev.zio" %% "zio" % "2.1.20",
      "dev.zio" %% "zio-config" % "4.0.4",
      "dev.zio" %% "zio-config-typesafe" % "4.0.4",
      "dev.zio" %% "zio-logging" % "2.5.1",
      "dev.zio" %% "zio-json" % "0.7.44",
      "dev.zio" %% "zio-http" % "3.3.3",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.5",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.6",
      "com.typesafe" % "config" % "1.4.4",
      "org.postgresql" % "postgresql" % "42.7.7",
      "org.slf4j" % "slf4j-simple" % "2.0.17",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    run / fork := true,
    Compile / scalacOptions := Seq(
      "-deprecation",
      "-unchecked",
      "-feature"
    ),
    Compile / doc / sources := Seq.empty
  )
