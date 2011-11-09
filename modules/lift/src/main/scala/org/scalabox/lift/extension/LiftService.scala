package org.scalabox.lift.extension

import scala.collection.mutable.HashSet
import scala.collection.mutable.SynchronizedSet
import org.jboss.msc.service.{ServiceName, StartContext, StopContext, Service}
import java.util.concurrent.atomic.AtomicLong
import java.lang.Thread

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftService(suffix: String, tick: Long) extends Service[LiftService] {

   // Constructor
   currentTick.set(tick)

   // Lazy so that it can be initialized when it's used by the constructor
   lazy val currentTick = new AtomicLong(10000)

   val deployments = new HashSet[String] with SynchronizedSet[String]

   val coolDeployments = new HashSet[String] with SynchronizedSet[String]

   val output = new Thread {
      override def run: Unit = {
         while (true) {
            try {
               Thread.sleep(currentTick.get)
               print("""|Current deployments deployed while %s tracking active:
                        |Cool: %d""".format(suffix, coolDeployments.size).stripMargin)
            }
            catch {
               case e: InterruptedException => Thread.interrupted()
            }
         }
      }
   }

   override def start(context: StartContext) = output.start()

   override def stop(context: StopContext) = output.interrupt()

   override def getValue = this

   def addDeployment(name: String) = deployments.add(name)

   def addCoolDeployment(name: String) = coolDeployments.add(name)

   def removeDeployment(name: String) {
      deployments.remove(name)
      coolDeployments.remove(name)
   }

   def setTick(tick: Long) = currentTick.set(tick)

   def getTick = currentTick.get()

}

object LiftService {

   def createServiceName(suffix: String) =
      ServiceName.JBOSS.append("lift", suffix)

}