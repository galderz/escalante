/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.usermapper

import model.User
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.sitemap.{Loc, Menu, SiteMap}
import net.liftweb.sitemap.Loc.Link
import net.liftweb.common.Full
import net.liftweb.mapper.{DB, Schemifier}
import net.liftweb.http.{Bootable, S, LiftRules}

/**
 * Boot class.
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/lift/lift_24_sbt">Lift project templates</a>
 */
class UserMapperBoot extends Bootable {

   def boot() {
      // JBoss AS7 default datasource jndi name
      DefaultConnectionIdentifier.jndiName = "java:jboss/datasources/ExampleDS"

      // Use Lift's Mapper ORM to populate the database
      // you don't need to use Mapper to use Lift... use
      // any ORM you want
      Schemifier.schemify(true, Schemifier.infoF _, User)

      // where to search snippet
      LiftRules.addToPackages("io.escalante.lift.usermapper")

      // Build SiteMap
      def sitemap = SiteMap(
         Menu.i("Home") / "index" >> User.AddUserMenusAfter, // the simple way to declare a menu

         // more complex because this menu allows anything in the
         // /static path to be visible
         Menu(Loc("Static", Link(List("static"), true, "/static/index"),
            "Static Content")))

      def sitemapMutators = User.sitemapMutator

      // set the sitemap.  Note if you don't want access control for
      // each page, just comment this line out.
      LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

      // Use jQuery 1.4
      LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

      //Show the spinny image when an Ajax call starts
      LiftRules.ajaxStart =
              Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

      // Make the spinny image go away when it ends
      LiftRules.ajaxEnd =
              Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

      // Force the request to be UTF-8
      LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

      // What is the function to test if a user is logged in?
      LiftRules.loggedInTest = Full(() => User.loggedIn_?)

      // Make a transaction span the whole HTTP request
      S.addAround(DB.buildLoanWrapper())
   }

}
