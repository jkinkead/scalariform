package scalariform.formatter.preferences

trait IFormattingPreferences {
  def apply[T](preference: PreferenceDescriptor[T]): T

  def setPreference[T](preference: PreferenceDescriptor[T], value: T): IFormattingPreferences

  def preferencesMap: Map[PreferenceDescriptor[_], Any]

  /** The indent style configured by the user. If IndentWithTabs is set, Tabs is used; else, Spaces
    * is used, with IndentSpaces spaces per indent level.
    */
  def indentStyle: IndentStyle
}

class FormattingPreferences(val preferencesMap: Map[PreferenceDescriptor[_], Any])
    extends IFormattingPreferences {

  def apply[T](preference: PreferenceDescriptor[T]): T = preferencesMap.get(preference) map { _.asInstanceOf[T] } getOrElse preference.defaultValue

  def setPreference[T](preference: PreferenceDescriptor[T], value: T) = new FormattingPreferences(preferencesMap + (preference -> value))

  override def toString = getClass.getSimpleName + "(" + preferencesMap + ")"

  override val indentStyle = if (this(IndentWithTabs)) Tabs else Spaces(this(IndentSpaces))
}

case object FormattingPreferences extends FormattingPreferences(Map()) {
  def apply() = new FormattingPreferences(Map())
}

trait HasFormattingPreferences {
  val formattingPreferences: IFormattingPreferences
}
