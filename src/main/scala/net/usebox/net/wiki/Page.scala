package net.usebox.net.wiki

import java.time.LocalDateTime

import scala.util.Try

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

final case class Page(
    id: String,
    body: String,
    createdOn: LocalDateTime = LocalDateTime.now(),
    updatedOn: LocalDateTime = LocalDateTime.now()
)

object Page {

  val parser = Parser.builder().build()
  val renderer = HtmlRenderer.builder().build()

  implicit class PageOps(page: Page) {
    def toHtml: String =
      (for {
        document <- Try(parser.parse(page.body))
        output <- Try(renderer.render(document))
      } yield output).toOption.getOrElse("<i>error processing markdown</i>")
  }
}
