package net.usebox.net.wiki

import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect._

object Main extends IOApp {
  implicit def unsafeLogger[F[_]: Sync] = Slf4jLogger.getLogger[F]

  def run(args: List[String]) =
    WikiServer.stream[IO].compile.drain.as(ExitCode.Success)
}
