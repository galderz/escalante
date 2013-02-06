/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift.jpa

import net.liftweb.common.{Full,LazyLoggable}
import net.liftweb.http.{Bootable, LiftRules, RedirectResponse}
import net.liftweb.sitemap.{SiteMap,Menu}
import net.liftweb.sitemap.Loc.{EarlyResponse,Hidden}

/**
 * JPA Library boot class.
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/timperrett/lift-in-action">
 *        Lift In Action book</a>
 */
class JpaBoot extends Bootable with LazyLoggable {

  def boot {
    LiftRules.addToPackages("io.escalante.test.lift.jpa")
    
    /**
     * Set the character encoding to utf-8 early in the pipline
     */
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    /**
     * Build the sitemap
     */
    LiftRules.setSiteMap(SiteMap(
      // Menu("Home") / "index" >> EarlyResponse(() => Full(RedirectResponse("/"))) >> Hidden,
      Menu("Java Enterprise Integration") / "index" submenus(
        Menu("JPA: Authors: List") / "authors" / "index",
        Menu("JPA: Authors: Add") / "authors" / "add",
        Menu("JPA: Books: Add") / "books" / "add"
      )
    ))

  }
}
