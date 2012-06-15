package io.escalante.lift

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.ModelNode
import java.net.InetAddress
import io.escalante.util.Closeable._
import io.escalante.logging.Log

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class LiftInstallTest extends Log {

   @Test def testCheckInstall() {
      use(ModelControllerClient.Factory.create(
         InetAddress.getByName("localhost"), 9999)) {
         client =>
            val op = new ModelNode()
            op.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION)
            op.get(OP_ADDR).add("extension", "io.escalante.lift")
            val resp = client.execute(op)
            LiftTestSetup.validateResponse(resp)
            info("Lift is installed: %s", resp.get(OUTCOME))
      }
   }

}