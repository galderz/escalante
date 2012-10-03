/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.controller.PathAddress
import org.junit.Test
import subsystem.{LiftService, ThirdPartyModulesRepo, LiftExtension}
import org.jboss.as.subsystem.test.{ControllerInitializer, AdditionalInitialization}

/**
 * Unit test for the Lift subsystem.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftSubsystemUnitTest extends AbstractScalaSubsystemTest {

  /**
   * Tests that the xml is parsed into the correct operations.
   */
  @Test def testParseSubsystem() {
    // Parse the subsystem xml into operations
    val subsystemXml = <subsystem xmlns={LiftExtension.SUBSYSTEM_NAMESPACE}/>

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

  /**
   * Tests xml parsing of the thirdparty modules repository location.
   */
  @Test def testParseThirdpartyModulesRepo() {
    val subsystemXml =
      <subsystem xmlns={LiftExtension.SUBSYSTEM_NAMESPACE}>
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

  /**
   * Tests installation of the lift subsystem with default configuration.
   */
  @Test def testInstallLiftSubsystem() {
    // Parse the subsystem xml and install into the controller
    val subsystemXml = <subsystem xmlns={LiftExtension.SUBSYSTEM_NAMESPACE} />
    val services = super.installInController(new PathInitialization(), subsystemXml)
    val model = services.readWholeModel
    assert(model.get(SUBSYSTEM).hasDefined(LiftExtension.SUBSYSTEM_NAME))
    val liftNode = model.get(SUBSYSTEM).get(LiftExtension.SUBSYSTEM_NAME)
    assert(!liftNode.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO))
    assert(!liftNode.hasDefined(ThirdPartyModulesRepo.PATH))
  }

  /**
   * Tests installation of the lift subsystem with a custom thirdparty modules
   * repository that requires system property based expression resolution.
   */
  @Test def testInstallWithSysPropertyThirdpartyModulesRepoPath() {
    val key = "my.path.expression"
    val value = "lift1234"
    System.setProperty(key, value)

    try {
      val subsystemXml =
        <subsystem xmlns={LiftExtension.SUBSYSTEM_NAMESPACE}>
          <thirdparty-modules-repo path="/path/${my.path.expression}"/>
        </subsystem>

      val services = super.installInController(new PathInitialization(), subsystemXml)
      val model = services.readWholeModel
      assert(model.get(SUBSYSTEM).hasDefined(LiftExtension.SUBSYSTEM_NAME))
      val liftNode = model.get(SUBSYSTEM).get(LiftExtension.SUBSYSTEM_NAME)
      assert(!liftNode.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO))
      val liftService = services.getContainer.getService(
        LiftService.createServiceName).getValue.asInstanceOf[LiftService]
      assert("/path/" + value === liftService.thirdPartyModulesPath)
      assert("/path/${" + key + "}" === liftNode.get(ThirdPartyModulesRepo.PATH).asString())
    } finally {
      System.clearProperty(key)
    }
  }

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

  class PathInitialization extends AdditionalInitialization {

    override def setupController(init: ControllerInitializer) {
      init.addPath("jboss.home.dir", "thirdparty-modules", null)
    }

  }

}