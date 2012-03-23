package org.scalabox.lift

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.ModelNode
import java.net.InetAddress
import org.scalabox.util.Closeable._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class LiftInstallTest {

   @Test def testCheckInstall() {
      use(ModelControllerClient.Factory.create(
         InetAddress.getByName("localhost"), 9999)) {
         client =>
            val op = new ModelNode()
            op.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION)
            op.get(OP_ADDR).add("extension", "org.scalabox.lift")
            val resp = TestLiftExtension.validateResponse(client.execute(op))
            assert(!resp.asString().isEmpty)
      }
   }

}