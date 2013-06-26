package io.escalante.test.lift

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import io.escalante.lift.subsystem.{LiftMetadata, LiftParsingProcessor}
import io.escalante.lift.Lift
import io.escalante.Scala

/**
 * Lift metadata parsing processor test.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftParsingProcessorTest extends AssertionsForJUnit {

  @Test def testNoDistributableInWebXml() {
    val parsingProcessor = new LiftParsingProcessor
    val meta = LiftMetadata(Lift(), Scala(), List(), replication = false)
    val xml = parsingProcessor.generateWebXml(meta)
    val seq = xml \\ "distributable"
    assert(seq.isEmpty)
  }

  @Test def testDistributableInWebXml() {
    val parsingProcessor = new LiftParsingProcessor
    val meta = LiftMetadata(Lift(), Scala(), List(), replication = true)
    val xml = parsingProcessor.generateWebXml(meta)
    val seq = xml \\ "distributable"
    assert(!seq.isEmpty)
  }

}
