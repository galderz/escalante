/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift.cluster.cometchat

import net.liftweb._

import common._
import http._
import sitemap._
import io.escalante.test.lift.cluster.cometchat.service.CentralChatServer
import net.liftweb.util.Schedule

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class ClusterCometChatBoot extends Bootable {

  override def boot() {
    // where to search snippet
    LiftRules.addToPackages(classOf[ClusterCometChatBoot].getPackage)

    // Build SiteMap
    val entries = List(
      Menu.i("Index") / "index"
    )

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(entries:_*))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
        Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
        Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // set DocType to HTML5
    LiftRules.htmlProperties.default.set((r: Req) =>new Html5Properties(r.userAgent))

    //Initial schedule to get the ball rolling :)
    Schedule(() => CentralChatServer.readChangesFeed())
  }

}
