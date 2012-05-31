package org.scalabox.maven

import org.scalabox.modules.JBossModule

/**
 * Metadata representation of a Maven artifact.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class MavenArtifact(val groupId: String, val artifactId: String,
        val version: String, val isMain: Boolean) {

   // TODO: Add a DependencyFilter as instance variable, so that we can control inclusions, exclusions...etc

   def this(groupId: String, artifactId: String, version: String) =
      this(groupId, artifactId, version, true)

   private val moduleFriendlyArtifactId = artifactId.replace('.', '_')

   private val moduleFriendlyGroupId = groupId.replace('.', '/')

   private val moduleName: String = new java.lang.StringBuilder()
           .append(groupId).append('.')
           .append(moduleFriendlyArtifactId).toString

   private val slot: String = if (isMain) "main" else version

   /**
    * Full directory path compatible with JBoss Modules constructed
    * out of the metadata of this Maven artifact.
    */
   val moduleDirName: String = new java.lang.StringBuilder()
           .append(moduleFriendlyGroupId).append('/')
           .append(moduleFriendlyArtifactId).append('/')
           .append(slot).toString

   /**
    * Version-less jar file name compatible with JBoss Modules constructed
    * out of the metadata of this Maven artifact.
    */
   val moduleJarName: String = artifactId + ".jar"

   /**
    * Maven artifact metadata represented as JBossModule instance, which
    * represents metadata of a JBoss Module.
    *
    * @param export
    * @return
    */
   def jbossModule(export: Boolean): JBossModule =
      new JBossModule(moduleName, export, slot)

   /**
    * TODO
    *
    * @return
    */
   def coordinates: String =
      new java.lang.StringBuilder()
              .append(groupId).append(":")
              .append(artifactId).append(":")
              .append(version)
              .toString

}
