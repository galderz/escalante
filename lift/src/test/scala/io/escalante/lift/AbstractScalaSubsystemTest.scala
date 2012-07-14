/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import org.jboss.dmr.ModelNode
import scala.xml.Elem
import collection.JavaConversions._
import org.jboss.as.subsystem.test.{AdditionalInitialization, AbstractSubsystemTest}
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.scalatest.junit.AssertionsForJUnit
import subsystem.LiftExtension

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractScalaSubsystemTest extends AbstractSubsystemTest(
   LiftExtension.SUBSYSTEM_NAME, new LiftExtension()) with AssertionsForJUnit {

   def parse(e: Elem): Iterable[ModelNode] =
      asScalaIterable(super.parse(e.toString()))

   def installInController(e: Elem) = super.installInController(e.toString())

   def installInController(additionalInit: AdditionalInitialization, e: Elem) =
      super.installInController(additionalInit, e.toString())

   def checkResultAndGetContents(result: ModelNode): ModelNode = {
      assert(SUCCESS === result.get(OUTCOME).asString())
      assert(result.hasDefined(RESULT))
      result.get(RESULT)
   }

}