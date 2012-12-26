/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.util

import org.yaml.snakeyaml.Yaml
import java.util
import io.escalante.{LiftYaml, ScalaYaml, EscalanteYaml}
import org.jboss.vfs.VirtualFile

/**
 * Yaml descriptor parser.
 *
 * In order to minimise performance degradation of parsing, this class uses
 * Java collections directly avoiding unnecessary conversion to Scala
 * equivalents, hence the code might not look very Scala-ish.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object YamlParser {

  def parse(file: VirtualFile): EscalanteYaml = {
    decode(new Yaml().load(file.openStream())
      .asInstanceOf[util.Map[String, Object]])
  }

  def parse(contents: String): EscalanteYaml = {
    decode(new Yaml().load(contents).asInstanceOf[util.Map[String, Object]])
  }

  private def decode(parsed: util.Map[String, Object]): EscalanteYaml = {
    if (parsed == null) EscalanteYaml(None, None)
    else {
      var tmp = parsed.get("scala")
      val scala =
        if (tmp != null)
          Some(ScalaYaml(tmp
            .asInstanceOf[util.Map[String, Object]].get("version").toString))
        else
          None

      val liftKey = "lift"
      val hasLift = parsed.containsKey(liftKey)
      tmp = parsed.get(liftKey)
      val lift =
        if (!hasLift)
          None
        else if (hasLift && tmp == null)
          Some(LiftYaml(None))
        else
          Some(LiftYaml(Some(tmp.asInstanceOf[util.Map[String, Object]]
            .get("version").toString)))

      EscalanteYaml(scala, lift)
    }
  }

}
