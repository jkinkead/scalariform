# sbt-scalariform

This contains an sbt plugin that provides format tasks to run scalariform within sbt.

## Usage

Add the following to `project/plugins.sbt`:

```
addSbtPlugin("com.github.jkinkead" % "sbt-scalariform" % "0.1.6")
```

This will add `format`, `formatCheck`, and `formatCheckStrict` tasks to sbt. By default, this installs these tasks at the Global, Compile, and Test scopes.

Tasks within the non-global scope will operate on all `*.scala` files in the current scope's `sourceDirectories` key. They also will use any `includeFilter` and / or `excludeFilter` key set.

Tasks within the Global scope will run all sub-scoped tasks in-order. By default, this means that the Compile task will be run first, then the Test task.

##### `format`

This task will run scalariform on all source files, modifying any that had changes after formatting. The updated set of files is returned by the task.

##### `formatCheck`

This task checks all files to see if any are misformatted, prints the ones that are, and returns the list of files needing change.

##### `formatCheckStrict`

This task checks all files to see if any are misformatted, and throws an exception if any are.

## Configuration

##### `scalariformPreferences: settingKey[IFormattingPreferences]`

```
import scalariform.formatter.preferences._

scalariformPreferences :=
  FormattingPreferences().setPreference(AlignSingleLineCaseStatements, true)
```

This key holds any overrides from the default formatting preferences for scalariform. See [the main README](https://github.com/jkinkead/scalariform) for a list of all available keys.

##### `sourceDirectories`

This holds the directories to search when looking for code to format. There should be no reason to update this for most projects.

##### `includeFilter`, `excludeFilter`

These start empty, but can be set to modify the list of files that will be formatted. For example, if you wanted to format all `*.scala` and `*.sc` files, but not files named `Foo.scala`, you could do:

```
lazy val filters = Seq(
  includeFilter := "*.sc",
  excludeFilter := "Foo.scala"
)

// You need to add these to the appropriate Config(s).
inConfig(Compile)(filters)

inConfig(Test)(filters)
```

##### Formatting integration test code (or code in other Configurations)

sbt-scalariform exports one function, `addFormatTo`, to add the three `format` tasks to a new configuration:

```
// Assumes that IntegrationTest has been added to this project, and that
// Defaults.itSettings have been applied.
addFormatTo(IntegrationTest)
```

This also appends the added config-scoped tasks to the list of tasks run by the global `format` tasks.
