/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.jpa

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class LibraryJpaScala29Test extends LibraryJpaTest {

  // Tests in parent class
  override protected def appUrl: String = "http://localhost:8080/libraryjpa-292"

}

object LibraryJpaScala29Test {

  @Deployment def deployment: WebArchive = {
    LibraryJpaTest.deployment("2.9.2", "2.4", classOf[LibraryJpaBoot],
      List(classOf[LibraryJpaTest]))
  }

}
