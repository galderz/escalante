/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.artifact.subsystem

import io.escalante.test.subsystem.AbstractScalaSubsystemTest
import io.escalante.artifact.subsystem.{ArtifactRepositoryService, ThirdPartyModulesRepo, ArtifactExtension}
import org.junit.Test
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.controller.PathAddress
import org.jboss.as.subsystem.test.{ControllerInitializer, AdditionalInitialization}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class ArtifactSubsystemUnitTest
    extends AbstractScalaSubsystemTest(
      ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME, new ArtifactExtension()) {

  /**
   * Tests that the xml is parsed into the correct operations.
   */
  @Test def testParseSubsystem() {
    // Parse the subsystem xml into operations
    val subsystemXml =
        <subsystem xmlns={ArtifactExtension.ARTIFACT_SUBSYSTEM_NAMESPACE}/>

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
    assert(ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME === element.getValue)
    assert(!op.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO))
    assert(!op.hasDefined(ThirdPartyModulesRepo.PATH))
  }

  /**
   * Tests xml parsing of the thirdparty modules repository location.
   */
  @Test def testParseThirdpartyModulesRepo() {
    val subsystemXml =
      <subsystem xmlns={ArtifactExtension.ARTIFACT_SUBSYSTEM_NAMESPACE}>
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
    assert(ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME === element.getValue)
    assert("x" === op.get(ThirdPartyModulesRepo.RELATIVE_TO).asString())
    assert("y" === op.get(ThirdPartyModulesRepo.PATH).asString())
  }

  /**
   * Tests installation of the lift subsystem with default configuration.
   */
  @Test def testInstallLiftSubsystem() {
    // Parse the subsystem xml and install into the controller
    val subsystemXml =
        <subsystem xmlns={ArtifactExtension.ARTIFACT_SUBSYSTEM_NAMESPACE} />

    val services = super.installInController(
        new PathInitialization(), subsystemXml)

    val model = services.readWholeModel
    assert(model.get(SUBSYSTEM)
        .hasDefined(ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME))

    val liftNode = model.get(SUBSYSTEM)
        .get(ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME)
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
        <subsystem xmlns={ArtifactExtension.ARTIFACT_SUBSYSTEM_NAMESPACE}>
          <thirdparty-modules-repo path="/path/${my.path.expression}"/>
        </subsystem>

      val services = super.installInController(new PathInitialization(), subsystemXml)
      val model = services.readWholeModel
      assert(model.get(SUBSYSTEM)
          .hasDefined(ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME))

      val node = model.get(SUBSYSTEM)
          .get(ArtifactExtension.ARTIFACT_SUBSYSTEM_NAME)
      assert(!node.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO))

      val service = services.getContainer
          .getService(ArtifactRepositoryService.SERVICE_NAME)
          .getValue.asInstanceOf[ArtifactRepositoryService]
      assert("/path/" + value === service.thirdPartyModulesPath)
      assert("/path/${" + key + "}" ===
          node.get(ThirdPartyModulesRepo.PATH).asString())
    } finally {
      System.clearProperty(key)
    }
  }

  class PathInitialization extends AdditionalInitialization {

    override def setupController(init: ControllerInitializer) {
      init.addPath("jboss.home.dir", "thirdparty-modules", null)
    }

  }

}
