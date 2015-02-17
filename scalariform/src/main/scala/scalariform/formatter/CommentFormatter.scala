package scalariform.formatter

import scalariform.parser._
import scalariform.utils._
import scalariform.lexer._
import scalariform.formatter.preferences._
import scala.annotation.tailrec

/** Formats multi-line comments - non-// comments. This will strip any empty lines at the start of
  * the comment, add comment leaders to any lines missing them, and correct asterisk alignment if
  * it's incorrect.
  */
trait CommentFormatter { self: HasFormattingPreferences with ScalaFormatter ⇒
  /** A case class representing a comment.
    * @param trimmedAfterStarText the text of the comment after the star, trimmed of whitespace
    * @param leadingSpaceCount the number of literal spaces preceding the text, after any star. Zero
    *     if there was no star.
    */
  case class Comment(trimmedAfterStarText: String, leadingSpaceCount: Int)

  val LineDelimiter = """\r?\n""".r

  /** Matches a comment leader and contents. Includes newlines. */
  val CommentContents = """(?s)(/\*\*?)(.*)\*/""".r
  /** Special-case handling of a nested comment end. */
  val NestedCommentEnd = """\s*(\*/.*)""".r
  /** Matches a comment line that starts with whitespace and includes an asterisk. This captures the
    * spaces after the asterisk, plus the main body contents.
    */
  val CommentWithStar = """\s*\*( *)(.*)""".r

  /** Returns the contents of a comment string, as a sequence of comments, paired with the comment
    * start sequence. This preserves empty lines. Each line has any leading whitespace, asterisk,
    * and following whitespace trimmed, plus end-of-line whitespace stripped.
    * This assumes multi-line comments, starting with / * or / **.
    */
  private def getComments(comment: String): (String, Seq[Comment]) = {
    comment match {
      case CommentContents(start, contents) =>
        val comments = for (rawLine <- LineDelimiter.split(contents)) yield rawLine match {
          case NestedCommentEnd(afterStar) => Comment(afterStar.trim, 0)
          case CommentWithStar(spaces, afterStar) => Comment(afterStar.trim, spaces.length)
          case noStar => Comment(rawLine.trim, 0)
        }
        (start, comments)
      case _ => ("/**", Seq(Comment(comment, 0)))
    }
  }

  def formatScaladocComment(commentToken: HiddenToken, indentLevel: Int): String = {
    // Only format multi-line comments.
    if (commentToken.rawText contains '\n') {

      val (start, comments) = getComments(commentToken.rawText)
      // Drop any leading empty lines.
      val trimmedComments = comments dropWhile { _.trimmedAfterStarText.isEmpty }

      val hasTwoAsterisks = start == "/**"
      val alignBeneathSecondAsterisk = hasTwoAsterisks &&
        formattingPreferences(PlaceScaladocAsterisksBeneathSecondAsterisk)

      val startOnFirstLine = formattingPreferences(MultilineScaladocCommentsStartOnFirstLine)

      val beforeStarSpaces = if (alignBeneathSecondAsterisk) "  " else " "
      val afterStarSpaces =
        if (startOnFirstLine && hasTwoAsterisks && !alignBeneathSecondAsterisk) "  " else " "

      // Start the comment with the same characters as we had originally.
      val sb = new StringBuilder(start)
      var firstLine = true
      for (comment <- trimmedComments) {
        if (firstLine && startOnFirstLine) {
          sb.append(" ").append(comment.trimmedAfterStarText)
        } else {
          sb.append(newlineSequence).indent(indentLevel).append(beforeStarSpaces).append("*")
          if (comment.trimmedAfterStarText.nonEmpty) {
            // Preserve whitespace after the star, but only if we're sure it's correct.
            // TODO(jkinkead): This would be more accurate if it also checked the initial indent -
            // that is, we should only correct the space after the asterisk if the space before the
            // asterisk was wrong.
            if (comment.leadingSpaceCount > 2) {
              sb.append(" " * comment.leadingSpaceCount)
            } else {
              sb.append(afterStarSpaces)
            }
            sb.append(comment.trimmedAfterStarText)
          }
        }
        firstLine = false
      }
      if (trimmedComments.nonEmpty && trimmedComments.last.trimmedAfterStarText == "") {
        sb.append("/")
      } else {
        sb.append(newlineSequence).indent(indentLevel).append(beforeStarSpaces).append("*/")
      }
      sb.toString
    } else {
      commentToken.rawText
    }
  }

  /** Formats a single-line comment (starting with '//'). This merely trims the text. */
  def formatSingleLineComment(commentToken: HiddenToken): String = {
    commentToken.rawText.trim + newlineSequence
  }
}
