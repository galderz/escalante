/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift.cluster.containervar

import net.liftweb.http.{LiftRules, Bootable}
import net.liftweb.sitemap.{SiteMap, Loc, Menu}

/**
 * Boot class.
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/lift/lift_24_sbt">Lift project templates</a>
 */
class ClusterContainerVarBoot extends Bootable {

  def boot() {
    LiftRules.addToPackages("io.escalante.test.lift.cluster.containervar")

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: Nil
    LiftRules.setSiteMap(SiteMap(entries: _*))
  }

}
