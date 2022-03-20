package net.usebox.net.wiki

import scalatags.Text.all._
import scalatags.Text.tags2.{title => headTitle}

object WikiTemplates {

  private val css = link(rel := "stylesheet", href := "/water.css")

  private val validPageId = "^[A-Z]([A-Za-z0-9]*)$".r

  def isValid(id: String): Boolean = validPageId.matches(id)

  def notFoundInvalid =
    html(
      head(
        css,
        headTitle(s"Wiki: Not Found")
      ),
      body(
        h1("Page not found"),
        p("Go to ", a(href := "/", "WikiHome")),
        hr()
      )
    )

  def notFound(id: String) =
    if (!isValid(id))
      notFoundInvalid
    else
      html(
        head(
          css,
          headTitle(s"Wiki: Not Found")
        ),
        body(
          h1("Page not found"),
          p("Try one of these options:"),
          ul(
            li("Create ", a(href := s"/$id/edit", id)),
            li("Go to ", a(href := "/", "WikiHome"))
          ),
          hr()
        )
      )

  def show(wp: Page) =
    html(
      head(
        css,
        headTitle(s"Wiki: ${wp.id}")
      ),
      body(
        form(
          method := "POST",
          action := s"/search",
          label("Search", `for` := "query"),
          input(`type` := "text", name := "query", id := "query")
        ),
        hr(),
        p(
          a(href := "/", "WikiHome"),
          " > "
        ),
        raw(wp.toHtml),
        hr(),
        span(style := "float: right;", s"Last modified: ${wp.updatedOn}"),
        form(
          method := "GET",
          action := s"/${wp.id}/edit",
          input(`type` := "submit", value := "Edit")
        )
      )
    )

  def edit(wp: Page) =
    if (!isValid(wp.id))
      notFoundInvalid
    else
      html(
        head(
          css,
          headTitle(s"Wiki: Edit ${wp.id}")
        ),
        body(
          p(
            a(href := "/", "WikiHome"),
            " > "
          ),
          h1(s"Edit Page: ${wp.id}"),
          form(
            method := "POST",
            action := s"/${wp.id}/save",
            textarea(
              name := "content",
              rows := 20,
              cols := 80,
              wp.body,
              required,
              autofocus
            ),
            p(
              input(
                style := "float: left;",
                `type` := "submit",
                value := "Save"
              ),
              input(
                style := "float: left;",
                `type` := "reset",
                value := "Reset"
              ),
              input(
                style := "margin-left: 2em;",
                `type` := "button",
                onclick := s"document.location='/${wp.id}';",
                value := "Cancel"
              )
            )
          )
        )
      )

  def searchResults(query: String, pages: List[Page]) =
    html(
      head(
        css,
        headTitle(s"Wiki: Search Results")
      ),
      body(
        p(
          a(href := "/", "WikiHome"),
          " > "
        ),
        h1("Search"),
        form(
          method := "POST",
          action := s"/search",
          label("Search", `for` := "query"),
          input(
            `type` := "text",
            name := "query",
            id := "query",
            value := query
          )
        ),
        if (pages.size == 0)
          p("No results found")
        else
          ul(
            pages.map { p =>
              li(a(href := s"/${p.id}", p.id))
            }
          )
      )
    )

}
