/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.assembly

import io.escalante.lift.maven.RegexDependencyFilter
import util.matching.Regex

/**
 * Wagon connector dependency exclusion filter
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object ConnectorWagonDependenciesFilter extends RegexDependencyFilter {

  def createRegex: Regex = new Regex("^(?!.*(sisu|wagon-provider-api)).*$")

}
