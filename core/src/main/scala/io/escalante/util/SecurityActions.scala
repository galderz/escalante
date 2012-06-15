package io.escalante.util

import java.security.{PrivilegedAction, AccessController}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object SecurityActions {

   def getSystemProperty(key: String): String = {
      if (System.getSecurityManager() == null)
         return System.getProperty(key);

      return AccessController.doPrivileged(new PrivilegedAction[String]() {
         override def run(): String = System.getProperty(key)
      });
   }

}
