/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.subsystem

import org.jboss.dmr.ModelNode
import scala.xml.Elem
import collection.JavaConversions._
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.subsystem.test.{AdditionalInitialization, AbstractSubsystemTest}
import org.jboss.as.controller.Extension
import org.scalatest.junit.AssertionsForJUnit

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractScalaSubsystemTest(subsystemName: String, ext: Extension)
    extends AbstractSubsystemTest(subsystemName, ext)
    with AssertionsForJUnit {

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