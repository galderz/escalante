package org.scalabox.lift

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.scalabox.logging.Log
import org.jboss.shrinkwrap.api.asset.{StringAsset, Asset}
import xml.Elem
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import org.jboss.shrinkwrap.api.{GenericArchive, ShrinkWrap}
import org.scalabox.util.ScalaXmlParser._
import org.scalabox.util.{FileSystem, Closeable}
import java.io.File

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractLiftWebAppTest extends Log {

   val deploymentArchiveName: String

   val indexHtml: Elem

   val defaultHtml: Elem

   def deployment(lift: Option[String], scala: Option[String],
           bootClass: Class[_ <: AnyRef], bootLoader: String,
           classes: Class[_]*): WebArchive = {
      val war = ShrinkWrap.create(classOf[WebArchive], deploymentArchiveName)
      info("Create war deployment: %s", deploymentArchiveName)

      val indexHtmlContent = xml(indexHtml)
      val defaultHtmlContent = xml(defaultHtml)
      val liftXmlContent = xml(liftXml(lift, scala))
      val webXmlContent = xml(webXml(bootLoader))

      val resolver = DependencyResolvers.use(classOf[MavenDependencyResolver])
         .loadMetadataFromPom(new File(FileSystem.getTarget(
               classOf[AbstractLiftWebAppTest]), "../pom.xml").getCanonicalPath)

      war.addAsWebResource(indexHtmlContent, "index.html")
           .addAsWebResource(defaultHtmlContent, "templates-hidden/default.html")
           .addAsWebResource(liftXmlContent, "WEB-INF/lift.xml")
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

   private def liftXml(lift: Option[String], scala: Option[String]): Elem = {
      (lift, scala) match {
         case (Some(liftVersion), Some(scalaVersion)) =>
            addXmlAttributes(<lift-app/>,
               ("version", liftVersion), ("scala-version", scalaVersion))
         case (Some(liftVersion), None) =>
            addXmlAttribute(<lift-app/>, "version", liftVersion)
         case (None, Some(scalaVersion)) =>
            addXmlAttribute(<lift-app/>, "scala-version", scalaVersion)
         case (None, None) => <lift-app/>
      }
   }

   private def webXml(bootLoader: String): Elem = {
      <web-app>
         <filter>
            <filter-name>LiftFilter</filter-name>
            <display-name>Lift Filter</display-name>
            <description>The Filter that intercepts lift calls</description>
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
