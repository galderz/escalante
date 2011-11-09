package org.scalabox.lift.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Contains the description providers. The description providers are what print
 * out the information when you execute the {@code read-resource-description}
 * operation.
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class SubsystemProviders {

   /**
    * Used to create the description of the subsystem
    */
   public static DescriptionProvider SUBSYSTEM = new DescriptionProvider() {
      public ModelNode getModelDescription(Locale locale) {
         // The locale is passed in so you can internationalize the strings
         // used in the descriptions

         final ModelNode subsystem = new ModelNode();
         subsystem.get(DESCRIPTION).set("This subsystem deploys Lift applications");
         subsystem.get(HEAD_COMMENT_ALLOWED).set(true);
         subsystem.get(TAIL_COMMENT_ALLOWED).set(true);
         subsystem.get(NAMESPACE).set(SubsystemExtension.NAMESPACE);

         // Add information about the 'type' child
         subsystem.get(CHILDREN, "type", DESCRIPTION)
               .set("Deployment types that Lift can deploy");
         subsystem.get(CHILDREN, "type", MIN_OCCURS).set(0);
         subsystem.get(CHILDREN, "type", MAX_OCCURS).set(Integer.MAX_VALUE);
         subsystem.get(CHILDREN, "type", MODEL_DESCRIPTION);

         return subsystem;
      }
   };

   /**
    * Used to create the description of the subsystem add method
    */
   public static DescriptionProvider SUBSYSTEM_ADD = new DescriptionProvider() {
      public ModelNode getModelDescription(Locale locale) {
         // The locale is passed in so you can internationalize the strings
         // used in the descriptions

         final ModelNode subsystem = new ModelNode();
         subsystem.get(DESCRIPTION).set("Adds the Lift subsystem");

         return subsystem;
      }
   };

   /**
    * Used to create the description of the {@code type}  child
    */
   public static DescriptionProvider TYPE_CHILD = new DescriptionProvider() {
      @Override
      public ModelNode getModelDescription(Locale locale) {
         ModelNode node = new ModelNode();
         node.get(DESCRIPTION).set("Contains information about a tracked deployment type");
         node.get(ATTRIBUTES, "tick", DESCRIPTION).set("How often in milliseconds to output the information about the tracked deployments");
         node.get(ATTRIBUTES, "tick", TYPE).set(ModelType.LONG);
         node.get(ATTRIBUTES, "tick", REQUIRED).set(true);
         node.get(ATTRIBUTES, "tick", DEFAULT).set(10000);
         return node;
      }
   };

}
