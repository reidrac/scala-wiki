package net.usebox.net.wiki

import java.time.LocalDateTime

import org.typelevel.log4cats.Logger
import doobie._
import doobie.implicits._
import cats.effect._
import cats.implicits._
import scalatags.Text.TypedTag

trait Wiki[F[_], R] {
  type R = TypedTag[String]

  def wikiHome: String
  def init: F[Unit]
  def getPage(id: String): F[Option[R]]
  def editPage(id: String): F[Option[R]]
  def savePage(id: String, body: String): F[Int]
  def searchPage(query: String): F[R]
}

object Wiki {
  implicit def apply[F[_], R](implicit ev: Wiki[F, R]) = ev

  def impl[F[_]: Async: Logger, R]: Wiki[F, R] =
    new Wiki[F, R] {
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

      def getPage(id: String): F[Option[R]] =
        Logger[F].debug(s"getPage $id") >>
          WikiSql
            .getPage(id)
            .transact(xa)
            .map(_.map(p => WikiTemplates.show(p)))

      def editPage(id: String): F[Option[R]] =
        Logger[F].debug(s"editPage $id") >>
          WikiSql
            .getPage(id)
            .transact(xa)
            .map(_.map(p => WikiTemplates.edit(p)))

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

      def searchPage(query: String): F[R] =
        Logger[F].debug(s"searchPage $query") >>
          (if (query.isEmpty)
             WikiTemplates.searchResults("", Nil).pure[F]
           else
             WikiSql
               .search(query)
               .transact(xa)
               .map(WikiTemplates.searchResults(query, _)))

    }
}
