package io.escalante.lift.helloworld

import org.openqa.selenium.htmlunit.HtmlUnitDriver

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractHelloWorldTest {

   def helloWorld(appVersion: String) {
      val driver = new HtmlUnitDriver()
      driver.get("http://localhost:8080/helloworld-%s/index.html".format(appVersion))
      assert(driver.getPageSource.contains("Hello World!"))
   }

}
