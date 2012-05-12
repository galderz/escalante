package org.scalabox.lift

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.scalabox.util.Closeable._
import javax.xml.stream.XMLInputFactory
import org.scalabox.logging.Log
import org.jboss.metadata.parser.util.{MetaDataElementParser, NoopXMLResolver}
import org.jboss.metadata.parser.servlet.WebMetaDataParser
import org.jboss.as.web.deployment.WarMetaData
import java.io.StringReader
import org.jboss.as.ee.structure.{SpecDescriptorPropertyReplacement, DeploymentType, DeploymentTypeMarker}
import org.jboss.as.server.deployment.module.ResourceRoot

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftParsingProcessor extends DeploymentUnitProcessor {

   import LiftParsingProcessor._

   def deploy(ctx: DeploymentPhaseContext) {
      trace("Try to parse lift descriptor")
      val deployment = ctx.getDeploymentUnit
      if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deployment))
         return

      val root = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
      val liftXml = root.getRoot.getChild(LIFT_XML)
      if (liftXml.exists()) {
         trace("Lift application detected in %s", deployment)

         // Custom web xml for lift apps
         addLiftMetadata(deployment, root)

         use(liftXml.openStream()) {
            input =>
               val inputFactory = XMLInputFactory.newInstance
               inputFactory.setXMLResolver(NoopXMLResolver.create())
               val xmlReader = inputFactory.createXMLStreamReader(input)
               val liftMetaData = LiftMetaDataParser.parse(xmlReader)
               deployment.putAttachment(LiftMetaData.ATTACHMENT_KEY, liftMetaData)
         }
      }
   }

   def undeploy(deployment: DeploymentUnit) {}

   private def addLiftMetadata(deployment: DeploymentUnit, root: ResourceRoot) {
      val webXmlVirtualFile = root.getRoot.getChild(WEB_XML)
      val dtdInfo = new MetaDataElementParser.DTDInfo()
      val inputFactory = XMLInputFactory.newInstance()
      inputFactory.setXMLResolver(dtdInfo)
      val xmlReader =
         if (webXmlVirtualFile.exists()) {
            // TODO: If web.xml present, rather than ignoring it, merge it adding filter
            trace("Web app descriptor 'web.xml' present in deployment, read it")
            inputFactory.createXMLStreamReader(webXmlVirtualFile.openStream())
         } else {
            trace("No web.xml present in deployment, generate a Lift-friendly web.xml")
            val webXml =
               <web-app>
                  <filter>
                     <filter-name>LiftFilter</filter-name>
                     <display-name>Lift Filter</display-name>
                     <description>The Filter that intercepts lift calls</description>
                     <filter-class>net.liftweb.http.LiftFilter</filter-class>
                  </filter>
                  <filter-mapping>
                     <filter-name>LiftFilter</filter-name>
                     <url-pattern>/*</url-pattern>
                  </filter-mapping>
               </web-app>
            val reader = new StringReader(webXml.toString())
            val inputFactory = XMLInputFactory.newInstance()
            val dtdInfo = new MetaDataElementParser.DTDInfo()
            inputFactory.setXMLResolver(dtdInfo)
            inputFactory.createXMLStreamReader(reader)
         }

      val webMetaData = WebMetaDataParser.parse(xmlReader, dtdInfo,
         SpecDescriptorPropertyReplacement.propertyReplacer(deployment))
      val warMetaData = deployment.getAttachment(WarMetaData.ATTACHMENT_KEY)
      warMetaData.setWebMetaData(webMetaData)
   }

}

object LiftParsingProcessor extends Log {

   val LIFT_XML = "WEB-INF/lift.xml"

   val WEB_XML = "WEB-INF/web.xml"

}