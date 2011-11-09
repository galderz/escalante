package org.scalabox.lift.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * TODO - document
 *
 * @author Galder Zamarreño
 */
public class SubsystemExtension implements Extension {

   /**
    * The name space used for the {@code substystem} element
    */
   public static final String NAMESPACE = "urn:org.scalabox:lift:1.0";

   /**
    * The name of our subsystem within the model.
    */
   public static final String SUBSYSTEM_NAME = "lift";

   /**
    * The parser used for parsing our subsystem
    */
   private final SubsystemParser parser = new SubsystemParser();

   @Override
   public void initializeParsers(ExtensionParsingContext context) {
      context.setSubsystemXmlMapping(NAMESPACE, parser);
   }

   @Override
   public void initialize(ExtensionContext context) {
      SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
      ManagementResourceRegistration registration =
            subsystem.registerSubsystemModel(SubsystemProviders.SUBSYSTEM);

      //We always need to add an 'add' operation
      registration.registerOperationHandler(
            ADD, SubsystemAdd.INSTANCE, SubsystemProviders.SUBSYSTEM_ADD, false);

      //We always need to add a 'describe' operation
      registration.registerOperationHandler(
            DESCRIBE, SubsystemDescribeHandler.INSTANCE,
            SubsystemDescribeHandler.INSTANCE, false,
            OperationEntry.EntryType.PRIVATE);

      //Add the type child
      ManagementResourceRegistration typeChild = registration.registerSubModel(
            PathElement.pathElement("type"), SubsystemProviders.TYPE_CHILD);
      typeChild.registerOperationHandler(ADD,
                                         TypeAddHandler.INSTANCE, TypeAddHandler.INSTANCE);
      typeChild.registerOperationHandler(REMOVE,
                                         TypeRemoveHandler.INSTANCE, TypeRemoveHandler.INSTANCE);
      typeChild.registerReadWriteAttribute("tick", null,
                                           LiftTickHandler.INSTANCE, AttributeAccess.Storage.CONFIGURATION);

      subsystem.registerXMLElementWriter(parser);
   }

   private static ModelNode createAddSubsystemOperation() {
      final ModelNode subsystem = new ModelNode();
      subsystem.get(OP).set(ADD);
      subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME);
      return subsystem;
   }

   /**
    * The subsystem parser, which uses stax to read and write to and from xml
    */
   private static class SubsystemParser
         implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
                    XMLElementWriter<SubsystemMarshallingContext> {

      /**
       * {@inheritDoc}
       */
      @Override
      public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
         // Write out the main subsystem element
         context.startSubsystemElement(SubsystemExtension.NAMESPACE, false);

         writer.writeStartElement("deployment-types");

         ModelNode node = context.getModelNode();
         ModelNode type = node.get("type");
         for (Property property : type.asPropertyList()) {
            // Write each child element to xml
            writer.writeStartElement("deployment-type");
            writer.writeAttribute("suffix", property.getName());
            ModelNode entry = property.getValue();
            if (entry.hasDefined("tick")) {
               writer.writeAttribute("tick", entry.get("tick").asString());
            }
            writer.writeEndElement();
         }

         //End deployment-types
         writer.writeEndElement();
         //End subsystem
         writer.writeEndElement();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
         // Require no attributes
         ParseUtils.requireNoAttributes(reader);

         //Add the main subsystem 'add' operation
         list.add(createAddSubsystemOperation());

         //Read the children
         while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (!reader.getLocalName().equals("deployment-types")) {
               throw ParseUtils.unexpectedElement(reader);
            }
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
               if (reader.isStartElement()) {
                  readDeploymentType(reader, list);
               }
            }
         }
      }

      private void readDeploymentType(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
         if (!reader.getLocalName().equals("deployment-type")) {
            throw ParseUtils.unexpectedElement(reader);
         }
         String suffix = null;
         Long tick = null;
         for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attr = reader.getAttributeLocalName(i);
            if (attr.equals("tick")) {
               tick = Long.valueOf(reader.getAttributeValue(i));
            } else if (attr.equals("suffix")) {
               suffix = reader.getAttributeValue(i);
            } else {
               throw ParseUtils.unexpectedAttribute(reader, i);
            }
         }
         ParseUtils.requireNoContent(reader);
         if (suffix == null) {
            throw ParseUtils.missingRequiredElement(reader, Collections.singleton("suffix"));
         }

         //Add the 'add' operation for each 'type' child
         ModelNode addType = new ModelNode();
         addType.get(OP).set(ADD);
         PathAddress addr = PathAddress.pathAddress(
               PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME),
               PathElement.pathElement("type", suffix));
         addType.get(OP_ADDR).set(addr.toModelNode());
         if (tick != null)
            addType.get("tick").set(tick);

         list.add(addType);
      }
   }

   /**
    * Recreate the steps to put the subsystem in the same state it was in. This
    * is used in domain mode to query the profile being used, in order to get
    * the steps needed to create the servers
    */
   private static class SubsystemDescribeHandler implements OperationStepHandler, DescriptionProvider {

      static final SubsystemDescribeHandler INSTANCE = new SubsystemDescribeHandler();

      public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
         // Add the main operation
         context.getResult().add(createAddSubsystemOperation());

         //Add the operations to create each child
         ModelNode node = context.readModel(PathAddress.EMPTY_ADDRESS);
         for (Property property : node.get("type").asPropertyList()) {
            ModelNode addType = new ModelNode();
            addType.get(OP).set(ADD);
            PathAddress addr = PathAddress.pathAddress(
                  PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME),
                  PathElement.pathElement("type", property.getName()));
            addType.get(OP_ADDR).set(addr.toModelNode());
            if (property.getValue().hasDefined("tick")) {
               addType.get("tick").set(property.getValue().get("tick").asLong());
            }
            context.getResult().add(addType);
         }
         context.completeStep();
      }

      @Override
      public ModelNode getModelDescription(Locale locale) {
         return CommonDescriptions.getSubsystemDescribeOperation(locale);
      }

   }

}
