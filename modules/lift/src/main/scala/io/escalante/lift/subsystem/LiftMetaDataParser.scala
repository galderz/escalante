/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import io.escalante.Scala
import io.escalante.lift.Lift
import org.jboss.vfs.VirtualFile
import java.util
import collection.JavaConversions.asScalaBuffer
import scala.Some
import io.escalante.yaml.YamlParser

/**
 * Lift application metadata parser.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftMetaDataParser {

  def parse(descriptor: VirtualFile, isImplicitJpa: Boolean): Option[LiftMetaData] =
    parse(YamlParser.parse(descriptor), isImplicitJpa)

  def parse(contents: String, isImplicitJpa: Boolean): Option[LiftMetaData] =
    parse(YamlParser.parse(contents), isImplicitJpa)

  def parse(parsed: util.Map[String, Object], isImplicitJpa: Boolean): Option[LiftMetaData] = {
    val scala = Scala(parsed)
    val lift = Lift(parsed)

    // TODO: Merge with Lift?
    lift match {
      case Some(l) =>
        // If lift key found, check if modules present
        val liftMeta = parsed.get("lift").asInstanceOf[util.Map[String, Object]]
        val modules = extractModules(liftMeta, isImplicitJpa)
        Some(LiftMetaData(l, scala, modules))
      case None =>
        None
    }
  }

  private def extractModules(
        liftMeta: util.Map[String, Object],
        isImplicitJpa: Boolean): Seq[String] = {
    val modules = new util.ArrayList[String]()
    // Add jpa module if implicitly enabled
    if (isImplicitJpa)
      modules.add("jpa")

    if (liftMeta != null) {
      val modulesMeta = liftMeta.get("modules")
      if (modulesMeta != null) {
        modules.addAll(modulesMeta.asInstanceOf[util.List[String]])
      }
    }

    asScalaBuffer(modules).toSeq
  }

}