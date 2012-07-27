/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.{PathElement, PathAddress}
import org.junit.{Ignore, Test}
import subsystem.{ThirdPartyModulesRepo, LiftExtension}
import xml.Elem
import org.jboss.as.subsystem.test.{ControllerInitializer, AdditionalInitialization}
import java.io.File

/**
 * Unit test for the Lift subsystem.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftSubsystemUnitTest extends AbstractScalaSubsystemTest {

  /**
   * Tests that the xml is parsed into the correct operations
   */
  @Test def testParseSubsystem {
    // Parse the subsystem xml into operations
    val subsystemXml = <subsystem xmlns={LiftExtension.NAMESPACE}/>

    val operations = super.parse(subsystemXml)

    // Check that we have the expected number of operations
    assert(1 === operations.size)

    // Check that each operation has the correct content
    val op = operations.head
    assert(ADD === op.get(OP).asString)
    val addr = PathAddress.pathAddress(op.get(OP_ADDR))
    assert(1 === addr.size)
    val element = addr.getElement(0)
    assert(SUBSYSTEM === element.getKey)
    assert(LiftExtension.SUBSYSTEM_NAME === element.getValue)
    assert(!op.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO))
    assert(!op.hasDefined(ThirdPartyModulesRepo.PATH))
  }

  @Test def testParseThirdpartyModulesRepo {
    val subsystemXml =
      <subsystem xmlns={LiftExtension.NAMESPACE}>
        <thirdparty-modules-repo relative-to="x" path="y"/>
      </subsystem>

    val operations = super.parse(subsystemXml)

    // Check that we have the expected number of operations
    assert(1 === operations.size)

    // Check that each operation has the correct content
    val op = operations.head
    assert(ADD === op.get(OP).asString)
    val addr = PathAddress.pathAddress(op.get(OP_ADDR))
    assert(1 === addr.size)
    val element = addr.getElement(0)
    assert(SUBSYSTEM === element.getKey)
    assert(LiftExtension.SUBSYSTEM_NAME === element.getValue)
    assert("x" === op.get(ThirdPartyModulesRepo.RELATIVE_TO).asString())
    assert("y" === op.get(ThirdPartyModulesRepo.PATH).asString())
  }

  // TODO: Ask AS guys on how to best unit test path related tests

  //   /**
  //    * Test that the model created from the xml looks as expected
  //    */
  //   @Test def testInstallIntoController {
  //      // Parse the subsystem xml and install into the controller
  //      val subsystemXml = <subsystem xmlns={LiftExtension.NAMESPACE} />
  //
  //      val services = super.installInController(subsystemXml)
  //      val model = services.readWholeModel
  //      assert(model.get(SUBSYSTEM).hasDefined(LiftExtension.SUBSYSTEM_NAME))
  //   }
  //
  //   /**
  //    * Starts a controller with a given subsystem xml and then checks that a
  //    * second controller started with the xml marshalled from the first one
  //    * results in the same model
  //    */
  //   @Test def testParseAndMarshalModel {
  //      // Parse the subsystem xml and install into the first controller
  //      val subsystemXml =
  //         <subsystem xmlns={LiftExtension.NAMESPACE}>
  //            <!--
  //            <deployment-types>
  //               <deployment-type suffix="tst" tick="12345"/>
  //            </deployment-types>
  //            -->
  //         </subsystem>
  //
  //      val servicesA = super.installInController(subsystemXml)
  //      val modelA = servicesA.readWholeModel
  //      val marshalled = servicesA.getPersistedSubsystemXml
  //      val servicesB = super.installInController(marshalled)
  //      val modelB = servicesB.readWholeModel
  //      super.compare(modelA, modelB)
  //   }
  //
  //   /**
  //    * Starts a controller with the given subsystem xml and then checks that a
  //    * second controller started with the operations from its describe action
  //    * results in the same model
  //    */
  //   @Test def testDescribeHandler {
  //      val subsystemXml =
  //         <subsystem xmlns={LiftExtension.NAMESPACE}></subsystem>
  //
  //      val servicesA = super.installInController(subsystemXml)
  //      val modelA = servicesA.readWholeModel
  //      val describeOp = new ModelNode
  //      describeOp.get(OP).set(DESCRIBE)
  //      describeOp.get(OP_ADDR).set(
  //         PathAddress.pathAddress(
  //            PathElement.pathElement(SUBSYSTEM, LiftExtension.SUBSYSTEM_NAME))
  //               .toModelNode)
  //      val operations: java.util.List[ModelNode] =
  //         super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList()
  //      val servicesB = super.installInController(operations)
  //      val modelB = servicesB.readWholeModel
  //      super.compare(modelA, modelB)
  //   }


}