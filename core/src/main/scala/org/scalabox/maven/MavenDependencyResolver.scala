package org.scalabox.maven

import org.sonatype.aether.repository.LocalRepository
import java.io.File
import org.sonatype.aether.util.artifact.DefaultArtifact
import org.sonatype.aether.resolution.DependencyRequest
import org.sonatype.aether.collection.CollectRequest
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import collection.JavaConversions._
import org.scalabox.util.SecurityActions
import org.sonatype.aether.graph.{DependencyFilter, Dependency}
import java.util.Collections
import org.apache.maven.repository.internal.{MavenServiceLocator, MavenRepositorySystemSession}
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.wagon.{WagonRepositoryConnectorFactory, WagonProvider}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object MavenDependencyResolver {

   private val DEFAULT_REPOSITORY_PATH =
         SecurityActions.getSystemProperty("user.home")
           .concat("/.m2/repository")

   private lazy val SYSTEM = createSystem()

   private val SESSION = createSession()

   private val SETTINGS = new MavenSettings

   /**
    * TODO
    *
    * @param artifact
    * @return
    */
   def resolveArtifact(artifact: MavenArtifact): Seq[File] =
      resolveArtifact(artifact, null)

   /**
    *
    * @param artifact
    * @param filter
    * @return
    */
   def resolveArtifact(artifact: MavenArtifact, filter: DependencyFilter): Seq[File] = {
      val aetherArtifact = new DefaultArtifact(artifact.coordinates)
      val dependency = new Dependency(aetherArtifact, null)

      val request = new CollectRequest(Collections.singletonList(dependency),
         null, SETTINGS.getRemoteRepositories)

      val results = SYSTEM.resolveDependencies(
         SESSION, new DependencyRequest(request, filter)).getArtifactResults
      asScalaIterator(results.iterator())
              .filter(_.getArtifact.getExtension != "pom")
              .map(_.getArtifact.getFile).toSeq
   }

   private def createSystem(): RepositorySystem =  {
      // TODO: Why do I need to be in the TCCL?
      val prevCl = Thread.currentThread().getContextClassLoader
      try {
         Thread.currentThread().setContextClassLoader(this.getClass.getClassLoader)
         // Without ioc, yayyy!
         val locator = new MavenServiceLocator()
         locator.setServices(classOf[WagonProvider], ManualWagonProvider)
         locator.addService(classOf[RepositoryConnectorFactory],
               classOf[WagonRepositoryConnectorFactory])
         locator.getService(classOf[RepositorySystem])
      } finally {
         Thread.currentThread().setContextClassLoader(prevCl)
      }
   }

   private def createSession(): RepositorySystemSession = {
      val localRepo = SYSTEM.newLocalRepositoryManager(
            new LocalRepository(DEFAULT_REPOSITORY_PATH))
      val session = new MavenRepositorySystemSession()
      session.setLocalRepositoryManager(localRepo)
      session.setTransferListener(new LogTransferListener)
      session
   }

}
