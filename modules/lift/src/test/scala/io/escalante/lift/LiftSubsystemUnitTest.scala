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
import subsystem.LiftExtension
import io.escalante.artifact.subsystem.ThirdPartyModulesRepo
import io.escalante.test.subsystem.AbstractScalaSubsystemTest

/**
 * Unit test for the Lift subsystem.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftSubsystemUnitTest
    extends AbstractScalaSubsystemTest(
      LiftExtension.LIFT_SUBSYSTEM_NAME, new LiftExtension) {

  /**
   * Tests that the xml is parsed into the correct operations.
   */
  @Test def testParseSubsystem() {
    // Parse the subsystem xml into operations
    val subsystemXml =
        <subsystem xmlns={LiftExtension.LIFT_SUBSYSTEM_NAMESPACE}/>

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
    assert(LiftExtension.LIFT_SUBSYSTEM_NAME === element.getValue)
    assert(!op.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO))
    assert(!op.hasDefined(ThirdPartyModulesRepo.PATH))
  }


}