/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.util.classload

/**
 * Class loading related utility methods.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object ClassLoading {

  /**
   * Executes the given function in the context of the provided
   * [[java.lang.ClassLoader]].
   *
   * @param classloader to use as thread's context classloader
   * @param function to execute with the given classloader in context
   * @tparam T is the type of the return of function
   * @return an instance of type parameter
   */
  def withContextClassLoader[T](classloader: ClassLoader)(function: => T): T = {
    val currentThread = Thread.currentThread()
    val prevCl = currentThread.getContextClassLoader
    try {
      currentThread.setContextClassLoader(classloader)
      function
    } finally {
      currentThread.setContextClassLoader(prevCl)
    }
  }

}
