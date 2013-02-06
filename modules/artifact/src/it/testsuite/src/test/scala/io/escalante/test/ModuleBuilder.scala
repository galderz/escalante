/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test

import java.io.{FileOutputStream, File}
import org.jboss.shrinkwrap.api.spec.JavaArchive
import io.escalante.io.FileSystem._
import org.jboss.shrinkwrap.api.ShrinkWrap
import scala.xml.Node
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import io.escalante.xml.ScalaXmlParser._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object ModuleBuilder {

  def buildJavaArchive(
      destDir: File,
      modulePath: String,
      jarName: String,
      packages: Seq[String],
      classes: Seq[Class[_]]): JavaArchive = {
    // Create directories
    mkDirs(destDir, modulePath)
    // Create jar with the extension and packages
    val archive = ShrinkWrap.create(classOf[JavaArchive], jarName)
    packages.foreach(archive.addPackages(true, _))
    classes.foreach(archive.addClass(_))
    archive
  }

  def installModule(archive: JavaArchive, moduleDir: File, moduleXml: Node) {
    val jarInput = archive.as(classOf[ZipExporter]).exportAsInputStream()
    saveXml(new File(moduleDir, "module.xml"), moduleXml)
    copy(jarInput, new FileOutputStream(new File(moduleDir, archive.getName)))
  }

}
