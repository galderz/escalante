package org.scalabox.lift.extension;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class LiftService implements Service<LiftService> {

   private final AtomicLong tick = new AtomicLong(10000);

   private final Set<String> deployments =
         Collections.synchronizedSet(new HashSet<String>());

   private final Set<String> coolDeployments =
         Collections.synchronizedSet(new HashSet<String>());

   private final String suffix;

   private final Thread OUTPUT = new Thread() {
      @Override
      public void run() {
         while (true) {
            try {
               Thread.sleep(tick.get());
               System.out.println("Current deployments deployed while " + suffix + " tracking active:\n" + deployments
                                        + "\nCool: " + coolDeployments.size());
            } catch (InterruptedException e) {
               interrupted();
               break;
            }
         }
      }
   };

   public LiftService(String suffix, long tick) {
      this.suffix = suffix;
      this.tick.set(tick);
   }

   @Override
   public void start(StartContext context) throws StartException {
      OUTPUT.start();
   }

   @Override
   public void stop(StopContext context) {
      OUTPUT.interrupt();
   }

   @Override
   public LiftService getValue() throws IllegalStateException, IllegalArgumentException {
      return this;
   }

   public void addDeployment(String name) {
      deployments.add(name);
   }

   public void addCoolDeployment(String name) {
      coolDeployments.add(name);
   }

   public void removeDeployment(String name) {
      deployments.remove(name);
      coolDeployments.remove(name);
   }

   void setTick(long tick) {
      this.tick.set(tick);
   }

   public long getTick() {
      return this.tick.get();
   }

   public static ServiceName createServiceName(String suffix) {
      return ServiceName.JBOSS.append("lift", suffix);
   }

}
