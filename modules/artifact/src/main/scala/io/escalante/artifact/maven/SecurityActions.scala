/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.artifact.maven

import java.security.{PrivilegedAction, AccessController}

/**
 * Security actions for this package.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object SecurityActions {

  def getSystemProperty(key: String): String = {
    if (System.getSecurityManager == null)
      return System.getProperty(key)

    AccessController.doPrivileged(new PrivilegedAction[String]() {
      override def run(): String = System.getProperty(key)
    })
  }

  def getSystemProperty(key: String, default: String): String = {
    if (System.getSecurityManager == null)
      return System.getProperty(key, default)

    AccessController.doPrivileged(new PrivilegedAction[String]() {
      override def run(): String = System.getProperty(key, default)
    })
  }

}
