# Minimal personal wiki with Scala

This is a simple project to get familiar with http4s.

## Stack

* HTTP server
  * [http4s](https://http4s.org/)
  * Development: `sbt ~reStart` via [sbt-revolver](https://github.com/spray/sbt-revolver)
* Storage
  * [Doobie](https://tpolecat.github.io/doobie/) with H2
* Templates
  * [ScalaTags](https://com-lihaoyi.github.io/scalatags/)
  * [commonmark-java](https://github.com/commonmark/commonmark-java) to render Markdown

## Features

* Markdown wiki
* One level pages (e.g. http://host/PageName)
  * page names must start with uppercase, can have letters and numbers
* Search
  * full text (case sensitive)
  * limited to n results, without pagination

The database is in memory, as this is mostly *a toy*. If you want persitence, you can edit `Wiki.xa` properties.

## Build

You need Java 11 or later (e.g. OpenJRE 11), and sbt.

Start the service with:

    sbt run

Then connect to http://localhost:8080/

## License

Please see [LICENSE](LICENSE).

