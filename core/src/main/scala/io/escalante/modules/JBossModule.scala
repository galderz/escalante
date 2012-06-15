package io.escalante.modules

import org.jboss.modules.{Module, ModuleIdentifier}
import org.jboss.as.server.deployment.module.ModuleDependency

/**
 * Metadata representation of a JBoss Module.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
class JBossModule(val name: String, val export: Boolean, val slot: String,
        val service: Service) {

   def this(name: String) = this(name, false, "main", NONE)

   def this(name: String, export: Boolean) = this(name, export, "main", NONE)

   def this(name: String, export: Boolean, slot: String) =
      this(name, export, slot, NONE)

   def this(name: String, slot: String) =
      this(name, false, slot, NONE)

   def moduleDependency = new ModuleDependency(Module.getBootModuleLoader(),
      ModuleIdentifier.create(name, slot), false, export, false, false)

}

sealed trait Service {def name: String}

case object NONE extends Service { val name = "none" }
case object IMPORT extends Service { val name = "import" }
case object EXPORT extends Service { val name = "export" }