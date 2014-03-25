package org.smop.markup.parsing

import util.parsing.combinator.RegexParsers
import collection.mutable.ListBuffer
import util.parsing.input.PagedSeqReader
import collection.immutable.PagedSeq
import java.io.StringReader
import org.smop.markup.ast._

/**
 * Parser for a subset of XML, with added placeholders.
 * This is meant to provide a base for XML literals in Scala code, using SIP-11 interpolation syntax. The parsers produce an AST which should be transformed
 * into a bunch of nested constructor calls for your favorite XML types.
 */
class XmlMarkup(val entityMap: Map[String, String]) extends RegexParsers {
  override def skipWhitespace = false

  /**
   * Top-level XML-fragment parser. Now that the document itself is already delimited, we can lift the restriction that a literal is always exactly one
   * element.
   */
  lazy val mixed: Parser[List[MNode]] = opt(text)~rep(nonText~opt(text)) ^^ makeMixed

  lazy val string: Parser[String] = entityRef | decRef | hexRef | plainText

  def stringNoQuote(quote: String): Parser[String] = rep(entityRef | decRef | hexRef | noQuote(quote)) ^^ { _.mkString }

  lazy val text: Parser[MText] = rep1(string) ^^ { l => MText(l.mkString) }

  lazy val entityRef: Parser[String] = "&"~> name <~";" >> makeEntityRef

  val decRef: Parser[String] = "&#"~> "[0-9]+".r <~";" ^^ {_.toInt.toChar.toString}

  val hexRef: Parser[String] = "&#x"~> "[0-9A-Fa-f]+".r <~";" ^^ { hex => Integer.valueOf(hex,16).intValue.toChar.toString }
  
  val plainText: Parser[String] = "[^<&]+".r

  def noQuote(quote: String): Parser[String] = s"[^<&$quote]+".r

  val space: Parser[String] = "[\u0009\u000A\u000D\u0020]+".r

  lazy val nonText: Parser[MNode] = placeholder | emptyElement | element

  lazy val placeholder: Parser[MNode] = XmlMarkup.placeholderString ^^ {_ => MPlaceholder}
  
  lazy val emptyElement: Parser[MElement] = "<"~> name ~ attributes <~ opt(space)<~"/>" ^^ makeEmptyElement

  lazy val element: Parser[MElement] = ("<"~> name ~ attributes <~opt(space)<~">")~ mixed ~("</"~> name <~opt(space)<~">") >> makeElement

  lazy val attributes: Parser[(Seq[(String, MString)], Boolean)] = rep(space~>attribute)~opt(placeholder) ^^ {case l~op => (l, op.isDefined)}
  
  lazy val attribute: Parser[(String,MString)] = (name <~ eq) ~ attributeValue ^^ {case n~v => (n,v)}

  lazy val eq: Parser[String] = opt(space)~>"="<~opt(space)

  lazy val attributeValue: Parser[MString] = (XmlMarkup.placeholderString ^^ {_ => MPlaceholderString }) |
    ((("\""~> stringNoQuote("\"") <~"\"") | ("'"~> stringNoQuote("'") <~"'")) ^^ {s => MWrappedString(s)})

  val nameStartChar: Parser[String] = "[:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]".r // how to represent these? | [x10000-#xEFFFF]

  lazy val nameChars: Parser[String] = "[-.0-9:A-Z_a-z\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F-\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]*".r

  lazy val name: Parser[String] = nameStartChar~nameChars ^^ {case start~rest => start+rest}

  private val makeEntityRef: String => Parser[String] = {
    case e => entityMap.get(e).fold[Parser[String]](err(s"unknown entity $e"))(success(_))
  }

  private val makeEmptyElement: String~(Seq[(String, MString)], Boolean) => MElement = {
    case elementName~attributesPlus => MElement(elementName, attributesPlus._1, attributesPlus._2, true, Nil)
  }

  private val makeElement: String~(Seq[(String, MString)], Boolean)~List[MNode]~String => Parser[MElement] = { case open~attributesPlus~children~close =>
    if (open == close) {
      success(MElement(open, attributesPlus._1, attributesPlus._2, false, children))
    } else
      err(s"mismatched open and close tags ($open versus $close)")
  }
  
  private val makeMixed: Option[MText]~List[MNode~Option[MText]] => List[MNode] = {
    case Some(txt)~l => txt :: makeMixed2(l)
    case None~l => makeMixed2(l)
  }
  
  private def makeMixed2(l: List[MNode~Option[MText]]): List[MNode] = {
    val acc = new ListBuffer[MNode]
    for (node~optionText <- l) {
      acc += node
      if (optionText.isDefined)
        acc += optionText.get
    }
    acc.toList
  }
}

object XmlMarkup {
  val xml5StandardEntities = Map("amp" ->"&", "lt"->"<", "gt"->">", "apos"->"'", "quot"->"\"")
  val placeholderString = "<\u0007>"
  private val standardParser = new XmlMarkup(xml5StandardEntities)

  def parse(markup: String): Either[String, List[MNode]] = doParse(standardParser, markup)
  def parse(markup: String, entityMap: Map[String, String]): Either[String, List[MNode]] = doParse(new XmlMarkup(entityMap), markup)
  private def doParse(parser: XmlMarkup, markup: String): Either[String, List[MNode]] = {
    val reader = new PagedSeqReader(PagedSeq.fromReader(new StringReader(markup)))
    parser.parseAll(parser.mixed, reader) match {
      case parser.Success(l: List[MNode], _) => Right(l)
      case parser.NoSuccess(msg, _) => Left(msg)
    }
  }
}
