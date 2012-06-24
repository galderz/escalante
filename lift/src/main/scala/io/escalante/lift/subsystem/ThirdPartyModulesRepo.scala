package io.escalante.lift.subsystem

import org.jboss.as.controller.descriptions.ModelDescriptionConstants

/**
 * Constants for the third party modules repository definition
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object ThirdPartyModulesRepo {

   val THIRDPARTY_MODULES_REPO = "thirdparty-modules-repo"

   val RELATIVE_TO = THIRDPARTY_MODULES_REPO + "." +
           ModelDescriptionConstants.RELATIVE_TO

   val PATH = THIRDPARTY_MODULES_REPO + "." +
           ModelDescriptionConstants.PATH

}
