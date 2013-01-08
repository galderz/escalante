/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.jpa

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import snippet.{AuthorsInjectedJpa, Books, Authors}
import scala.xml.Elem
import io.escalante.lift.AbstractLiftWebAppTest
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.{Ignore, Test}

/**
 * Library JPA example where EntityManager comes
 * via @PersistenceContext injection.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
// @RunWith(classOf[Arquillian])
class LibraryInjectedJpaTest extends LibraryJpaTest {

  // Tests in parent class
  override protected def appUrl: String =
    "http://localhost:8080/libraryinjectedjpa-282"

  @Test @Ignore // Temporarily ignore until @PersistenceContext injection works
  override def testAddAuthorAndBook() {
    super.testAddAuthorAndBook()
  }
}

object LibraryInjectedJpaTest extends AbstractLiftWebAppTest {

  @Deployment def deployment: WebArchive = {
    val scala = "2.8.2"
    deployment("libraryinjectedjpa",
      "libraryinjectedjpa-%s.war".format(scala.replace(".", "")),
      LibraryJpaTest.descriptor(scala, "2.4"), classOf[LibraryJpaBoot],
      List(classOf[Author], classOf[Book], classOf[AuthorsInjectedJpa],
        classOf[InjectedEntityManager], classOf[Books],
        classOf[LibraryJpaTest]))
  }

  override val indexHtml: Elem = LibraryJpaTest.indexHtml

  override val webResources: Map[String, String] = LibraryJpaTest.webResources

  override val static: Option[Elem] = LibraryJpaTest.static

}
