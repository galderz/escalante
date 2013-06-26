package io.escalante.test.lift.cluster.containervar.snippet

import net.liftweb.http.{SHtml, ContainerVar, DispatchSnippet}
import net.liftweb.util.Helpers._
import scala.xml.{Text, NodeSeq}

/**
 * World holder container variable.
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/timperrett/lift-in-action">
 *        Lift In Action book</a>
 */
class Words extends DispatchSnippet {
  object WordHolder extends ContainerVar[String]("n/a")

  def dispatch : DispatchIt = {
    case "update" => update
    case "show" => show _
  }

  def update =
    "type=text" #> SHtml.text(WordHolder.is, WordHolder(_)) &
    "type=submit" #> SHtml.submit("Update >>", () => println("Submitted!"))

  def show(xhtml: NodeSeq): NodeSeq = Text(WordHolder.is)

}