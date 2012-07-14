/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.util

import java.security.{PrivilegedAction, AccessController}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object SecurityActions {

   // TODO: Create one per package...

   def getSystemProperty(key: String): String = {
      if (System.getSecurityManager() == null)
         return System.getProperty(key)

      return AccessController.doPrivileged(new PrivilegedAction[String]() {
         override def run(): String = System.getProperty(key)
      });
   }

   def getSystemProperty(key: String, default: String): String = {
      if (System.getSecurityManager() == null)
         return System.getProperty(key, default)

      return AccessController.doPrivileged(new PrivilegedAction[String]() {
         override def run(): String = System.getProperty(key, default)
      });
   }

}
