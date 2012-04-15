package org.scalabox.assembly

import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver

/**
 * Shrinkwrap dependency resolver factory
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object DependencyResolverFactory {

   def getDependencyResolver(): MavenDependencyResolver = {
      // TODO: Remove workaround when https://issues.jboss.org/browse/SHRINKRES-26 is solved
      val prevCl = Thread.currentThread().getContextClassLoader
      try {
         Thread.currentThread().setContextClassLoader(this.getClass.getClassLoader)
         // TODO: Find a way to cache and avoid all the plexus/aether/guice wiring...
         DependencyResolvers.use(classOf[MavenDependencyResolver])
      } finally {
         Thread.currentThread().setContextClassLoader(prevCl)
      }
   }

}
