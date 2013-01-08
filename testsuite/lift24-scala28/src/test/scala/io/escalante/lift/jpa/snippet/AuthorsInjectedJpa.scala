/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.jpa.snippet

import scala.xml.Text
import scala.collection.JavaConversions._
import javax.validation.ConstraintViolationException
import net.liftweb.common.Failure
import net.liftweb.util.Helpers._
import net.liftweb.http.{RequestVar,SHtml,S}
import io.escalante.lift.jpa.{InjectedEntityManager, Author}
import net.liftweb.common.Full
import javax.persistence.{EntityManager, PersistenceContext}
import io.escalante.logging.Log

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object AuthorsInjectedJpa {
  object authorVar extends RequestVar(new Author)
}

class AuthorsInjectedJpa extends Log {

  println("Hello")

  import AuthorsInjectedJpa._
  def author = authorVar.is

  // TODO: Talk to AS guys to find out how to get this injected
  // TODO: Could it be injected as a type of ScalaEntityManager?
  @PersistenceContext(unitName = "LiftPersistenceUnit")
  var em: EntityManager = _

  lazy val scalaEm = new InjectedEntityManager(em)

  def list = {
    val sel = "tr" #> scalaEm.createNamedQuery[Author]("findAllAuthors").getResultList().map {
      a =>
        ".name" #> a.name &
          ".books" #> SHtml.link("/books/add",
            () => authorVar(a),
            Text("%s books (Add more)".format(a.books.size))) &
          ".edit" #> SHtml.link("add", () => authorVar(a), Text("Edit"))
    }
    sel
  }

  def add = {
    val current = author
    "type=hidden" #> SHtml.hidden(() => authorVar(current)) &
      "type=text" #> SHtml.text(author.name, author.name = _) &
      "type=submit" #> SHtml.onSubmitUnit(() =>
        tryo(scalaEm.mergeAndFlush(author)) match {
          case Failure(msg,Full(err: ConstraintViolationException),_) =>
            S.error(err.getConstraintViolations.toList.flatMap(c => <p>{c.getMessage}</p>))
          case Failure(msg,Full(err: Throwable),_) =>
            error(err, "Unable to store author")
            S.error(err.toString)
          case _ => S.redirectTo("index")
        })
  }
}