/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift

import java.io.File
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.api.{GenericArchive, ShrinkWrap}
import io.escalante.logging.Log
import scala.xml.{Node, Elem}
import org.jboss.shrinkwrap.api.asset.{StringAsset, ClassLoaderAsset, Asset}
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import scala.collection.JavaConversions._
import io.escalante.Scala
import io.escalante.lift.Lift
import io.escalante.xml.ScalaXmlParser._

/**
 * Builds a Lift web app for testing.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftWebApp extends Log {

  val SCALA_VERSION: Scala = Scala()

  val LIFT_VERSION: String = Lift("2.5-RC4").version

  def apply(
      appName: String,
      deployName: String,
      descriptor: String,
      bootClass: Class[_ <: AnyRef],
      classes: Seq[Class[_]],
      webResources: Map[String, String],
      indexHtml: Elem,
      replication: Boolean = false): WebArchive = {
    // Create deployment name
    val war = ShrinkWrap.create(classOf[WebArchive], deployName)
    info("Create war deployment: %s", deployName)

    val indexHtmlContent = xml(indexHtml)
    val webXmlContent = xml(webXml(bootClass.getName, replication))

    val ideFriendlyPath = "modules/lift/src/it/testsuite/pom.xml"
    // Figure out an IDE and Maven friendly path:
    val path =
      if (new File(ideFriendlyPath).exists()) ideFriendlyPath else "pom.xml"

    info("Loading pom from: " + new File(path).getCanonicalPath)

    val resolver = DependencyResolvers.use(classOf[MavenDependencyResolver])
        .loadMetadataFromPom(path)

    // Add web resources
    for ((location, target) <- webResources)
    yield
      war.addAsWebResource(
        resource(s"$appName/$location"), s"$target/$location")

    war.addAsWebResource(indexHtmlContent, "index.html")
        .addAsWebResource(new StringAsset(descriptor),
            "META-INF/escalante.yml")
        .addAsWebResource(webXmlContent, "WEB-INF/web.xml")
        .addClasses(bootClass)
        .addClasses(classes: _ *)
        .addClasses(classOf[Log])
        .addAsLibraries(resolver
        .artifacts("org.seleniumhq.selenium:selenium-htmlunit-driver")
        .resolveAs(classOf[GenericArchive]))

    val separator = System.getProperty("line.separator")
    val sortedContents = asScalaIterator(war.getContent.values().iterator())
        .map(_.toString).toSeq.sorted
    val files = separator + sortedContents.mkString(separator)
    info("War deployment created, content:" + files)
    war
  }

  private def xml(n: Node): Asset = new StringAsset(n.toString())

  private def resource(resource: String): Asset = new ClassLoaderAsset(resource)

  private def webXml(bootLoader: String, replication: Boolean): Node = {
    val baseWebXml = <web-app version="2.5"
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

    if (replication)
      addXmlElements("web-app", List(<distributable/>), baseWebXml)
    else
      baseWebXml
  }

}
