/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.AttachmentKey
import io.escalante._
import io.escalante.lift.{LIFT_24, Lift}
import maven.MavenArtifact
import org.sonatype.aether.graph.DependencyFilter
import lift.maven.{Lift24Scala28DependencyFilter, Lift24Scala29DependencyFilter}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
case class LiftMetaData(liftVersion: Lift, scalaVersion: Scala) {

   def liftDependencyFilter: DependencyFilter = {
      (liftVersion, scalaVersion) match {
         case (LIFT_24, SCALA_292) | (LIFT_24, SCALA_291) | (LIFT_24, SCALA_290)
            => Lift24Scala29DependencyFilter
         case (LIFT_24, SCALA_282) | (LIFT_24, SCALA_281) | (LIFT_24, SCALA_280)
            => Lift24Scala28DependencyFilter
         case _ => null
      }
   }

   def mavenArtifact: MavenArtifact = {
      val mavenScalaVersion =
         (liftVersion, scalaVersion) match {
            case (LIFT_24, SCALA_292) => SCALA_291 // no lift artifact for 2.9.2
            case _ => scalaVersion
         }

      new MavenArtifact("net.liftweb",
         "lift-mapper_" + mavenScalaVersion.version, liftVersion.version)
   }

}

object LiftMetaData {

   val ATTACHMENT_KEY: AttachmentKey[LiftMetaData] =
      AttachmentKey.create(classOf[LiftMetaData])

}