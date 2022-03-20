package net.usebox.net.wiki

import java.time.LocalDateTime

import org.typelevel.log4cats.Logger
import doobie._
import doobie.implicits._
import cats.effect._
import cats.implicits._
import scalatags.Text.TypedTag

trait Wiki[F[_]] {
  type R = TypedTag[String]

  var wikiHome: String = "WikiHome"
  def init: F[Unit]
  def getPage(id: String): F[Option[R]]
  def editPage(id: String): F[Option[R]]
  def savePage(id: String, body: String): F[Int]
  def searchPage(query: String): F[R]
}

object Wiki {
  implicit def apply[F[_]](implicit ev: Wiki[F]): Wiki[F] = ev

  def impl[F[_]: Async: Logger]: Wiki[F] = new Wiki[F] {
    val S = WikiSql
    val T = WikiTemplates

    lazy val xa = Transactor.fromDriverManager[F](
      "org.h2.Driver",
      "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
      "sa",
      ""
    )

    def init: F[Unit] =
      Logger[F].info("Initialising the DB...") >>
        S.init(wikiHome)
          .transact(xa)
          .onError(error => Logger[F].error(error)("Failed to init the DB"))

    def getPage(id: String): F[Option[R]] =
      Logger[F].debug(s"getPage $id") >>
        S.getPage(id).transact(xa).map(_.map(p => T.show(p)))

    def editPage(id: String): F[Option[R]] =
      Logger[F].debug(s"editPage $id") >>
        S.getPage(id).transact(xa).map(_.map(p => T.edit(p)))

    def savePage(id: String, body: String): F[Int] =
      Logger[F].debug(s"savePage $id") >>
        (for {
          page <- S.getPage(id)
          updated <- page.fold(S.createPage(Page(id, body)))(p =>
            S.updatePage(p.copy(body = body, updatedOn = LocalDateTime.now()))
          )
        } yield updated).transact(xa)

    def searchPage(query: String): F[R] =
      Logger[F].debug(s"searchPage $query") >>
        (if (query.isEmpty)
           T.searchResults("", Nil).pure[F]
         else
           S.search(query).transact(xa).map(T.searchResults(query, _)))

  }
}
