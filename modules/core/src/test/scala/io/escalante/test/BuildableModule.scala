/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test

import java.io.File
import scala.xml.Node
import io.escalante.xml.ScalaXmlParser._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
trait BuildableModule {

  /**
   * Builds a module in the destination directory applying necessary
   * XML modifications to the given configuration.
   *
   * @param destDir File representing directory where to install module
   * @param config XML where to apply configuration changes
   * @return XML configuration with changes applied
   */
  def build(destDir: File, config: Node): Node

  /**
   * Adds extension and subsystem XML fragments to given XML configuration.
   *
   * @param extension XML fragment for extension to add
   * @param subsystem XML fragment for subsystem configuration
   * @param config XML to which add configuration
   * @return XML node containing changes
   */
  def addExtensionSubsystem(
      extension: Node,
      subsystem: Node,
      config: Node): Node = {
    addXmlElement("profile", subsystem,
      addXmlElement("extensions", extension, config))
  }

}
