package io.escalante.lift

import org.jboss.dmr.ModelNode
import scala.xml.Elem
import collection.JavaConversions._
import org.jboss.as.subsystem.test.{AbstractSubsystemTest}
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.scalatest.junit.AssertionsForJUnit

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

   def checkResultAndGetContents(result: ModelNode): ModelNode = {
      assert(SUCCESS === result.get(OUTCOME).asString())
      assert(result.hasDefined(RESULT))
      result.get(RESULT)
   }

}