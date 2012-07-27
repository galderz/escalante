/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.usermapper.model

import net.liftweb.common.Full
import net.liftweb.mapper.{MappedTextarea, MegaProtoUser, MetaMegaProtoUser}

/**
 * The singleton that has methods for accessing the database
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/lift/lift_24_sbt">Lift project templates</a>
 */
object User extends User with MetaMegaProtoUser[User] {

  override def dbTableName = "users"

  // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
    <lift:bind/>
  </lift:surround>)

  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, firstName, lastName, email,
    locale, timezone, password, textArea)

  // comment this line out to require email validations
  override def skipEmailValidation = true
}

/**
 * An O-R mapped "User" class that includes first name, last name, password/
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/lift/lift_24_sbt">Lift project templates</a>
 */
class User extends MegaProtoUser[User] {
  def getSingleton = User

  // what's the "meta" server

  // define an additional field for a personal essay
  object textArea extends MappedTextarea(this, 2048) {

    override def textareaRows = 10

    override def textareaCols = 50

    override def displayName = "A very long essay..."

  }

}

