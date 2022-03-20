package net.usebox.net.wiki

import doobie._
import doobie.implicits._
import cats.effect._
import doobie.implicits.javatimedrivernative._

object WikiSql {

  val searchLimit = 16

  def init(wikiHome: String): ConnectionIO[Unit] =
    for {
      _ <- createPageTable
      page <- getPage(wikiHome)
      _ <- Sync[ConnectionIO].whenA(page.isEmpty)(
        createPage(
          Page(
            wikiHome,
            "# Welcome!\n\nThis is your *WikiHome*.\n"
          )
        )
      )
    } yield ()

  def createPage(
      page: Page
  ): ConnectionIO[Int] = {
    sql"""insert into page values(
          ${page.id},
          ${page.body},
          ${page.createdOn},
          ${page.updatedOn}
          )""".update.run
  }

  def updatePage(
      page: Page
  ): ConnectionIO[Int] = {
    sql"""update page set
          body = ${page.body},
          updated_on = ${page.updatedOn}
          where id = ${page.id}
          """.update.run
  }

  def getPage(
      id: String
  ): ConnectionIO[Option[Page]] =
    sql"""select id, body, created_on, updated_on
          from page
          where id = $id"""
      .query[Page]
      .option

  def search(
      query: String
  ): ConnectionIO[List[Page]] = {
    val q = "%" + query.replace("%", "\\%") + "%"
    sql"""select id, body, created_on, updated_on
          from page
          where body like $q
          order by updated_on desc limit $searchLimit"""
      .query[Page]
      .to[List]
  }

  def createPageTable: ConnectionIO[Int] =
    sql"""create table if not exists page(
          id varchar,
          body varchar,
          created_on timestamp,
          updated_on timestamp,
          primary key(id)
        )""".update.run
}
