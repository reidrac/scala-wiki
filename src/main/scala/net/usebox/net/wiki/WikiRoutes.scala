package net.usebox.net.wiki

import cats.effect.Async
import cats.implicits._
import org.http4s._
import org.http4s.headers.Location
import org.http4s.dsl.Http4sDsl
import org.http4s.scalatags._

object WikiRoutes {

  def wikiRoutes[F[_]: Async](
      wiki: Wiki[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._

    HttpRoutes.of[F] {
      case req @ GET -> Root / "water.css" =>
        StaticFile
          .fromResource("water.css", Some(req))
          .getOrElseF(NotFound())

      case req @ POST -> Root / "search" =>
        req
          .decode[UrlForm] { data =>
            data.values.get("query").flatMap(_.uncons) match {
              case Some((query, _)) =>
                wiki
                  .searchPage(query)
                  .flatMap(found =>
                    Ok(WikiTemplates.searchResults(query, found))
                  )
              case None =>
                BadRequest()
            }
          }

      case GET -> Root =>
        wiki
          .getPage(wiki.wikiHome)
          .flatMap(_.fold(NotFound())(p => Ok(WikiTemplates.show(p))))

      case GET -> Root / id =>
        wiki
          .getPage(id)
          .flatMap(
            _.fold(NotFound(WikiTemplates.notFound(id)))(p =>
              Ok(WikiTemplates.show(p))
            )
          )

      case GET -> Root / id / "edit" =>
        wiki
          .editPage(id)
          .flatMap(
            _.fold(
              NotFound(WikiTemplates.edit(Page(id, s"# $id\n")))
            )(p => Ok(WikiTemplates.edit(p)))
          )

      case req @ POST -> Root / id / "save" =>
        req
          .decode[UrlForm] { data =>
            data.values.get("content").flatMap(_.uncons) match {
              case Some((content, _)) =>
                wiki
                  .savePage(id, content)
                  .flatMap { updated =>
                    if (updated != 1)
                      InternalServerError()
                    else
                      SeeOther(
                        Location(new Uri(path = Path.unsafeFromString(s"/$id")))
                      )
                  }
              case None =>
                BadRequest()
            }
          }
    }
  }
}
