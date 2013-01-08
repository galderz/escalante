/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.jpa

import javax.persistence.EntityManager
import org.scala_libs.jpa.{ScalaEMFactory, ScalaEntityManager}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class InjectedEntityManager(entityManager: EntityManager)
  extends ScalaEntityManager {

  protected def em: EntityManager = entityManager

  val factory: ScalaEMFactory = null

}
