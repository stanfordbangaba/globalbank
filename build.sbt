organization in ThisBuild := "com.globalbank.bookentry"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

lazy val `globalbank` = (project in file("."))
  .aggregate(`bookentry-api`, `bookentry-impl`, `bookentry-stream-api`, `bookentry-stream-impl`)

lazy val `bookentry-api` = (project in file("bookentry-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `bookentry-impl` = (project in file("bookentry-impl"))
  .enablePlugins(LagomJava, GatlingPlugin)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.1.2" % "test",
      "io.gatling"            % "gatling-test-framework"    % "3.1.2" % "test"
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`bookentry-api`)

lazy val `bookentry-stream-api` = (project in file("bookentry-stream-api"))
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `bookentry-stream-impl` = (project in file("bookentry-stream-impl"))
  .enablePlugins(LagomJava)
  .settings(common: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslKafkaClient,
      lagomLogback,
      lagomJavadslTestKit
    )
  )
  .dependsOn(`bookentry-stream-api`, `bookentry-api`)


val lombok = "org.projectlombok" % "lombok" % "1.16.18"

def common = Seq(
  javacOptions in compile += "-parameters"
)

lagomCassandraCleanOnStart := true
lagomKafkaCleanOnStart := true