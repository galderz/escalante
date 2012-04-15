package org.scalabox.assembly

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class MavenArtifact(val groupId: String, val artifactId: String,
        val version: String, val isMain: Boolean) {

   def this(groupId: String, artifactId: String, version: String) =
           this(groupId, artifactId, version, true)

   private val moduleFriendlyArtifactId = artifactId.replace('.', '_')

   private val moduleFriendlyGroupId = groupId.replace('.', '/')

   private val moduleName: String = new java.lang.StringBuilder()
           .append(groupId).append('.')
           .append(moduleFriendlyArtifactId).toString

   private val slot: String = if (isMain) "main" else version

   val moduleDirName: String = new java.lang.StringBuilder()
           .append(moduleFriendlyGroupId).append('/')
           .append(moduleFriendlyArtifactId).append('/')
           .append(slot).toString

   val moduleJarName: String = artifactId + ".jar"

   def jbossModule(export: Boolean): JBossModule =
      new JBossModule(moduleName, export, slot)

   def coordinates : String =
      new java.lang.StringBuilder()
              .append(groupId).append(":")
              .append(artifactId).append(":")
              .append(version)
              .toString

}
