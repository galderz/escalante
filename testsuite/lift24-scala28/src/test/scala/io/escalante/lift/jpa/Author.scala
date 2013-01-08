/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.jpa

import javax.persistence._
import javax.validation.constraints.Size

/**
 * Author entity.
 *
 * @author Galder Zamarreño
 * @since 1.0
 * @see This code is based on sample code provided in the
 *      <a href="https://github.com/timperrett/lift-in-action">
 *        Lift In Action book</a>
 */
@Entity
@NamedQuery(name = "findAllAuthors", query = "from Author a order by a.name")
class Author {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id : Long = _
  
  @Size(min = 3, max = 20)
  @Column(unique = true, nullable = false)
  var name : String = ""

  @OneToMany(mappedBy = "author", targetEntity = classOf[Book])
  var books : java.util.Set[Book] = new java.util.HashSet[Book]()
}
