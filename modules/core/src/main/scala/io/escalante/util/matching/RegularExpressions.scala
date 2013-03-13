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

  final val NotProvidedByServerRegex =
//    "^(?!.*(scala-compiler|scala-library|scala-reflect|scalap|slf4j|logback|specs2*)).*$".r
    "^(?!.*(scala-compiler|scala-library|scala-reflect|scalap|specs2*)).*$".r

  final val FileSplitRegex = "\\.(?=[^\\.]+$)".r

  final val ScalaFolderRegex = "(scala-*)".r

  final val JarFileRegex = "(.*.jar)".r

  final val ExecutableFilesRegex = "(.*.sh)".r

  final val NoSisuWagonArtifactsRegex = "^(?!.*(sisu|wagon-provider-api)).*$".r

}
