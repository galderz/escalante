/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift.cluster

import org.jboss.arquillian.container.test.api.{Deployer, ContainerController}
import io.escalante.logging.Log

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object NodeUtil extends Log {

  final val CONTAINER_0 = "container-0"
  final val CONTAINER_1 = "container-1"

  final val DEPLOYMENT_0 = "deployment-0"
  final val DEPLOYMENT_1 = "deployment-1"

  def start(controller: ContainerController, deployer: Deployer,
      container: String, deployment: String) {
    redirectStackTrace { () =>
      info(s" starting deployment=$deployment, container=$container")
      controller.start(container)
      deployer.deploy(deployment)
      info(s" started deployment=$deployment, container=$container")
    }
  }

  def stop(controller: ContainerController, deployer: Deployer,
      container: String, deployment: String) {
    redirectStackTrace { () =>
      info(s" stopping deployment=$deployment, container=$container")
      deployer.undeploy(deployment)
      controller.stop(container)
      info(s" stopped deployment=$deployment, container=$container")
    }
  }

  private def redirectStackTrace(block: () => Unit) {
    try {
      block()
    } catch {
      case t: Throwable =>
        t.printStackTrace(System.err)
        throw t
    }
  }

}
