/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.liftx.jpa

import io.escalante.lift.jpa.{LibraryJpaBoot, LibraryJpaTest, LibraryJpaScala29Test}
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.Deployment
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.{Test, Ignore}

/**
 * Lift JPA test using latest Lift version.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
// @RunWith(classOf[Arquillian])
class LibraryJpaLatestTest extends LibraryJpaScala29Test {

  // Test currently ignore due to binary backwards compatibility issues:
  // Message: java.lang.NoSuchMethodError: net.liftweb.util.ToCssBindPromoter.$hash$greater(Lscala/xml/NodeSeq;)Lnet/liftweb/util/CssSel;
  //   io.escalante.lift.jpa.snippet.Authors.add(Authors.scala:82)
  @Ignore
  @Test
  override def testAddAuthorAndBook() {
    super.testAddAuthorAndBook()
  }

}

object LibraryJpaLatestTest {

  @Deployment def deployment: WebArchive = {
    LibraryJpaTest.deployment("2.9.2", "2.5-M3", classOf[LibraryJpaBoot],
      List(classOf[LibraryJpaTest], classOf[LibraryJpaScala29Test]))
  }

}
