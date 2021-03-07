package com.github.ajablonski

import com.github.ajablonski.components.LeafletMapManager
import com.raquo.laminar.api.L._
import org.scalajs.dom


object Main {
  val defaultRoute = "22"
  val routeStream = Var(defaultRoute)
  private val routesEventStream = Streams.routesEventStream()

  def main(args: Array[String]): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      val appContainer = dom.document.querySelector("body")
      val appElement = div(
        div(
          idAttr := "app",
          h1("Hello world"),
          select(
            idAttr := "routes",
            children <-- routesEventStream,
            controlled(
              value <-- routeStream.signal,
              onChange.mapToValue --> routeStream.writer
            )
          )
        ),
        new LeafletMapManager(routeStream.signal).render()
      )
      render(appContainer, appElement)
    }(unsafeWindowOwner)
  }
}
