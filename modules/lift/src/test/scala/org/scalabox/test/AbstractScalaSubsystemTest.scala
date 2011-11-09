package org.scalabox.test

import org.jboss.dmr.ModelNode
import org.scalabox.lift.extension.SubsystemExtension
import org.scalatest.junit.AssertionsForJUnit
import xml.Elem
import collection.JavaConversions._
import org.jboss.as.subsystem.test.{KernelServices, AbstractSubsystemTest}
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class AbstractScalaSubsystemTest extends AbstractSubsystemTest(
      SubsystemExtension.SUBSYSTEM_NAME, new SubsystemExtension()) with AssertionsForJUnit {

   def parse(e: Elem): Iterable[ModelNode] =
      collectionAsScalaIterable(super.parse(e.toString()))

   def installInController(e: Elem) = super.installInController(e.toString())

   def checkResultAndGetContents(result: ModelNode): ModelNode =  {
      assert(SUCCESS === result.get(OUTCOME).asString())
      assert(result.hasDefined(RESULT))
      result.get(RESULT);
   }

}