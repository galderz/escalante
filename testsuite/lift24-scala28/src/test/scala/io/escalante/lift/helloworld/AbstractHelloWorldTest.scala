/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
