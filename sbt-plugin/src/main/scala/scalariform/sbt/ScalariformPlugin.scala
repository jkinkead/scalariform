package scalariform.sbt

import scalariform.formatter.ScalaFormatter
import scalariform.formatter.preferences.{ DoubleIndentClassDeclaration, FormattingPreferences, IFormattingPreferences }
import scalariform.parser.ScalaParserException

import sbt._
import sbt.Keys._
import sbt.plugins.JvmPlugin

object ScalariformPlugin extends AutoPlugin {
  object autoImport {
    lazy val scalariformPreferences = settingKey[IFormattingPreferences](
      "The Scalariform preferences to use in formatting."
    )
  }
  import autoImport._

  case class FormatResult(sourceFile: File, original: String, formatted: String)

  // Private task implementation for generating output.
  // Returns FormatResult for all *.scala files in `sourceDirectories`, also honoring the in-scope
  // `includeFilter` and `excludeFilter`.
  private lazy val formatInternal = Def.task {
    val preferences = scalariformPreferences.value
    // Find all of the scala source files, then run them through scalariform.
    val sourceFiles = sourceDirectories.value.descendantsExcept(
      includeFilter.value || "*.scala",
      excludeFilter.value
    ).get
    val scalaMajorVersion = scalaVersion.value.split("-").head
    for {
      sourceFile <- sourceFiles
      original = IO.read(sourceFile)
      formatted = try {
        ScalaFormatter.format(original, preferences, scalaVersion = scalaMajorVersion)
      } catch {
        // A sclariform parse error generally means a file that won't compile.
        case e: ScalaParserException =>
          streams.value.log.error(s"Scalariform parser error in file $sourceFile: ${e.getMessage}")
          original
      }
    } yield FormatResult(sourceFile, original, formatted)
  }

  lazy val format = taskKey[Seq[File]]("Format all scala source files, returning the changed files")

  lazy val formatCheck = taskKey[Seq[File]](
    "Check for misformatted scala files, and print out & return those with errors"
  )

  lazy val formatCheckStrict = taskKey[Unit](
    "Check for misformatted scala files, print out the names of those with errors, " +
      "and throw an error if any do have errors"
  )

  lazy val baseScalariformSettings: Seq[Def.Setting[_]] = Seq(
    format := {
      // The mainline SbtScalariform uses FileFunction to cache this, but it's not really worth the
      // effort here - especially given that we actually don't want to cache for formatCheck.
      for {
        FormatResult(sourceFile, original, formatted) <- formatInternal.value
        if original != formatted
      } yield {
        // Shorten the name to a friendlier path.
        val shortName = sourceFile.relativeTo(baseDirectory.value).getOrElse(sourceFile)
        streams.value.log.info(s"Formatting $shortName . . .")
        IO.write(sourceFile, formatted)
        sourceFile
      }
    },
    formatCheck := {
      val misformatted = for {
        FormatResult(sourceFile, original, formatted) <- formatInternal.value
        if original != formatted
      } yield sourceFile

      if (misformatted.nonEmpty) {
        val log = streams.value.log
        log.error("""Some files contain formatting errors; please run "sbt format" to fix.""")
        log.error("")
        log.error("Files with errors:")
        for (result <- misformatted) {
          // TODO(jkinkead): Log some / all of the diffs?
          log.error(s"\t$result")
        }
      }
      misformatted
    },
    formatCheckStrict := {
      val misformatted = formatCheck.value
      if (misformatted.nonEmpty) {
        throw new MessageOnlyException("Some files have formatting errors.")
      }
    }
  )

  // Require the JvmPlugin. Without this, the updates to the `compile` tasks below fire too early,
  // and get overwritten by the defaults.
  override def requires: Plugins = plugins.JvmPlugin

  // Enable automatically, if it's available.
  override def trigger: PluginTrigger = allRequirements

  /** Run two tasks in serial, returning the result of the second iff the first succeeds. */
  def serialifyCombine[T](
    first: TaskKey[Seq[T]],
    second: TaskKey[Seq[T]]
  ): Def.Initialize[Task[Seq[T]]] = Def.taskDyn {
    first.result.value match {
      case Value(firstValue) => Def.task { firstValue ++ second.value }
      // Forward the exception encountered.
      case Inc(Incomplete(_, Incomplete.Error, _, _, Some(throwable))) => throw throwable
      // According to the sbt docs, the only Incomplete type is Error, so this should be
      // unreachable. It's an error regardless.
      case inc => throw new IllegalStateException("Unexpected Incomplete: " + inc)
    }
  }

  /** Run two tasks in serial, returning the result of the second iff the first succeeds. */
  def serialify[T](first: TaskKey[T], second: TaskKey[T]): Def.Initialize[Task[T]] = Def.taskDyn {
    first.result.value match {
      case Value(_) => Def.task { second.value }
      // Forward the exception encountered.
      case Inc(Incomplete(_, Incomplete.Error, _, _, Some(throwable))) => throw throwable
      // According to the sbt docs, the only Incomplete type is Error, so this should be
      // unreachable. It's an error regardless.
      case inc => throw new IllegalStateException("Unexpected Incomplete: " + inc)
    }
  }

  override def projectSettings: Seq[Def.Setting[_]] = {
    inConfig(Compile)(baseScalariformSettings) ++
      inConfig(Test)(baseScalariformSettings) ++
      Seq(
        // Preferences are at top-level by default.
        scalariformPreferences := FormattingPreferences(),
        // Make compile tasks depend on the proper format check tasks.
        compileInputs in (Compile, compile) := {
          ((compileInputs in (Compile, compile)) dependsOn (formatCheck in Compile)).value
        },
        compileInputs in (Test, compile) := {
          ((compileInputs in (Test, compile)) dependsOn (formatCheck in Test)).value
        },
        // Configure root-level tasks to perform (compile, test) tasks in-order.
        format := serialify((format in Compile), (format in Test)).value,
        formatCheck := serialifyCombine((formatCheck in Compile), (formatCheck in Test)).value,
        formatCheckStrict :=
          serialify((formatCheckStrict in Compile), (formatCheckStrict in Test)).value
      )
  }
}
