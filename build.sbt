ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

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
          "/opt//weather-zio/conf/application.conf"
        )
      )
        .withConfig("noreplace")
        .withUser(daemonUser.value)
        .withGroup(daemonGroup.value)
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC5",
      "dev.zio" %% "zio" % "2.1.7",
      "dev.zio" %% "zio-config" % "4.0.2",
      "dev.zio" %% "zio-config-typesafe" % "4.0.2",
      "dev.zio" %% "zio-logging" % "2.3.0",
      "dev.zio" %% "zio-json" % "0.7.2",
      "dev.zio" %% "zio-http" % "3.0.0-RC9",
      "dev.zio" %% "zio-interop-cats" % "23.1.0.3",
      "io.getquill" %% "quill-jdbc-zio" % "4.8.5",
      "com.typesafe" % "config" % "1.4.3",
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.slf4j" % "slf4j-simple" % "2.0.16",
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
