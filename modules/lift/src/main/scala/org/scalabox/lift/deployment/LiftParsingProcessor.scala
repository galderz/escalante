package org.scalabox.lift.deployment

import org.jboss.as.ee.structure.{DeploymentType, DeploymentTypeMarker}
import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.scalabox.util.Closeable._
import javax.xml.stream.XMLInputFactory
import org.jboss.metadata.parser.util.NoopXMLResolver
import org.scalabox.logging.Log
import org.scalabox.lift.parsing.LiftMetaDataParser
import org.scalabox.lift.extension.LiftMetaData

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftParsingProcessor extends DeploymentUnitProcessor {

   import LiftParsingProcessor._

   def deploy(ctx: DeploymentPhaseContext) {
      info("Try to parse lift descriptor")
      val deployment = ctx.getDeploymentUnit
      if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deployment))
         return;

      val root = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
      val liftXml = root.getRoot.getChild(LIFT_XML)
      if (liftXml.exists()) {
         info("Lift application detected in %s", deployment)
         use(liftXml.openStream()) { input =>
            val inputFactory = XMLInputFactory.newInstance
            inputFactory.setXMLResolver(NoopXMLResolver.create())
            val xmlReader = inputFactory.createXMLStreamReader(input)
            val liftMetaData = LiftMetaDataParser.parse(xmlReader)
            deployment.putAttachment(LiftMetaData.ATTACHMENT_KEY, liftMetaData)
         }
      }
   }

   def undeploy(deployment: DeploymentUnit) {}

}

object LiftParsingProcessor extends Log {

   val LIFT_XML = "WEB-INF/lift.xml"

}