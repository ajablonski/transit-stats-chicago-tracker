package com.github.ajablonski

import com.github.ajablonski.Streams.routeStream
import com.github.ajablonski.components.LeafletMapManager
import com.raquo.domtypes.generic.codecs.StringAsIsCodec
import com.raquo.laminar.api.L._
import org.scalajs.dom


object Main {

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

  private val routesEventStream = Streams.routesEventStream().map {
    _.map { route =>
      val opt = option(
        customHtmlAttr("label", StringAsIsCodec) := f"${route.routeId}: ${route.name}",
        value := route.routeId,
        f"${route.routeId}: ${route.name}"
      )
      if (route.routeId == routeStream.now()) {
        opt.amend(selected := true)
      } else {
        opt
      }
    }
  }
}
