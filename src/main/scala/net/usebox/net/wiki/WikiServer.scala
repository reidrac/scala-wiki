package net.usebox.net.wiki

import org.typelevel.log4cats.Logger
import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{Logger => MiddlewareLgger}

object WikiServer {
  def stream[F[_]: Async: Logger]: Stream[F, Nothing] = {
    val wikiAlg = Wiki.impl[F]
    val httpApp = WikiRoutes.wikiRoutes[F](wikiAlg).orNotFound

    // add middleware
    val finalHttpApp = MiddlewareLgger.httpApp(false, false)(httpApp)

    for {
      _ <- Stream.eval(wikiAlg.init)
      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
