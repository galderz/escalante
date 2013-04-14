/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.util.matching

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object RegularExpressions {

  val NotProvidedByServerRegex =
      "^(?!.*(scala-compiler|scala-library|scala-reflect|scalap|slf4j-api|jul-to-slf4j|jcl-over-slf4j|specs2*)).*$".r

  val FileSplitRegex = "\\.(?=[^\\.]+$)".r

  val ScalaFolderRegex = "(scala-*)".r

  val JarFileRegex = "(.*.jar)".r

  val ExecutableFilesRegex = "(.*.sh)".r

  val NoSisuWagonArtifactsRegex = "^(?!.*(sisu|wagon-provider-api)).*$".r

}
