/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.io

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object Closeable {

  def use[T <: {def close()}](closable: T)(block: T => Unit) {
    try {
      block(closable)
    } finally {
      closable.close()
    }
  }

}