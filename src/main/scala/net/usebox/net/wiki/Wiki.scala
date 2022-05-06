package net.usebox.net.wiki

import java.time.LocalDateTime

import org.typelevel.log4cats.Logger
import doobie._
import doobie.implicits._
import cats.effect._
import cats.implicits._

trait Wiki[F[_]] {
  def wikiHome: String
  def init: F[Unit]
  def getPage(id: String): F[Option[Page]]
  def editPage(id: String): F[Option[Page]]
  def savePage(id: String, body: String): F[Int]
  def searchPage(query: String): F[List[Page]]
}

object Wiki {
  implicit def apply[F[_]](implicit ev: Wiki[F]) = ev

  def impl[F[_]: Async: Logger]: Wiki[F] =
    new Wiki[F] {
      val wikiHome: String = "WikiHome"

      lazy val xa = Transactor.fromDriverManager[F](
        "org.h2.Driver",
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        "sa",
        ""
      )

      def init: F[Unit] =
        Logger[F].info("Initialising the DB...") >>
          WikiSql
            .init(wikiHome)
            .transact(xa)
            .onError(error => Logger[F].error(error)("Failed to init the DB"))

      def getPage(id: String): F[Option[Page]] =
        Logger[F].debug(s"getPage $id") >>
          WikiSql
            .getPage(id)
            .transact(xa)

      def editPage(id: String): F[Option[Page]] =
        Logger[F].debug(s"editPage $id") >>
          WikiSql
            .getPage(id)
            .transact(xa)

      def savePage(id: String, body: String): F[Int] =
        Logger[F].debug(s"savePage $id") >>
          (for {
            page <- WikiSql.getPage(id)
            updated <- page.fold(WikiSql.createPage(Page(id, body)))(p =>
              WikiSql.updatePage(
                p.copy(body = body, updatedOn = LocalDateTime.now())
              )
            )
          } yield updated).transact(xa)

      def searchPage(query: String): F[List[Page]] =
        Logger[F].debug(s"searchPage $query") >>
          (if (query.isEmpty)
             Sync[F].pure(Nil)
           else
             WikiSql
               .search(query)
               .transact(xa))
    }
}
