package org.scalabox.lift.extension

import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.junit.Test
import org.scalabox.test.AbstractScalaSubsystemTest
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.{PathElement, PathAddress}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftSubsystemUnitTest extends AbstractScalaSubsystemTest {

   /**
    * Tests that the xml is parsed into the correct operations
    */
   @Test def testParseSubsystem {
      // Parse the subsystem xml into operations
      val subsystemXml =
         <subsystem xmlns={LiftExtension.NAMESPACE}>
            <!--
            <deployment-types>
               <deployment-type suffix="tst" tick="12345"/>
            </deployment-types>
            -->
         </subsystem>

      val operations = super.parse(subsystemXml)

      // Check that we have the expected number of operations
      assert(1 === operations.size)

      // Check that each operation has the correct content
      val addSubsystem = operations.head
      assert(ADD === addSubsystem.get(OP).asString)
      val addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR))
      assert(1 === addr.size)
      val element = addr.getElement(0)
      assert(SUBSYSTEM === element.getKey)
      assert(LiftExtension.SUBSYSTEM_NAME === element.getValue)

//      // Then we will get the add type operation
//      val addType = operations.tail.head
//      assert(ADD === addType.get(OP).asString)
//      assert(12345 == addType.get("tick").asLong)
//      addr = PathAddress.pathAddress(addType.get(OP_ADDR))
//      assert(2 === addr.size)
//      element = addr.getElement(0)
//      assert(SUBSYSTEM === element.getKey)
//      assert(LiftExtension.SUBSYSTEM_NAME === element.getValue)
//      element = addr.getElement(1)
//      assert("type" === element.getKey)
//      assert("tst"=== element.getValue)
   }

   /**
    * Test that the model created from the xml looks as expected
    */
   @Test def testInstallIntoController {
      // Parse the subsystem xml and install into the controller
      val subsystemXml =
         <subsystem xmlns={LiftExtension.NAMESPACE}>
            <!--
            <deployment-types>
               <deployment-type suffix="tst" tick="12345"/>
            </deployment-types>
            -->
         </subsystem>

      val services = super.installInController(subsystemXml)
      val model = services.readWholeModel
      assert(model.get(SUBSYSTEM).hasDefined(LiftExtension.SUBSYSTEM_NAME))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME).hasDefined("type"))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type").hasDefined("tst"))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type", "tst").hasDefined("tick"))
//      assert(12345 === model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type", "tst", "tick").asLong)
   }

   /**
    * Starts a controller with a given subsystem xml and then checks that a
    * second controller started with the xml marshalled from the first one
    * results in the same model
    */
   @Test def testParseAndMarshalModel {
      // Parse the subsystem xml and install into the first controller
      val subsystemXml =
         <subsystem xmlns={LiftExtension.NAMESPACE}>
            <!--
            <deployment-types>
               <deployment-type suffix="tst" tick="12345"/>
            </deployment-types>
            -->
         </subsystem>

      val servicesA = super.installInController(subsystemXml)
      val modelA = servicesA.readWholeModel
      val marshalled = servicesA.getPersistedSubsystemXml
      val servicesB = super.installInController(marshalled)
      val modelB = servicesB.readWholeModel
      super.compare(modelA, modelB)
   }

   /**
    * Starts a controller with the given subsystem xml and then checks that a
    * second controller started with the operations from its describe action
    * results in the same model
    */
   @Test def testDescribeHandler {
      val subsystemXml =
         <subsystem xmlns={LiftExtension.NAMESPACE}></subsystem>

      val servicesA = super.installInController(subsystemXml)
      val modelA = servicesA.readWholeModel
      val describeOp = new ModelNode
      describeOp.get(OP).set(DESCRIBE)
      describeOp.get(OP_ADDR).set(
         PathAddress.pathAddress(
            PathElement.pathElement(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME))
                 .toModelNode)
      val operations: java.util.List[ModelNode] =
         super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList()
      val servicesB = super.installInController(operations)
      val modelB = servicesB.readWholeModel
      super.compare(modelA, modelB)
   }

//   @Test def testExecuteOperations {
//      val subsystemXml =
//         <subsystem xmlns={LiftExtension.NAMESPACE}>
//            <!-- <deployment-types>
//               <deployment-type suffix="tst" tick="12345"/>
//            </deployment-types> -->
//         </subsystem>
//
//      val services = super.installInController(subsystemXml)
//      val fooTypeAddr = PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME), PathElement.pathElement("type", "foo"))
//      val addOp = new ModelNode
//      addOp.get(OP).set(ADD)
//      addOp.get(OP_ADDR).set(fooTypeAddr.toModelNode)
//      addOp.get("tick").set(1000)
//      var result = services.executeOperation(addOp)
//      assert(SUCCESS === result.get(OUTCOME).asString)
//      val model = services.readWholeModel
//      assert(model.get(SUBSYSTEM).hasDefined(LiftExtension.SUBSYSTEM_NAME))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME).hasDefined("type"))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type").hasDefined("tst"))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type", "tst").hasDefined("tick"))
//      assert(12345 === model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type", "tst", "tick").asLong)
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type").hasDefined("foo"))
//      assert(model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type", "foo").hasDefined("tick"))
//      assert(1000 === model.get(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME, "type", "foo", "tick").asLong)
//      val writeOp = new ModelNode
//      writeOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION)
//      writeOp.get(OP_ADDR).set(fooTypeAddr.toModelNode)
//      writeOp.get(NAME).set("tick")
//      writeOp.get(VALUE).set(3456)
//      result = services.executeOperation(writeOp)
//      assert(SUCCESS === result.get(OUTCOME).asString)
//      val readOp = new ModelNode
//      readOp.get(OP).set(READ_ATTRIBUTE_OPERATION)
//      readOp.get(OP_ADDR).set(fooTypeAddr.toModelNode)
//      readOp.get(NAME).set("tick")
//      result = services.executeOperation(readOp)
//      assert(3456 === checkResultAndGetContents(result).asLong)
//      val service = services.getContainer.getService(LiftService.createServiceName("foo")).getValue.asInstanceOf[LiftService]
//      assert(3456 === service.getTick)
//   }

}