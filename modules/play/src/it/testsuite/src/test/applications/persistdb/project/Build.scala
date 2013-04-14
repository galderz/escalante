/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play21-persistdb"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-encoding", "UTF-8")
  )



}
