/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import org.jboss.shrinkwrap.api.spec.WebArchive
import io.escalante.logging.Log
import scala.xml.Elem
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import org.jboss.shrinkwrap.api.{GenericArchive, ShrinkWrap}
import io.escalante.util.Closeable
import java.io.File
import org.jboss.shrinkwrap.api.asset.{ClassLoaderAsset, StringAsset, Asset}

/**
 * Parent for static methods required for Lift tests.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
abstract class AbstractLiftWebAppTest extends Log {

  val indexHtml: Elem

  val templates: Seq[String]

  val static: Option[Elem]

  def deployment(appName: String, deployName: String,
    descriptor: String, bootClass: Class[_ <: AnyRef], bootLoader: String,
    classes: Class[_]*): WebArchive = {
    // Create deployment name
    val war = ShrinkWrap.create(classOf[WebArchive], deployName)
    info("Create war deployment: %s", deployName)

    val indexHtmlContent = xml(indexHtml)
    val webXmlContent = xml(webXml(bootLoader))

    val ideFriendlyPath = "testsuite/lift24-scala28/pom.xml"
    // Figure out an IDE and Maven friendly path:
    val path =
      if (new File(ideFriendlyPath).exists()) ideFriendlyPath else "pom.xml"

    info("Loading pom from: " + new File(path).getCanonicalPath)

    val resolver = DependencyResolvers.use(classOf[MavenDependencyResolver])
      .loadMetadataFromPom(path)

    // Add hidden templates
    templates.foreach {
      template =>
        war.addAsWebResource(resource("%s/%s".format(appName, template)), template)
    }

    static.map(staticResource =>
      war.addAsWebResource(xml(staticResource), "static/index.html"))

    war.addAsWebResource(indexHtmlContent, "index.html")
      .addAsWebResource(new StringAsset(descriptor),
          "META-INF/escalante.yml")
      .addAsWebResource(webXmlContent, "WEB-INF/web.xml")
      .addClasses(bootClass, classOf[Closeable])
      .addClasses(classes: _ *)
      .addAsLibraries(resolver
      .artifacts("org.scalatest:scalatest_2.8.2")
      .exclusion("org.scala-lang:scala-library")
      .artifacts("org.seleniumhq.selenium:selenium-htmlunit-driver")
      .resolveAs(classOf[GenericArchive]))

    info("War deployment created")
    war
  }

  private def xml(e: Elem): Asset = new StringAsset(e.toString())

  private def resource(resource: String): Asset = new ClassLoaderAsset(resource)

//  private def escalanteYaml(lift: Option[String], scala: Option[String]): String = {
//    (lift, scala) match {
//      case (Some(liftVersion), Some(scalaVersion)) =>
//        """
//          | scala:
//          |   version: %s
//          | lift:
//          |   version: %s
//        """.format(scalaVersion, liftVersion).stripMargin
//      case (Some(liftVersion), None) =>
//        """
//          | lift:
//          |   version: %s
//        """.format(liftVersion).stripMargin
//      case (None, Some(scalaVersion)) =>
//        """
//          | scala:
//          |   version: %s
//          | lift:
//        """.format(scalaVersion).stripMargin
//      case (None, None) =>
//        """
//          | lift:
//        """.stripMargin
//    }
//  }

  private def webXml(bootLoader: String): Elem = {
    <web-app version="2.5"
             xmlns="http://java.sun.com/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://java.sun.com/xml/ns/javaee
               http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">
      <filter>
        <filter-name>LiftFilter</filter-name>
        <filter-class>net.liftweb.http.LiftFilter</filter-class>
        <init-param>
          <param-name>bootloader</param-name>
          <param-value>
            {bootLoader}
          </param-value>
        </init-param>
      </filter>
      <filter-mapping>
        <filter-name>LiftFilter</filter-name>
        <url-pattern>/*</url-pattern>
      </filter-mapping>
    </web-app>
  }

}
