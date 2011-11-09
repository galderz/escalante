package org.scalabox.lift.extension

import org.jboss.as.controller.descriptions.DescriptionProvider
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import java.util.Locale
import org.jboss.dmr.{ModelType, ModelNode}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object SubsystemProviders {

   /**
    * Used to create the description of the subsystem
    */
   val SUBSYSTEM = new DescriptionProvider() {
      def getModelDescription(locale: Locale): ModelNode = {
         // The locale is passed in so you can internationalize the strings
         // used in the descriptions
         val subsystem = new ModelNode()
         subsystem.get(DESCRIPTION).set("This subsystem deploys Lift applications")
         subsystem.get(HEAD_COMMENT_ALLOWED).set(true)
         subsystem.get(TAIL_COMMENT_ALLOWED).set(true)
         subsystem.get(NAMESPACE).set(SubsystemExtension.NAMESPACE)

         // Add information about the 'type' child
         subsystem.get(CHILDREN, "type", DESCRIPTION)
               .set("Deployment types that Lift can deploy")
         subsystem.get(CHILDREN, "type", MIN_OCCURS).set(0)
         subsystem.get(CHILDREN, "type", MAX_OCCURS).set(Integer.MAX_VALUE)
         subsystem.get(CHILDREN, "type", MODEL_DESCRIPTION)
         subsystem;
      }
   }

   /**
    * Used to create the description of the subsystem add method
    */
   val SUBSYSTEM_ADD = new DescriptionProvider() {
      def getModelDescription(locale: Locale): ModelNode = {
         // The locale is passed in so you can internationalize the strings
         // used in the descriptions
         val subsystem = new ModelNode()
         subsystem.get(DESCRIPTION).set("Adds the Lift subsystem")
         subsystem;
      }
   }
   /**
    * Used to create the description of the {@code type}  child
    */
   var TYPE_CHILD = new DescriptionProvider {
      def getModelDescription(locale: Locale): ModelNode = {
         val node = new ModelNode
         node.get(DESCRIPTION).set("Contains information about a tracked deployment type")
         node.get(ATTRIBUTES, "tick", DESCRIPTION).set("How often in milliseconds to output the information about the tracked deployments")
         node.get(ATTRIBUTES, "tick", TYPE).set(ModelType.LONG)
         node.get(ATTRIBUTES, "tick", REQUIRED).set(true)
         node.get(ATTRIBUTES, "tick", DEFAULT).set(10000)
         node
      }
   }

}