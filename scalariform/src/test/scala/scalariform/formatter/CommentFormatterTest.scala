package scalariform.formatter

import scalariform.parser._
import scalariform.formatter._
import scalariform.formatter.preferences._

// format: OFF
class CommentFormatterTest extends AbstractFormatterTest {

  type Result = CompilationUnit

  def parse(parser: ScalaParser) = parser.scriptBody()
  
  def format(formatter: ScalaFormatter, result: Result) = formatter.format(result)(FormatterState())

  override val debug = false

  """/** 
    |*a
    |b
    | */c""" ==>
  """/** a
    |  * b
    |  */
    |c
    |"""

  """/** 
    |*a
    |b
    | */""" ==>
  """/** a
    |  * b
    |  */
    |"""

  """/** This line has trailing spaces.  
    |  *   
    |  * So does the line above this.
    |  */""" ==>
  """/** This line has trailing spaces.
    |  *
    |  * So does the line above this.
    |  */
    |"""

  """/**
    | *
    | *Wibble*/ 
    |class X""" ==>
  """/** Wibble
    |  */
    |class X
    |"""

  """/***/
    |class A""" ==>
  """/***/
    |class A
    |"""

  """/** */
    |class A""" ==>
  """/** */
    |class A
    |"""

  """/** a */
    |class A""" ==>
  """/** a */
    |class A
    |"""

  """/**
    | * {{
    | *   wibble
    | * }}
    | */
    |class A""" ==>
  """/** {{
    |  *   wibble
    |  * }}
    |  */
    |class A
    |"""

  """/**
    |*
    |*/""" ==>
  """/**
    |  */
    |"""

  """/** 
    |  * Scaladoc should be retained.
    |  * @param first line no indent.
    |  *     second line has indent.
    |  */""" ==>
  """/** Scaladoc should be retained.
    |  * @param first line no indent.
    |  *     second line has indent.
    |  */
    |"""

  """/** a
    |  * b */""" ==>
  """/** a
    |  * b
    |  */
    |"""
      
  // nested comments
  """/**
    |  /*
    |  */
    | */""" ==>
  """/** /*
    |  * */
    |  */
    |"""

  """/**
    | * /*
    | *    Nested comment.
    | * */
    | */""" ==>
  """/** /*
    |  *    Nested comment.
    |  * */
    |  */
    |"""
      
  {
  implicit val formattingPreferences = FormattingPreferences.setPreference(MultilineScaladocCommentsStartOnFirstLine, true)

  """/** First line  (well-formatted)
    | *  Second line (well-formatted)
    | */""" ==>
  """/** First line  (well-formatted)
    |  * Second line (well-formatted)
    |  */
    |""" 

  """/**
    | * Scaladoc should be retained.
    | * @param first line changes indent.
    | *     second line retains indent.
    | */""" ==>
  """/** Scaladoc should be retained.
    |  * @param first line changes indent.
    |  *     second line retains indent.
    |  */
    |"""

  """/** First
    |Second line, no leader
    |*Third line, comment ender  */""" ==>
  """/** First
    |  * Second line, no leader
    |  * Third line, comment ender
    |  */
    |"""

  """/** Ending misaligned
    |*/""" ==>
  """/** Ending misaligned
    |  */
    |"""

  """/**
    |*/""" ==>
  """/**
    |  */
    |"""
  }
  
  {
  implicit val formattingPreferences = FormattingPreferences.setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)

  """/** This method applies f to each 
    | * element of the given list.
    | */""" ==>
  """/** This method applies f to each
    |  * element of the given list.
    |  */
    |""" 

  """/** Foo
    |Bar
    |*Baz  */""" ==>
  """/** Foo
    |  * Bar
    |  * Baz
    |  */
    |"""

  """/** Line on start, should stay
    |*/""" ==>
  """/** Line on start, should stay
    |  */
    |"""

  """/**
    |*/""" ==>
  """/**
    |  */
    |"""
  }
  
  {
  implicit val formattingPreferences = FormattingPreferences
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
  """/** This method applies f to each 
    | * element of the given list.
    | */""" ==>
  """/** This method applies f to each
    |  * element of the given list.
    |  */
    |""" 

  """/**
    | * Scaladoc should be retained.
    | * @param first line retains indent.
    | *     second line retains indent.
    | */""" ==>
  """/** Scaladoc should be retained.
    |  * @param first line retains indent.
    |  *     second line retains indent.
    |  */
    |"""

  """/** Foo
    |Bar
    |*Baz  */""" ==>
  """/** Foo
    |  * Bar
    |  * Baz
    |  */
    |"""

  """/** Misaligned close
    |*/""" ==>
  """/** Misaligned close
    |  */
    |"""

  """/**
    |*/""" ==>
  """/**
    |  */
    |"""

  """/**          This method applies f to each
    | * element of the given list.
    | */""" ==>
  """/** This method applies f to each
    |  * element of the given list.
    |  */
    |"""
  }
}
