/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.logging

import org.jboss.logging.Logger

/**
 * Logging interface.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
trait Log {

   private lazy val log = Logger.getLogger(getClass.getPackage.getName)

   def info(msg: => String) {
      log.info(msg)
   }

   def info(msg: => String, param1: Any) {
      log.infof(msg, param1)
   }

   def info(msg: => String, param1: Any, param2: Any, param3: Any) {
      log.infof(msg, param1, param2, param3)
   }

   def warn(msg: => String) {
      log.warn(msg)
   }

   def warn(msg: => String, param1: Any, param2: Any, param3: Any) {
      log.warnf(msg, param1, param2, param3)
   }

   def debug(msg: => String) {
      log.debug(msg)
   }

   def debug(msg: => String, param1: Any) {
      log.debugf(msg, param1)
   }

   def debug(msg: => String, param1: Any, param2: Any) {
      log.debugf(msg, param1, param2)
   }

   def debug(t: Throwable, msg: => String, param1: Any) {
      log.debugf(t, msg, param1)
   }

   def trace(msg: => String) {
      log.tracef(msg)
   }

   def trace(msg: => String, param1: Any) {
      log.tracef(msg, param1)
   }

}