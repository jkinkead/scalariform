package scalariform.formatter.preferences

/** The style of indentation - tabs or spaces. */
abstract sealed class IndentStyle {
  /** @return the indentation string for the given indent level */
  def indent(n: Int): String
}

/** Tabs uses one tab per indent level. */
case object Tabs extends IndentStyle {
  def indent(indentLevel: Int) = "\t" * indentLevel
}

/** Spaces uses `n` spaces per indent level. */
case class Spaces(n: Int) extends IndentStyle {
  def indent(indentLevel: Int) = " " * (n * indentLevel)

  def length(indentLevel: Int) = indent(indentLevel).length
}
