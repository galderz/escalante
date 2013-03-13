//package io.escalante.play.compiler
//
//import play.core.SBTLink
//import java.util
//import java.io.File
//import sbt.inc.Analysis
//import sbt.IO
//import org.pegdown.PegDownProcessor
//import org.pegdown.Extensions
//import org.pegdown.LinkRenderer
//import org.pegdown.ast.WikiLinkNode
//
///**
// * // TODO: Document this
// * @author Galder Zamarreño
// * @since // TODO
// */
//class ReloadablePlayApp(appPath: File) extends SBTLink {
//
//  var currentAnalysis = Option.empty[Analysis]
//
//  var reloadNextTime = false
//
//  // --- USING jnotify to detect file change (TODO: Use Java 7 standard API if available)
//
//  lazy val jnotify = { // This create a fully dynamic version of JNotify that support reloading
//
//    try {
//
//      var _changed = true
//
//      // --
//
//      var jnotifyJarFile = this.getClass.getClassLoader.asInstanceOf[java.net.URLClassLoader].getURLs
//          .map(_.getFile)
//          .find(_.contains("/jnotify"))
//          .map(new File(_))
//          .getOrElse(sys.error("Missing JNotify?"))
//
//      val sbtLoader = this.getClass.getClassLoader.getParent.asInstanceOf[java.net.URLClassLoader]
//      val method = classOf[java.net.URLClassLoader].getDeclaredMethod("addURL", classOf[java.net.URL])
//      method.setAccessible(true)
//      method.invoke(sbtLoader, jnotifyJarFile.toURI.toURL)
//
//      // val targetDirectory = extracted.get(target)
//      val targetDirectory = new File(appPath, "target")
//      val nativeLibrariesDirectory = new File(targetDirectory, "native_libraries")
//
//      if (!nativeLibrariesDirectory.exists) {
//        // Unzip native libraries from the jnotify jar to target/native_libraries
//        IO.unzip(jnotifyJarFile, targetDirectory, (name: String) => name.startsWith("native_libraries"))
//      }
//
//      val libs = new File(nativeLibrariesDirectory, System.getProperty("sun.arch.data.model") + "bits").getAbsolutePath
//
//      // Hack to set java.library.path
//      System.setProperty("java.library.path", {
//        Option(System.getProperty("java.library.path")).map { existing =>
//          existing + java.io.File.pathSeparator + libs
//        }.getOrElse(libs)
//      })
//      import java.lang.reflect._
//      val fieldSysPath = classOf[ClassLoader].getDeclaredField("sys_paths")
//      fieldSysPath.setAccessible(true)
//      fieldSysPath.set(null, null)
//
//      val jnotifyClass = sbtLoader.loadClass("net.contentobjects.jnotify.JNotify")
//      val jnotifyListenerClass = sbtLoader.loadClass("net.contentobjects.jnotify.JNotifyListener")
//      val addWatchMethod = jnotifyClass.getMethod("addWatch", classOf[String], classOf[Int], classOf[Boolean], jnotifyListenerClass)
//      val removeWatchMethod = jnotifyClass.getMethod("removeWatch", classOf[Int])
//      val listener = java.lang.reflect.Proxy.newProxyInstance(sbtLoader, Seq(jnotifyListenerClass).toArray, new java.lang.reflect.InvocationHandler {
//        def invoke(proxy: AnyRef, m: java.lang.reflect.Method, args: scala.Array[AnyRef]): AnyRef = {
//          _changed = true
//          null
//        }
//      })
//
//      val nativeWatcher = new {
//        def addWatch(directoryToWatch: String): Int = {
//          addWatchMethod.invoke(null, directoryToWatch, 15: java.lang.Integer, true: java.lang.Boolean, listener).asInstanceOf[Int]
//        }
//        def removeWatch(id: Int): Unit = removeWatchMethod.invoke(null, id.asInstanceOf[AnyRef])
//        def reloaded() { _changed = false }
//        def changed() { _changed = true }
//        def hasChanged = _changed
//      }
//
//      ( /* Try it */ nativeWatcher.removeWatch(0) )
//
//      nativeWatcher
//
//    } catch {
//      case e: Throwable => {
//
//        println(
//          """|
//            |Cannot load the JNotify native library (%s)
//            |Play will check file changes for each request, so expect degraded reloading performace.
//            |""".format(e.getMessage).stripMargin
//        )
//
//        new {
//          def addWatch(directoryToWatch: String): Int = 0
//          def removeWatch(id: Int): Unit = ()
//          def reloaded(): Unit = ()
//          def changed(): Unit = ()
//          def hasChanged = true
//        }
//
//      }
//    }
//
//
//  }
//
//  def findSource(className: String, line: Integer): Array[AnyRef] = {
//    val topType = className.split('$').head
//    currentAnalysis.flatMap { analysis =>
//      analysis.apis.internal.flatMap {
//        case (sourceFile, source) => {
//          source.api.definitions.find(defined => defined.name == topType).map(_ => {
//            sourceFile: java.io.File
//          } -> line)
//        }
//      }.headOption.map {
//        case (source, maybeLine) => {
//          MaybeGeneratedSource.unapply(source).map { generatedSource =>
//            generatedSource.source.get -> Option(maybeLine).map(l => generatedSource.mapLine(l):java.lang.Integer).orNull
//          }.getOrElse(source -> maybeLine)
//        }
//      }
//    }.map {
//      case (file, line) => {
//        Array[java.lang.Object](file, line)
//      }
//    }.orNull
//  }
//
//  def forceReload() {
//    reloadNextTime = true
//    jnotify.changed()
//  }
//
//  def markdownToHtml(markdown: String, pagePath: String): String = {
//    import org.pegdown._
//    import org.pegdown.ast._
//
//    val link:(String => (String, String)) = _ match {
//      case link if link.contains("|") => {
//        val parts = link.split('|')
//        (parts.tail.head, parts.head)
//      }
//      case image if image.endsWith(".png") => {
//        val link = image match {
//          case full if full.startsWith("http://") => full
//          case absolute if absolute.startsWith("/") => "resources/manual" + absolute
//          case relative => "resources/" + pagePath + "/" + relative
//        }
//        (link, """<img src="""" + link + """"/>""")
//      }
//      case link => {
//        (link, link)
//      }
//    }
//
//    val processor = new PegDownProcessor(Extensions.ALL)
//    val links = new LinkRenderer {
//      override def render(node: WikiLinkNode) = {
//        val (href, text) = link(node.getText)
//        new LinkRenderer.Rendering(href, text)
//      }
//    }
//
//    processor.markdownToHtml(markdown, links)
//  }
//
//  def projectPath(): File = appPath
//
//  def reload(): AnyRef = {
//    if (jnotify.hasChanged || hasChangedFiles) {
//      jnotify.reloaded()
//      Project.runTask(playReload, state).map(_._2).get.toEither
//          .left.map { incomplete =>
//        jnotify.changed()
//        Incomplete.allExceptions(incomplete).headOption.map {
//          case e: PlayException => e
//          case e: xsbti.CompileFailed => {
//            getProblems(incomplete).headOption.map(CompilationException(_)).getOrElse {
//              UnexpectedException(Some("Compilation failed without reporting any problem!?"), Some(e))
//            }
//          }
//          case e: Exception => UnexpectedException(unexpected = Some(e))
//        }.getOrElse {
//          UnexpectedException(Some("Compilation task failed without any exception!?"))
//        }
//      }
//          .right.map { compilationResult =>
//        updateAnalysis(compilationResult).map { _ =>
//          newClassLoader
//        }
//      }.fold(
//        oops => oops,
//        maybeClassloader => maybeClassloader.getOrElse(null)
//      )  }
//
//  def runTask(name: String): AnyRef = ??? // TODO
//
//  def settings(): util.Map[String, String] = ??? // TODO
//
//  def hasChangedFiles: Boolean = monitoredFiles.exists{ f =>
//    val fileChanged = fileTimestamps.get(f.getAbsolutePath).map { timestamp =>
//      f.lastModified != timestamp
//    }.getOrElse{
//      state.log.debug("Did not find expected timestamp of file: " + f.getAbsolutePath + " in timestamps. Marking it as changed...")
//      true
//    }
//    if (fileChanged) {
//      fileTimestamps = calculateTimestamps //recalulating all, one _or more_ files has changed
//    }
//    fileChanged
//  }
//
//}
