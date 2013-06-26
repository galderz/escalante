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
import collection.JavaConversions._
import scala.Some

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

  /**
   * Detects a framework in the descriptor data
   *
   * @param framework name of framework to detect
   * @param defaultFrameworkVersion default framework version
   * @param parsed descriptor data
   * @return [[scala.None]] if no framework was detected, otherwise
   *         [[scala.Some]] containing the framework version detected
   */
  def detectFramework(
      framework: String,
      defaultFrameworkVersion: String,
      parsed: util.Map[String, Object]): Option[String] = {
    if (parsed != null) {
      val hasFramework = parsed.containsKey(framework)
      val tmp = parsed.get(framework)
      if (!hasFramework)
        None
      else if (hasFramework && tmp == null)
        Some(defaultFrameworkVersion)
      else {
        val playMeta = tmp.asInstanceOf[util.Map[String, Object]]
        val version = playMeta.get("version")
        if (version != null)
          Some(version.toString)
        else
          Some(defaultFrameworkVersion)
      }
    } else {
      None
    }
  }

  /**
   * Extract dependency modules defined in parsed data
   *
   * @param parsed optional set of modules
   * @return optional list of modules
   */
  def extractModules(parsed: Option[util.Map[String, Object]]): Option[Seq[String]] = {
    for {
      modulesMap <- parsed
      modules <- Option(modulesMap.get("modules"))
    } yield {
      modules.asInstanceOf[util.List[String]].toSeq
    }
  }

}
