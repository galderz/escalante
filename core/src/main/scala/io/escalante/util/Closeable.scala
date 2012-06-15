package io.escalante.util

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class Closeable {
   // Empty - do not delete!
}

object Closeable {

   def use[T <: {def close() : Unit}](closable: T)(block: T => Unit) {
      try {
         block(closable)
      } finally {
         closable.close()
      }
   }

}