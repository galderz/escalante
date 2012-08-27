/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.msc.service.{ServiceName, StartContext, StopContext, Service}
import org.jboss.as.controller.services.path.PathManager
import org.jboss.msc.value.InjectedValue
import org.jboss.msc.inject.Injector
import io.escalante.logging.Log
import io.escalante.Version

/**
 * The Lift module service
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftService(modulesRelativeTo: String, modulesPath: String)
  extends Service[LiftService] with Log {

  private val pathManager = new InjectedValue[PathManager]()

  // Why use a separate directory for thirdparty modules?
  // Reason 1: Keeps thirdparty vs shipped in different locations
  // Reason 2: Makes it easy to wipe out thirdparty modules when tests are started
  private var resolvedModulesPath: String = _

  def start(context: StartContext) {
    resolvedModulesPath = pathManager.getValue
      .resolveRelativePathEntry(modulesPath, modulesRelativeTo)

    info("Initializing Escalante Lift subsystem")

    // TODO: Move once we have multiple nodes
    info("Welcome to Escalante AS - http://escalante.io/")
    info("  version................ " + Version.VERSION)
  }

  def stop(context: StopContext) {
    resolvedModulesPath = null
  }

  def getValue: LiftService = this

  def thirdPartyModulesPath: String = resolvedModulesPath

  def pathManagerInjector: Injector[PathManager] = pathManager

}

object LiftService {

  private[subsystem] def createServiceName =
    ServiceName.of("escalante").append("lift")

}
