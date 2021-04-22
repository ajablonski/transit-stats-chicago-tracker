package com.github.ajablonski

import com.github.ajablonski.components.{LeafletMapManager, RouteDropDown}
import com.raquo.laminar.api.L._
import org.scalajs.dom


object Main {

  def main(args: Array[String]): Unit = {
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(
        dom.document.querySelector("body"),
        div(
          div(
            idAttr := "app",
            h1("Hello world"),
            new RouteDropDown(StateStreams.routeListStream, StateStreams.currentRouteStream).render()
          ),
          new LeafletMapManager(StateStreams.routeListStream, StateStreams.currentRouteStream.signal).render()
        ))
    }(unsafeWindowOwner)
  }
}
