/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.yaml

import org.yaml.snakeyaml.Yaml
import java.util
import org.jboss.vfs.VirtualFile

/**
 * Yaml descriptor parser.
 *
 * In order to minimise performance degradation of parsing, unless it's
 * totally necessary, this class uses Java collections directly avoiding
 * unnecessary conversion to Scala equivalents, hence the code might not look
 * very Scala-ish.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object YamlParser {

  def parse(file: VirtualFile): util.Map[String, Object] = {
    new Yaml().load(file.openStream()).asInstanceOf[util.Map[String, Object]]
  }

  def parse(contents: String): util.Map[String, Object] = {
    new Yaml().load(contents).asInstanceOf[util.Map[String, Object]]
  }

}
