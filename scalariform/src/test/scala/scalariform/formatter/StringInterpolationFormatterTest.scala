package scalariform.formatter

import scalariform.parser._
import scalariform.formatter._
import scalariform.formatter.preferences._

// format: OFF
class StringInterpolationFormatterTest extends AbstractExpressionFormatterTest {
  implicit val scalaVersion: String = "2.10.0"
  
  <t>s"foo"</t>.text ==> <t>s"foo"</t>.text 
  <t>s""</t>.text ==> <t>s""</t>.text
  <t>s"my name is $name"</t>.text ==> <t>s"my name is $name"</t>.text
  <t>s"my name is $this"</t>.text ==> <t>s"my name is $this"</t>.text
  """s"my name is ${bob}"""" ==> """s"my name is ${bob}"""" 
  """s"my name is ${ person.name }"""" ==> """s"my name is ${person.name}"""" 
 
  """s"my name is ${
    |bob}"""" ==>
  """s"my name is ${
    |  bob
    |}""""

  """s"my name is ${
    |val person = getPerson()
    |person.getName}"""" ==>
  """s"my name is ${
    |  val person = getPerson()
    |  person.getName
    |}""""

  <t>s"""foo"""</t>.text ==> <t>s"""foo"""</t>.text 
  <t>s""""""</t>.text ==> <t>s""""""</t>.text
  <t>s"""my name is $name"""</t>.text ==> <t>s"""my name is $name"""</t>.text
  "s\"\"\"my name is ${bob}\"\"\"" ==> "s\"\"\"my name is ${bob}\"\"\"" 
  "s\"\"\"my name is ${ person.name }\"\"\"" ==> "s\"\"\"my name is ${person.name}\"\"\"" 
}
