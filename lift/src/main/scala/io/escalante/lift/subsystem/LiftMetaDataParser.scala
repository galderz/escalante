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
import io.escalante.util.YamlParser
import java.util
import collection.JavaConversions.asScalaBuffer
import scala.Some

/**
 * Lift application metadata parser.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftMetaDataParser {

  def parse(descriptor: VirtualFile): Option[LiftMetaData] =
    parse(YamlParser.parse(descriptor))

  def parse(contents: String): Option[LiftMetaData] =
    parse(YamlParser.parse(contents))

  def parse(parsed: util.Map[String, Object]): Option[LiftMetaData] = {
    val scala = Scala.parse(parsed)
    val lift = Lift.parse(parsed)

    // TODO: Merge with Lift?
    lift match {
      case Some(l) =>
        // If lift key found, check if modules present
        val liftMeta = parsed.get("lift").asInstanceOf[util.Map[String, Object]]
        val modules =
          if (liftMeta != null) {
            val modulesMeta = liftMeta.get("modules")
            if (modulesMeta != null)
              asScalaBuffer(modulesMeta.asInstanceOf[util.List[String]]).toSeq
            else
              List()
          } else {
            List()
          }

        Some(LiftMetaData(l, scala, modules))
      case None =>
        None
    }
  }

//  def parse(meta: EscalanteDescriptor): LiftMetaData = {
//    // If reached this far, it's a Lift app
//    val version = LiftVersion.forName(meta.lift.get.version)
//
//    // Default Scala version based on last Lift release
//    val scalaVersion = Scala.forName(
//      meta.scala.getOrElse(ScalaDescriptor("2.9.2")).version)
//
//    new LiftMetaData(version, scalaVersion)
//  }

}