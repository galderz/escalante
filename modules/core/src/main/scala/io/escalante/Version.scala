/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante

/**
 * Escalante version.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object Version {

  val MAJOR = 0
  val MINOR = 4
  val MICRO = 0
  val SNAPSHOT = true

  val VERSION = "%s.%s.%s%s".format(MAJOR, MINOR, MICRO, if (SNAPSHOT) "-SNAPSHOT" else "")

}
